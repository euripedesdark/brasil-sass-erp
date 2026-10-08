package br.com.brasil_saas.producao.service.impl;

import br.com.brasil_saas.compras.model.SolicitacaoCompra;
import br.com.brasil_saas.compras.model.SolicitacaoCompraItem;
import br.com.brasil_saas.compras.repository.SolicitacaoCompraRepository;
import br.com.brasil_saas.estoque.repository.ReservaEstoqueRepository;
import br.com.brasil_saas.estoque.repository.SaldoEstoqueRepository;
import br.com.brasil_saas.producao.model.EstruturaProduto;
import br.com.brasil_saas.producao.model.Producao;
import br.com.brasil_saas.producao.repository.EstruturaProdutoRepository;
import br.com.brasil_saas.producao.service.ItemRequest;
import br.com.brasil_saas.producao.service.MrpRequest;
import br.com.brasil_saas.producao.service.MrpService;
import br.com.brasil_saas.producao.service.ProducaoRequest;
import br.com.brasil_saas.producao.service.ProducaoService;
import br.com.brasil_saas.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * MRP multinivel.
 *
 * Diferenca para a versao anterior: o estoque e abatido em TODO no da estrutura,
 * nao so nas folhas. Antes, um semiacabado em estoque era ignorado e o MRP
 * mandava comprar toda a materia-prima dele. Agora a necessidade liquida do
 * semiacabado e que desce para os componentes.
 *
 * O saldo e consumido uma unica vez na rodada: se o mesmo componente aparece em
 * dois ramos, o estoque nao e contado duas vezes.
 */
@Service
@RequiredArgsConstructor
public class MrpServiceImpl implements MrpService {

    private static final int ESCALA = 4;
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyMMddHHmmss");

    private final EstruturaProdutoRepository estrutura;
    private final SaldoEstoqueRepository saldo;
    private final ProducaoService producaoService;
    private final SolicitacaoCompraRepository solicitacaoRepository;
    private final ReservaEstoqueRepository reservaRepository;

    /** Estado de uma rodada de calculo. */
    private static final class Rodada {
        final Map<Long, BigDecimal> estoqueRestante = new HashMap<>();
        final Map<Long, Linha> linhas = new LinkedHashMap<>();
        final Map<Long, List<EstruturaProduto>> cacheBom = new HashMap<>();
    }

    private static final class Linha {
        Long produtoId;
        int nivel;
        BigDecimal bruta = BigDecimal.ZERO;
        BigDecimal estoqueInicial = BigDecimal.ZERO;
        BigDecimal utilizado = BigDecimal.ZERO;
        BigDecimal liquida = BigDecimal.ZERO;
        boolean temBom;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> simular(Long empresaId, MrpRequest request) {
        return toRows(calcular(empresaId, request));
    }

    @Override
    @Transactional
    public Map<String, Object> gerarSugestoes(Long empresaId, Long usuarioId, MrpRequest request) {
        Rodada r = calcular(empresaId, request);
        String stamp = LocalDateTime.now().format(TS);

        List<Linha> comprar = new ArrayList<>();
        List<Linha> produzir = new ArrayList<>();
        for (Linha l : r.linhas.values()) {
            if (l.liquida.signum() <= 0) continue;
            (l.temBom ? produzir : comprar).add(l);
        }

        Map<String, Object> out = new LinkedHashMap<>();

        List<Map<String, Object>> ops = new ArrayList<>();
        for (Linha l : produzir) {
            List<ItemRequest> itens = new ArrayList<>();
            for (EstruturaProduto e : bom(empresaId, l.produtoId, r)) {
                if (!Boolean.TRUE.equals(e.getAtivo())) continue;
                itens.add(new ItemRequest(e.getProdutoFilhoId(),
                        l.liquida.multiply(fator(e)).setScale(ESCALA, RoundingMode.HALF_UP)));
            }
            ProducaoRequest pr = new ProducaoRequest(
                    "MRP" + stamp + "-" + l.produtoId, "INDUSTRIA", l.produtoId,
                    l.liquida, "UN", null, itens);
            Producao op = producaoService.criarOrdem(empresaId, pr);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("ordemId", op.getId());
            m.put("numero", op.getNumero());
            m.put("produtoId", l.produtoId);
            m.put("quantidade", l.liquida);
            ops.add(m);
        }
        out.put("ordensProducao", ops);

        if (comprar.isEmpty()) {
            out.put("solicitacaoCompra", null);
        } else {
            SolicitacaoCompra sc = new SolicitacaoCompra();
            sc.setEmpresaId(empresaId);
            sc.setSolicitanteId(usuarioId);
            sc.setNumero(("MRP" + stamp).substring(0, Math.min(20, ("MRP" + stamp).length())));
            sc.setObservacao("Gerada pelo MRP para produto " + request.produtoId()
                    + " x " + request.quantidade());
            for (Linha l : comprar) {
                SolicitacaoCompraItem it = new SolicitacaoCompraItem();
                it.setEmpresaId(empresaId);
                it.setSolicitacao(sc);
                it.setProdutoId(l.produtoId);
                it.setQuantidade(l.liquida);
                it.setObservacao("Necessidade liquida MRP");
                sc.getItens().add(it);
            }
            SolicitacaoCompra salva = solicitacaoRepository.save(sc);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("solicitacaoId", salva.getId());
            m.put("numero", salva.getNumero());
            m.put("itens", comprar.size());
            out.put("solicitacaoCompra", m);
        }
        return out;
    }

