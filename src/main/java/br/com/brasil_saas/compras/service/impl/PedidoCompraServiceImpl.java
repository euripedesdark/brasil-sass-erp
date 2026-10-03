package br.com.brasil_saas.compras.service.impl;

import br.com.brasil_saas.compras.dto.PedidoCompraRequest;
import br.com.brasil_saas.compras.dto.PedidoCompraResponse;
import br.com.brasil_saas.compras.model.ItemPedidoCompra;
import br.com.brasil_saas.compras.model.PedidoCompra;
import br.com.brasil_saas.compras.model.RecebimentoCompra;
import br.com.brasil_saas.compras.model.RecebimentoCompraItem;
import br.com.brasil_saas.compras.repository.PedidoCompraRepository;
import br.com.brasil_saas.compras.repository.RecebimentoCompraItemRepository;
import br.com.brasil_saas.compras.repository.RecebimentoCompraRepository;
import br.com.brasil_saas.compras.service.PedidoCompraService;
import br.com.brasil_saas.estoque.model.MovimentacaoEstoque;
import br.com.brasil_saas.estoque.model.SaldoEstoque;
import br.com.brasil_saas.estoque.repository.DepositoRepository;
import br.com.brasil_saas.estoque.repository.MovimentacaoEstoqueRepository;
import br.com.brasil_saas.estoque.repository.SaldoEstoqueRepository;
import br.com.brasil_saas.financeiro.model.Titulo;
import br.com.brasil_saas.financeiro.repository.TituloRepository;
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

@Service
@RequiredArgsConstructor
public class PedidoCompraServiceImpl implements PedidoCompraService {

    private final PedidoCompraRepository pedidoRepository;
    private final RecebimentoCompraRepository recebimentoRepository;
    private final RecebimentoCompraItemRepository recebimentoItemRepository;
    private final SaldoEstoqueRepository saldoEstoqueRepository;
    private final DepositoRepository depositoRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;
    private final TituloRepository tituloRepository;
    private final br.com.brasil_saas.financeiro.service.TituloService tituloService;

