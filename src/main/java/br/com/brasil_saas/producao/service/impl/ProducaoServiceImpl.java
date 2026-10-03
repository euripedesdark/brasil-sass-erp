package br.com.brasil_saas.producao.service.impl;

import br.com.brasil_saas.producao.model.*;
import br.com.brasil_saas.producao.repository.*;
import br.com.brasil_saas.producao.service.ProducaoService;
import br.com.brasil_saas.producao.service.ProducaoRequest;
import br.com.brasil_saas.producao.service.ItemRequest;
import br.com.brasil_saas.producao.service.CustoProducaoService;
import br.com.brasil_saas.cadastro.repository.ProdutoRepository;
import java.math.RoundingMode;
import br.com.brasil_saas.estoque.repository.SaldoEstoqueRepository;
import br.com.brasil_saas.estoque.repository.DepositoRepository;
import br.com.brasil_saas.estoque.repository.MovimentacaoEstoqueRepository;
import br.com.brasil_saas.estoque.model.SaldoEstoque;
import br.com.brasil_saas.estoque.model.MovimentacaoEstoque;
import br.com.brasil_saas.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProducaoServiceImpl implements ProducaoService {

    private final ProducaoRepository producaoRepository;
    private final ItemProducaoRepository itemRepository;
    private final SaldoEstoqueRepository saldoEstoqueRepository;
    private final DepositoRepository depositoRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;
    private final CustoProducaoService custoService;
    private final ProdutoRepository produtoRepository;

    @Override
    @Transactional
    public Producao criarOrdem(Long empresaId, ProducaoRequest request) {
        Producao p = new Producao();
        p.setEmpresaId(empresaId);
        p.setNumero(request.numero());
        p.setTipoProducao(request.tipoProducao());
        p.setProdutoFinalId(request.produtoFinalId());
        p.setQuantidadePlanejada(request.quantidadePlanejada());
        p.setUnidadeMedida(request.unidadeMedida());
        p.setDensidade(request.densidade());
        p.setStatus("ABERTO");

        List<ItemProducao> itens = new ArrayList<>();
        for (var ir : request.itens()) {
            ItemProducao item = new ItemProducao();
            item.setProducao(p);
            item.setEmpresaId(empresaId);
            item.setProdutoId(ir.produtoId());
            item.setQuantidade(ir.quantidade());
            itens.add(item);
        }
        p.setItens(itens);
        return producaoRepository.save(p);
    }

    @Override
    @Transactional
    public Producao finalizarProducao(Long empresaId, Long producaoId) {
        // o tenant e conferido junto com o id: buscar so por id permitia a uma
        // empresa finalizar a ordem de outra, baixando o insumo do estoque dela
        // e marcando a ordem de terceiro como FINALIZADO
        Producao p = producaoRepository.findById(producaoId)
                .filter(x -> x.getEmpresaId().equals(empresaId))
                .orElseThrow(() -> new BusinessException("Ordem de produção não encontrada"));

        if (!"ABERTO".equals(p.getStatus()) && !"EM_PROCESSO".equals(p.getStatus())) {
            throw new BusinessException("Ordem não pode ser finalizada no estado atual");
        }

        // custeio antes de mexer no estoque: usa os apontamentos e o custo vigente dos insumos
        var custo = custoService.calcular(empresaId, producaoId);

        // 1. Consumir estoque de insumos
        for (ItemProducao item : p.getItens()) {
            baixarEstoque(empresaId, item.getProdutoId(), item.getQuantidade(), p.getId());
        }

        // 2. Adicionar produto final ao estoque: so as unidades boas (apontado - refugo);
        //    sem apontamento de producao, a quantidade boa e a planejada (comportamento anterior)
        BigDecimal entrada = custo.quantidadeBoa();
        if (entrada != null && entrada.signum() > 0) {
            atualizarCustoMedio(empresaId, p.getProdutoFinalId(), entrada, custo.custoUnitario());
            adicionarEstoque(empresaId, p.getProdutoFinalId(), entrada, p.getId());
        }

        // 3. Gravar o custo na OP e nos itens
        p.setCustoTotal(custo.custoTotal().setScale(2, RoundingMode.HALF_UP));
        for (ItemProducao item : p.getItens()) {
            var linha = custo.materiais().stream()
                    .filter(m -> item.getProdutoId().equals(m.get("produtoId"))).findFirst().orElse(null);
            if (linha != null) {
                item.setCustoUnitario(((BigDecimal) linha.get("custoUnitario")).setScale(2, RoundingMode.HALF_UP));
                item.setCustoTotal(((BigDecimal) linha.get("custoTotal")).setScale(2, RoundingMode.HALF_UP));
            }
        }

        p.setStatus("FINALIZADO");
        p.setDataFim(LocalDateTime.now());
        return producaoRepository.save(p);
    }

    /**
     * Custo medio ponderado do produto final: (saldo atual x custo atual + entrada x custo da OP)
     * / (saldo atual + entrada). Sem custo unitario calculado, nao altera o cadastro.
     */
    private void atualizarCustoMedio(Long empresaId, Long produtoId, BigDecimal entrada, BigDecimal custoUnitarioOp) {
        if (custoUnitarioOp == null || custoUnitarioOp.signum() <= 0) return;
        var produto = produtoRepository.findByIdAndEmpresaIdAndDeletedAtIsNull(produtoId, empresaId).orElse(null);
        if (produto == null) return;
        BigDecimal saldoAtual = saldoEstoqueRepository.findByEmpresaIdAndProdutoId(empresaId, produtoId)
                .map(SaldoEstoque::getQuantidade).orElse(BigDecimal.ZERO).max(BigDecimal.ZERO);
        BigDecimal custoAtual = produto.getPrecoCusto() == null ? BigDecimal.ZERO : produto.getPrecoCusto();
        BigDecimal base = saldoAtual.add(entrada);
        BigDecimal novo = saldoAtual.multiply(custoAtual).add(entrada.multiply(custoUnitarioOp))
                .divide(base, 4, RoundingMode.HALF_UP);
        produto.setPrecoCusto(novo);
        produtoRepository.save(produto);
    }

    private void baixarEstoque(Long empresaId, Long produtoId, BigDecimal quantidade, Long origemId) {
        SaldoEstoque saldo = saldoEstoqueRepository.findByEmpresaIdAndProdutoId(empresaId, produtoId)
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

        if (saldo.getQuantidade().compareTo(quantidade) < 0) {
            throw new BusinessException("Estoque insuficiente para o produto ID: " + produtoId);
        }

        saldo.setQuantidade(saldo.getQuantidade().subtract(quantidade));
        saldoEstoqueRepository.save(saldo);

        MovimentacaoEstoque mov = new MovimentacaoEstoque();
        mov.setEmpresaId(empresaId);
        mov.setProdutoId(produtoId);
        mov.setTipo("SAIDA");
        mov.setOrigem("PRODUCAO");
        mov.setOrigemId(origemId);
        mov.setQuantidade(quantidade.negate());
        mov.setSaldoApos(saldo.getQuantidade());
        mov.setObservacao("Consumo por produção");
        movimentacaoRepository.save(mov);
    }

    private void adicionarEstoque(Long empresaId, Long produtoId, BigDecimal quantidade, Long origemId) {
        SaldoEstoque saldo = saldoEstoqueRepository.findByEmpresaIdAndProdutoId(empresaId, produtoId)
                .orElseGet(() -> {
                    SaldoEstoque novo = new SaldoEstoque();
                    novo.setEmpresaId(empresaId);
                    // deposito_id e NOT NULL: sem isso, finalizar uma OP de produto
                    // que nunca teve saldo quebrava no INSERT
                    novo.setDepositoId(depositoRepository.findByEmpresaIdAndCodigoAndAtivoTrue(empresaId, "PADRAO")
                            .orElseThrow(() -> new BusinessException("Deposito PADRAO nao encontrado para a empresa " + empresaId))
                            .getId());
                    novo.setProdutoId(produtoId);
                    novo.setQuantidade(BigDecimal.ZERO);
                    return novo;
                });

        saldo.setQuantidade(saldo.getQuantidade().add(quantidade));
        saldoEstoqueRepository.save(saldo);

        MovimentacaoEstoque mov = new MovimentacaoEstoque();
        mov.setEmpresaId(empresaId);
        mov.setProdutoId(produtoId);
        mov.setTipo("ENTRADA");
        mov.setOrigem("PRODUCAO");
        mov.setOrigemId(origemId);
        mov.setQuantidade(quantidade);
        mov.setSaldoApos(saldo.getQuantidade());
        mov.setObservacao("Entrada por produção");
        movimentacaoRepository.save(mov);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producao> listar(Long empresaId) {
        return producaoRepository.findByEmpresaIdOrderByDataInicioDesc(empresaId);
    }
}