    // ---------------------------------------------------------------- calculo

    private Rodada calcular(Long empresaId, MrpRequest request) {
        if (request == null || request.produtoId() == null || request.quantidade() == null
                || request.quantidade().signum() <= 0) {
            throw new BusinessException("Produto e quantidade positiva são obrigatórios");
        }
        Rodada r = new Rodada();
        demanda(empresaId, request.produtoId(), request.quantidade(), 0, new LinkedHashSet<>(), r);
        return r;
    }

    private void demanda(Long empresaId, Long produtoId, BigDecimal qtd, int nivel,
                         LinkedHashSet<Long> caminho, Rodada r) {
        if (!caminho.add(produtoId)) {
            throw new BusinessException("Ciclo detectado na BOM: " + caminho + " -> " + produtoId);
        }
        try {
            Linha l = r.linhas.computeIfAbsent(produtoId, id -> {
                Linha n = new Linha();
                n.produtoId = id;
                return n;
            });
            l.nivel = Math.max(l.nivel, nivel);
            l.bruta = l.bruta.add(qtd);

            BigDecimal restante = r.estoqueRestante.computeIfAbsent(produtoId, id -> {
                // Estoque livre = saldo fisico menos o que ja esta reservado para vendas/separacao.
                // Sem isso o MRP promete a mesma unidade para a producao e para o cliente.
                BigDecimal q = saldo.findByEmpresaIdAndProdutoId(empresaId, id).map(s -> {
                    BigDecimal fisico = s.getQuantidade() == null ? BigDecimal.ZERO : s.getQuantidade();
                    BigDecimal reservado = s.getDepositoId() == null ? null
                            : reservaRepository.sumAtivas(empresaId, s.getDepositoId(), id);
                    return fisico.subtract(reservado == null ? BigDecimal.ZERO : reservado);
                }).orElse(BigDecimal.ZERO);
                return q.max(BigDecimal.ZERO);
            });
            if (l.estoqueInicial.signum() == 0 && l.utilizado.signum() == 0) {
                l.estoqueInicial = restante;
            }
            BigDecimal usar = restante.min(qtd);
            r.estoqueRestante.put(produtoId, restante.subtract(usar));
            l.utilizado = l.utilizado.add(usar);
            BigDecimal liquida = qtd.subtract(usar);
            l.liquida = l.liquida.add(liquida);

            List<EstruturaProduto> filhos = bom(empresaId, produtoId, r);
            boolean ativoNaBom = false;
            for (EstruturaProduto e : filhos) {
                if (!Boolean.TRUE.equals(e.getAtivo())) continue;
                ativoNaBom = true;
                if (liquida.signum() > 0) {
                    demanda(empresaId, e.getProdutoFilhoId(),
                            liquida.multiply(fator(e)).setScale(ESCALA, RoundingMode.HALF_UP),
                            nivel + 1, new LinkedHashSet<>(caminho), r);
                }
            }
            l.temBom = ativoNaBom;
        } finally {
            caminho.remove(produtoId);
        }
    }

    private List<EstruturaProduto> bom(Long empresaId, Long produtoId, Rodada r) {
        return r.cacheBom.computeIfAbsent(produtoId, id ->
                estrutura.findByEmpresaIdAndProdutoPaiIdAndDeletedAtIsNullOrderByNivelAscIdAsc(empresaId, id));
    }

    /** quantidade * (1 + perda%/100), tolerando perda nula. */
    private BigDecimal fator(EstruturaProduto e) {
        BigDecimal perda = e.getPerdaPercentual() == null ? BigDecimal.ZERO : e.getPerdaPercentual();
        BigDecimal q = e.getQuantidade() == null ? BigDecimal.ZERO : e.getQuantidade();
        return q.multiply(BigDecimal.ONE.add(perda.divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP)));
    }

    private List<Map<String, Object>> toRows(Rodada r) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Linha l : r.linhas.values()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("produtoId", l.produtoId);
            row.put("nivel", l.nivel);
            row.put("necessidadeBruta", l.bruta);
            row.put("estoqueDisponivel", l.estoqueInicial);
            row.put("estoqueUtilizado", l.utilizado);
            row.put("necessidadeLiquida", l.liquida);
            row.put("acao", l.liquida.signum() <= 0 ? "SEM_ACAO" : (l.temBom ? "PRODUZIR" : "COMPRAR"));
            out.add(row);
        }
        return out;
    }
}