    @Override
    @Transactional
    public PedidoCompraResponse criar(PedidoCompraRequest request) {
        PedidoCompra pedido = new PedidoCompra();
        pedido.setEmpresaId(request.empresaId());
        pedido.setFornecedorId(request.fornecedorId());
        String numero = request.numero();
        if (numero == null || numero.isBlank()) numero = "PC-" + System.currentTimeMillis();
        pedido.setNumero(numero);
        pedido.setStatus(request.status() != null ? request.status() : "ABERTO");
        pedido.setDataEmissao(request.dataEmissao() != null ? request.dataEmissao() : LocalDate.now());
        pedido.setDataPrevisaoEntrega(request.dataPrevisaoEntrega());
        pedido.setCondicaoPagamentoId(request.condicaoPagamentoId());
        pedido.setObservacao(request.observacao());

        List<ItemPedidoCompra> itens = new ArrayList<>();
        BigDecimal totalProdutos = BigDecimal.ZERO;
        if (request.itens() != null) {
            int num = 1;
            for (var itemReq : request.itens()) {
                ItemPedidoCompra item = new ItemPedidoCompra();
                item.setPedido(pedido);
                item.setEmpresaId(pedido.getEmpresaId());
                item.setNumeroItem(itemReq.numeroItem() != null ? itemReq.numeroItem() : num++);
                item.setProdutoId(itemReq.produtoId());
                item.setDescricao(itemReq.descricao());
                item.setQuantidade(itemReq.quantidade());
                item.setUnidade(itemReq.unidade());
                item.setValorUnitario(itemReq.valorUnitario());
                item.setValorDesconto(itemReq.valorDesconto() != null ? itemReq.valorDesconto() : BigDecimal.ZERO);
                BigDecimal valorTotal = itemReq.quantidade().multiply(itemReq.valorUnitario()).subtract(item.getValorDesconto());
                item.setValorTotal(valorTotal);
                item.setCriadoEstoque(false);
                itens.add(item);
                totalProdutos = totalProdutos.add(valorTotal);
            }
        }

        pedido.setItens(itens);
        pedido.setValorProdutos(totalProdutos);
        pedido.setValorDesconto(request.valorDesconto() != null ? request.valorDesconto() : BigDecimal.ZERO);
        pedido.setValorFrete(request.valorFrete() != null ? request.valorFrete() : BigDecimal.ZERO);
        pedido.setValorTotal(totalProdutos.subtract(pedido.getValorDesconto()).add(pedido.getValorFrete()));
        return PedidoCompraResponse.from(pedidoRepository.save(pedido));
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoCompraResponse buscarPorId(Long id) {
        PedidoCompra pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido de compra nao encontrado"));
        return PedidoCompraResponse.from(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoCompraResponse> listarPorEmpresa(Long empresaId) {
        return pedidoRepository.findByEmpresaIdOrderByDataEmissaoDesc(empresaId).stream()
                .map(PedidoCompraResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public void receber(Long id) {
        PedidoCompra pedido = pedidoRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido de compra nao encontrado"));
        if (!"ABERTO".equals(pedido.getStatus())) {
            throw new BusinessException("Apenas pedidos ABERTOS podem ser recebidos");
        }

        Map<Long, BigDecimal> recebidas = new java.util.HashMap<>();
        for (ItemPedidoCompra item : pedido.getItens()) {
            if (item.getProdutoId() != null && !Boolean.TRUE.equals(item.getCriadoEstoque())) {
                entrarEstoque(pedido.getEmpresaId(), item.getProdutoId(), item.getQuantidade(), pedido.getId());
                item.setQuantidadeRecebida(item.getQuantidade());
                item.setCriadoEstoque(true);
                recebidas.put(item.getId(), item.getQuantidade());
            }
        }

        registrarRecebimento(pedido, recebidas);
        Titulo titulo = criarTituloCompra(pedido);
        pedido.setTituloId(titulo.getId());
        tituloService.gerarParcelas(pedido.getEmpresaId(), titulo.getId(), pedido.getCondicaoPagamentoId());
        pedido.setStatus("RECEBIDO");
        pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public void receberParcial(Long id, Map<Long, BigDecimal> quantidades) {
        PedidoCompra pedido = pedidoRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido de compra nao encontrado"));
        if ("CANCELADO".equals(pedido.getStatus()) || "RECEBIDO".equals(pedido.getStatus())) {
            throw new BusinessException("Pedido nao pode receber novas quantidades no status " + pedido.getStatus());
        }
        if (quantidades == null || quantidades.isEmpty()) {
            throw new BusinessException("Informe ao menos uma quantidade de recebimento");
        }

        boolean recebeuAlgo = false;
        boolean tudoRecebido = true;
        Map<Long, BigDecimal> recebidas = new java.util.HashMap<>();

        for (ItemPedidoCompra item : pedido.getItens()) {
            BigDecimal entrada = quantidades.get(item.getId());
            BigDecimal jaRecebida = item.getQuantidadeRecebida() == null ? BigDecimal.ZERO : item.getQuantidadeRecebida();
            BigDecimal restante = item.getQuantidade().subtract(jaRecebida);

            if (entrada == null) {
                if (restante.signum() > 0) tudoRecebido = false;
                continue;
            }
            if (entrada.signum() < 0 || entrada.compareTo(restante) > 0) {
                throw new BusinessException("Quantidade recebida invalida para o item " + item.getNumeroItem()
                        + ". Restante: " + restante + ", informado: " + entrada);
            }
            if (entrada.signum() > 0) {
                if (item.getProdutoId() != null) {
                    entrarEstoque(pedido.getEmpresaId(), item.getProdutoId(), entrada, pedido.getId());
                }
                item.setQuantidadeRecebida(jaRecebida.add(entrada));
                item.setCriadoEstoque(item.getQuantidadeRecebida().compareTo(item.getQuantidade()) >= 0);
                recebidas.put(item.getId(), entrada);
                recebeuAlgo = true;
            }
            if (item.getQuantidadeRecebida().compareTo(item.getQuantidade()) < 0) tudoRecebido = false;
        }

        if (!recebeuAlgo) throw new BusinessException("Nenhuma quantidade nova foi recebida");
        registrarRecebimento(pedido, recebidas);

        if (tudoRecebido) {
            pedido.setStatus("RECEBIDO");
            if (pedido.getTituloId() == null) {
                Titulo titulo = criarTituloCompra(pedido);
                pedido.setTituloId(titulo.getId());
                tituloService.gerarParcelas(pedido.getEmpresaId(), titulo.getId(), pedido.getCondicaoPagamentoId());
            }
        } else {
            pedido.setStatus("PARCIAL");
        }
        pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public void cancelar(Long id) {
        PedidoCompra pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido de compra nao encontrado"));
        if ("RECEBIDO".equals(pedido.getStatus())) {
            throw new BusinessException("Pedidos RECEBIDOS nao podem ser cancelados diretamente");
        }
        pedido.setStatus("CANCELADO");
        pedidoRepository.save(pedido);
    }

    private Titulo criarTituloCompra(PedidoCompra pedido) {
        Titulo titulo = new Titulo();
        titulo.setEmpresaId(pedido.getEmpresaId());
        titulo.setTipo("P");
        titulo.setNumeroDocumento(pedido.getNumero());
        titulo.setDescricao("Compra - Pedido " + pedido.getNumero());
        titulo.setPessoaId(pedido.getFornecedorId());
        titulo.setValorOriginal(pedido.getValorTotal());
        titulo.setValorSaldo(pedido.getValorTotal());
        titulo.setDataEmissao(LocalDate.now());
        titulo.setDataVencimento(LocalDate.now().plusDays(30));
        titulo.setStatus("ABERTO");
        return tituloRepository.save(titulo);
    }

    private void registrarRecebimento(PedidoCompra pedido, Map<Long, BigDecimal> recebidas) {
        if (recebidas.isEmpty()) return;

        RecebimentoCompra recebimento = new RecebimentoCompra();
        recebimento.setEmpresaId(pedido.getEmpresaId());
        recebimento.setPedidoId(pedido.getId());
        recebimento.setNumero("REC-" + System.currentTimeMillis());
        recebimento.setDataRecebimento(LocalDate.now());
        recebimento.setStatus("RECEBIDO");

        BigDecimal valorTotal = BigDecimal.ZERO;
        List<RecebimentoCompraItem> itens = new ArrayList<>();
        for (ItemPedidoCompra item : pedido.getItens()) {
            BigDecimal quantidade = recebidas.get(item.getId());
            if (quantidade == null || quantidade.signum() <= 0 || item.getProdutoId() == null) continue;

            RecebimentoCompraItem recebido = new RecebimentoCompraItem();
            recebido.setEmpresaId(pedido.getEmpresaId());
            recebido.setProdutoId(item.getProdutoId());
            recebido.setQuantidadePedida(item.getQuantidade());
            recebido.setQuantidadeRecebida(quantidade);
            recebido.setValorUnitario(item.getValorUnitario());
            itens.add(recebido);
            valorTotal = valorTotal.add(quantidade.multiply(item.getValorUnitario()));
        }

        recebimento.setValorTotal(valorTotal);
        RecebimentoCompra salvo = recebimentoRepository.save(recebimento);
        for (RecebimentoCompraItem item : itens) item.setRecebimentoId(salvo.getId());
        recebimentoItemRepository.saveAll(itens);
    }

    private void entrarEstoque(Long empresaId, Long produtoId, BigDecimal quantidade, Long origemId) {
        SaldoEstoque saldo = saldoEstoqueRepository.findByEmpresaIdAndProdutoIdForUpdate(empresaId, produtoId)
                .orElseGet(() -> {
                    SaldoEstoque novo = new SaldoEstoque();
                    novo.setEmpresaId(empresaId);
                    novo.setDepositoId(depositoRepository.findByEmpresaIdAndCodigoAndAtivoTrue(empresaId, "PADRAO")
                            .orElseThrow(() -> new BusinessException("Deposito PADRAO nao encontrado para a empresa " + empresaId))
                            .getId());
                    novo.setProdutoId(produtoId);
                    novo.setQuantidade(BigDecimal.ZERO);
                    return novo;
                });

        BigDecimal saldoAtual = saldo.getQuantidade() != null ? saldo.getQuantidade() : BigDecimal.ZERO;
        saldo.setQuantidade(saldoAtual.add(quantidade));
        saldoEstoqueRepository.save(saldo);

        MovimentacaoEstoque mov = new MovimentacaoEstoque();
        mov.setEmpresaId(empresaId);
        mov.setProdutoId(produtoId);
        mov.setTipo("ENTRADA");
        mov.setOrigem("COMPRA");
        mov.setOrigemId(origemId);
        mov.setQuantidade(quantidade);
        mov.setSaldoApos(saldo.getQuantidade());
        mov.setObservacao("Entrada por compra");
        movimentacaoRepository.save(mov);
    }
}
