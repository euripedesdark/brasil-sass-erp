package br.com.brasil_saas.compras.service;

import br.com.brasil_saas.compras.dto.*;
import br.com.brasil_saas.compras.model.ContratoFornecimento;
import br.com.brasil_saas.compras.model.ContratoFornecimentoItem;
import br.com.brasil_saas.compras.repository.ContratoFornecimentoRepository;
import br.com.brasil_saas.compras.repository.PedidoCompraRepository;
import br.com.brasil_saas.core.service.DocumentoFluxoService;
import br.com.brasil_saas.shared.exception.BusinessException;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Contratos de fornecimento: criação, ciclo de vida e liberação de pedidos contra o contrato. */
@Service
@RequiredArgsConstructor
public class ContratoFornecimentoService {

    private final ContratoFornecimentoRepository repo;
    private final PedidoCompraService pedidoService;
    private final PedidoCompraRepository pedidoRepository;
    private final DocumentoFluxoService fluxo;

    @Transactional
    public ContratoFornecimentoResponse criar(ContratoFornecimentoRequest r) {
        if (r.vigenciaFim().isBefore(r.vigenciaInicio())) throw new BusinessException("Vigência final anterior à inicial");
        String tipo = r.tipo() == null ? "QUANTIDADE" : r.tipo().toUpperCase();
        if (!tipo.equals("QUANTIDADE") && !tipo.equals("VALOR")) throw new BusinessException("Tipo deve ser QUANTIDADE ou VALOR");
        if (tipo.equals("VALOR") && (r.valorLimite() == null || r.valorLimite().signum() <= 0))
            throw new BusinessException("Contrato por VALOR exige valor limite");
        String numero = r.numero() == null || r.numero().isBlank() ? "CT-" + System.currentTimeMillis() : r.numero();
        if (repo.existsByEmpresaIdAndNumeroAndDeletedAtIsNull(r.empresaId(), numero))
            throw new BusinessException("Já existe contrato com o número " + numero);

        ContratoFornecimento c = new ContratoFornecimento();
        c.setEmpresaId(r.empresaId());
        c.setFornecedorId(r.fornecedorId());
        c.setNumero(numero);
        c.setTipo(tipo);
        c.setVigenciaInicio(r.vigenciaInicio());
        c.setVigenciaFim(r.vigenciaFim());
        c.setCondicaoPagamentoId(r.condicaoPagamentoId());
        c.setValorLimite(r.valorLimite());
        c.setObservacao(r.observacao());
        int n = 1;
        List<ContratoFornecimentoItem> itens = new ArrayList<>();
        for (var ir : r.itens()) {
            if (ir.quantidadeContratada().signum() <= 0) throw new BusinessException("Quantidade contratada deve ser maior que zero");
            if (ir.valorUnitario().signum() < 0) throw new BusinessException("Valor unitário inválido");
            ContratoFornecimentoItem i = new ContratoFornecimentoItem();
            i.setContrato(c);
            i.setEmpresaId(r.empresaId());
            i.setNumeroItem(ir.numeroItem() != null ? ir.numeroItem() : n++);
            i.setProdutoId(ir.produtoId());
            i.setDescricao(ir.descricao());
            i.setUnidade(ir.unidade());
            i.setQuantidadeContratada(ir.quantidadeContratada());
            i.setValorUnitario(ir.valorUnitario());
            itens.add(i);
        }
        c.setItens(itens);
        return ContratoFornecimentoResponse.from(repo.save(c));
    }

    @Transactional(readOnly = true)
    public ContratoFornecimentoResponse buscar(Long id, Long empresaId) {
        return ContratoFornecimentoResponse.from(obter(id, empresaId));
    }

    @Transactional(readOnly = true)
    public List<ContratoFornecimentoResponse> listar(Long empresaId) {
        return repo.findByEmpresaIdAndDeletedAtIsNullOrderByVigenciaFimAsc(empresaId).stream()
                .map(ContratoFornecimentoResponse::from).toList();
    }

    /** Contratos ativos que vencem em até {@code dias} dias. */
    @Transactional(readOnly = true)
    public List<ContratoFornecimentoResponse> aVencer(Long empresaId, int dias) {
        LocalDate limite = LocalDate.now().plusDays(dias);
        return repo.findByEmpresaIdAndStatusAndDeletedAtIsNull(empresaId, ContratoFornecimento.ATIVO).stream()
                .filter(c -> c.getVigenciaFim() != null && !c.getVigenciaFim().isAfter(limite))
                .map(ContratoFornecimentoResponse::from).toList();
    }

    @Transactional
    public ContratoFornecimentoResponse ativar(Long id, Long empresaId) {
        ContratoFornecimento c = lock(id, empresaId);
        if (!ContratoFornecimento.RASCUNHO.equals(c.getStatus())) throw new BusinessException("Apenas contratos em RASCUNHO podem ser ativados");
        c.setStatus(ContratoFornecimento.ATIVO);
        return ContratoFornecimentoResponse.from(repo.save(c));
    }

