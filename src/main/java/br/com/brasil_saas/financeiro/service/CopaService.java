package br.com.brasil_saas.financeiro.service;

import br.com.brasil_saas.financeiro.repository.TituloRepository;
import br.com.brasil_saas.vendas.model.PedidoVenda;
import br.com.brasil_saas.vendas.repository.PedidoVendaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CO-PA lite (Controlling – Profitability Analysis):
 * receita de pedidos FATURADOS agrupada por cliente e por período (mês).
 * Custo estimado = 70% da receita quando não há CMV detalhado (placeholder
 * até integração completa de custo médio do estoque).
 */
@Service
@RequiredArgsConstructor
public class CopaService {

    private final PedidoVendaRepository pedidoVendaRepository;
    private final TituloRepository tituloRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> porCliente(Long empresaId, LocalDate de, LocalDate ate) {
        List<PedidoVenda> pedidos = pedidoVendaRepository
                .findByEmpresaIdOrderByDataEmissaoDesc(empresaId)
                .stream()
                .filter(p -> "FATURADO".equals(p.getStatus()))
                .filter(p -> p.getDataEmissao() != null)
                .filter(p -> (de == null || !p.getDataEmissao().isBefore(de)))
                .filter(p -> (ate == null || !p.getDataEmissao().isAfter(ate)))
                .toList();

        Map<Long, List<PedidoVenda>> porCli = pedidos.stream()
                .filter(p -> p.getClienteId() != null)
                .collect(Collectors.groupingBy(PedidoVenda::getClienteId));

        List<Map<String, Object>> linhas = new ArrayList<>();
        BigDecimal totalReceita = BigDecimal.ZERO;
        BigDecimal totalCusto = BigDecimal.ZERO;

        for (Map.Entry<Long, List<PedidoVenda>> e : porCli.entrySet()) {
            BigDecimal receita = e.getValue().stream()
                    .map(p -> p.getValorTotal() == null ? BigDecimal.ZERO : p.getValorTotal())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            // custo estimado 70% até CMV real
            BigDecimal custo = receita.multiply(new BigDecimal("0.70")).setScale(2, RoundingMode.HALF_UP);
            BigDecimal margem = receita.subtract(custo);
            BigDecimal pct = receita.signum() == 0 ? BigDecimal.ZERO
                    : margem.multiply(new BigDecimal("100")).divide(receita, 2, RoundingMode.HALF_UP);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("clienteId", e.getKey());
            row.put("qtdePedidos", e.getValue().size());
            row.put("receita", receita);
            row.put("custoEstimado", custo);
            row.put("margem", margem);
            row.put("margemPct", pct);
            linhas.add(row);
            totalReceita = totalReceita.add(receita);
            totalCusto = totalCusto.add(custo);
        }

        linhas.sort((a, b) -> ((BigDecimal) b.get("margem")).compareTo((BigDecimal) a.get("margem")));

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("de", de);
        out.put("ate", ate);
        out.put("totalReceita", totalReceita);
        out.put("totalCustoEstimado", totalCusto);
        out.put("totalMargem", totalReceita.subtract(totalCusto));
        out.put("clientes", linhas);
        out.put("aviso", "Custo estimado em 70% da receita até integração CMV/estoque. Paridade CO-PA SAP em evolução.");
        return out;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> porPeriodo(Long empresaId, int ano) {
        List<PedidoVenda> pedidos = pedidoVendaRepository
                .findByEmpresaIdOrderByDataEmissaoDesc(empresaId)
                .stream()
                .filter(p -> "FATURADO".equals(p.getStatus()))
                .filter(p -> p.getDataEmissao() != null && p.getDataEmissao().getYear() == ano)
                .toList();

        Map<String, BigDecimal> receitaMes = new TreeMap<>();
        for (PedidoVenda p : pedidos) {
            String mes = String.format("%04d-%02d", p.getDataEmissao().getYear(), p.getDataEmissao().getMonthValue());
            BigDecimal v = p.getValorTotal() == null ? BigDecimal.ZERO : p.getValorTotal();
            receitaMes.merge(mes, v, BigDecimal::add);
        }

        List<Map<String, Object>> meses = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> e : receitaMes.entrySet()) {
            BigDecimal rec = e.getValue();
            BigDecimal custo = rec.multiply(new BigDecimal("0.70")).setScale(2, RoundingMode.HALF_UP);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("periodo", e.getKey());
            m.put("receita", rec);
            m.put("custoEstimado", custo);
            m.put("margem", rec.subtract(custo));
            meses.add(m);
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ano", ano);
        out.put("meses", meses);
        return out;
    }
}