    @Transactional
    public ContratoFornecimentoResponse encerrar(Long id, Long empresaId) {
        ContratoFornecimento c = lock(id, empresaId);
        if (!ContratoFornecimento.ATIVO.equals(c.getStatus())) throw new BusinessException("Apenas contratos ATIVOS podem ser encerrados");
        c.setStatus(ContratoFornecimento.ENCERRADO);
        return ContratoFornecimentoResponse.from(repo.save(c));
    }

    @Transactional
    public ContratoFornecimentoResponse cancelar(Long id, Long empresaId) {
        ContratoFornecimento c = lock(id, empresaId);
        if (ContratoFornecimento.ENCERRADO.equals(c.getStatus()) || ContratoFornecimento.CANCELADO.equals(c.getStatus()))
            throw new BusinessException("Contrato já finalizado");
        boolean temLiberacao = c.getItens().stream().anyMatch(i -> i.getQuantidadeLiberada().signum() > 0);
        if (temLiberacao) throw new BusinessException("Contrato com liberações não pode ser cancelado; encerre-o");
        c.setStatus(ContratoFornecimento.CANCELADO);
        return ContratoFornecimentoResponse.from(repo.save(c));
    }

    /** Libera um pedido de compra a partir do contrato (SAP: release order). */
    @Transactional
    public PedidoCompraResponse liberar(Long id, Long empresaId, Long userId, ContratoLiberacaoRequest req) {
        ContratoFornecimento c = lock(id, empresaId);
        validarLiberavel(c, LocalDate.now());
        List<ItemPedidoCompraRequest> itensPedido = new ArrayList<>();
        BigDecimal valorLiberacao = BigDecimal.ZERO;
        for (Map.Entry<Long, BigDecimal> e : req.quantidades().entrySet()) {
            ContratoFornecimentoItem item = c.getItens().stream().filter(i -> i.getId().equals(e.getKey())).findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Item " + e.getKey() + " não pertence ao contrato"));
            BigDecimal qtd = e.getValue();
            if (qtd == null || qtd.signum() <= 0) throw new BusinessException("Quantidade deve ser maior que zero");
            if (qtd.compareTo(item.saldo()) > 0)
                throw new BusinessException("Item " + item.getNumeroItem() + ": quantidade excede o saldo do contrato (" + item.saldo() + ")");
            item.setQuantidadeLiberada(item.getQuantidadeLiberada().add(qtd));
            valorLiberacao = valorLiberacao.add(qtd.multiply(item.getValorUnitario()));
            itensPedido.add(new ItemPedidoCompraRequest(null, item.getProdutoId(), item.getDescricao(), qtd,
                    item.getUnidade(), item.getValorUnitario(), BigDecimal.ZERO));
        }
        if (c.getValorLimite() != null && c.getValorLiberado().add(valorLiberacao).compareTo(c.getValorLimite()) > 0)
            throw new BusinessException("Liberação excede o valor limite do contrato");
        c.setValorLiberado(c.getValorLiberado().add(valorLiberacao));
        repo.save(c);

        PedidoCompraResponse pedido = pedidoService.criar(new PedidoCompraRequest(empresaId, c.getFornecedorId(), null,
                LocalDate.now(), req.dataPrevisaoEntrega(), c.getCondicaoPagamentoId(),
                "Liberação do contrato " + c.getNumero() + (req.observacao() == null ? "" : " - " + req.observacao()),
                null, BigDecimal.ZERO, BigDecimal.ZERO, itensPedido));
        pedidoRepository.findByIdAndEmpresaId(pedido.id(), empresaId).ifPresent(p -> {
            p.setContratoId(c.getId());
            pedidoRepository.save(p);
        });
        fluxo.ligar(empresaId, userId, "CONTRATO_COMPRA", c.getId(), c.getNumero(),
                "PEDIDO_COMPRA", pedido.id(), pedido.numero(), "LIBERACAO");

        boolean esgotado = c.getItens().stream().allMatch(i -> i.saldo().signum() == 0);
        if (esgotado) { c.setStatus(ContratoFornecimento.ENCERRADO); repo.save(c); }
        return pedido;
    }

    /** Regra pura: contrato precisa estar ATIVO e dentro da vigência. */
    static void validarLiberavel(ContratoFornecimento c, LocalDate hoje) {
        if (!ContratoFornecimento.ATIVO.equals(c.getStatus())) throw new BusinessException("Contrato não está ATIVO");
        if (c.getVigenciaInicio() == null) throw new BusinessException("Contrato sem inicio de vigencia");
        if (hoje.isBefore(c.getVigenciaInicio()) || (c.getVigenciaFim() != null && hoje.isAfter(c.getVigenciaFim())))
            throw new BusinessException("Contrato fora da vigência");
    }

    private ContratoFornecimento obter(Long id, Long empresaId) {
        return repo.findByIdAndEmpresaIdAndDeletedAtIsNull(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato de fornecimento não encontrado"));
    }

    private ContratoFornecimento lock(Long id, Long empresaId) {
        return repo.findByIdForUpdate(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato de fornecimento não encontrado"));
    }
}
