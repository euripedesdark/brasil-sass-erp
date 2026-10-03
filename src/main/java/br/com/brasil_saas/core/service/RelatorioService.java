package br.com.brasil_saas.core.service;

import br.com.brasil_saas.core.model.RelatorioResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Relatórios gerenciais.
 *
 * Todas as consultas usam o schema "brasil_saas" e o tenant (empresa_id) é sempre
 * parametrizado — nunca interpolado — para evitar injecao e vazamento entre empresas.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RelatorioService {

    private static final String SCHEMA = "brasil_saas.";

    private final JdbcTemplate jdbcTemplate;

    public RelatorioResponse gerarRelatorio(String tipo, Long empresaId, Map<String, Object> filtros) {
        log.info("Gerando relatório {} para empresa {}", tipo, empresaId);

        String tipoNormalizado = tipo == null ? "" : tipo.trim().toUpperCase(Locale.ROOT);
        RelatorioResponse response = new RelatorioResponse();
        List<Map<String, Object>> dados;
        Map<String, Object> resumo = new LinkedHashMap<>();
        String periodo;

        LocalDate inicio = data(filtros, "dataInicio");
        LocalDate fim = data(filtros, "dataFim");
        periodo = (inicio == null ? "início" : inicio.toString())
                + " a " + (fim == null ? "hoje" : fim.toString());

        switch (tipoNormalizado) {
            case "VENDAS" -> {
                dados = relatorioVendas(empresaId, inicio, fim);
                resumo.put("totalVendas", total(dados, "valor_total"));
                resumo.put("quantidadePedidos", total(dados, "quantidade_pedidos"));
                response.setNomeRelatorio("Relatório de Vendas");
            }
            case "FINANCEIRO" -> {
                dados = relatorioFinanceiro(empresaId, inicio, fim);
                resumo.put("totalReceitas", total(dados, "valor_original", "R"));
                resumo.put("totalDespesas", total(dados, "valor_original", "P"));
                response.setNomeRelatorio("Fluxo de Caixa / Financeiro");
            }
            case "PRODUCAO" -> {
                dados = relatorioProducao(empresaId, inicio, fim);
                resumo.put("totalProduzido", total(dados, "quantidade_planejada"));
                resumo.put("custoTotal", total(dados, "custo_total"));
                response.setNomeRelatorio("Rendimento de Produção");
            }
            case "FISCAL" -> {
                dados = relatorioFiscal(empresaId, inicio, fim);
                resumo.put("totalNotas", total(dados, "quantidade_notas"));
                resumo.put("valorTotalNotas", total(dados, "valor_total"));
                response.setNomeRelatorio("Resumo de Notas Fiscais");
            }
            default -> throw new IllegalArgumentException("Tipo de relatório não suportado: " + tipo);
        }

        response.setDados(dados);
        response.setResumo(resumo);
        response.setPeriodo(periodo);
        return response;
    }

    private List<Map<String, Object>> relatorioVendas(Long empresaId, LocalDate inicio, LocalDate fim) {
        String sql = """
            SELECT p.numero,
                   p.data_emissao,
                   COALESCE(pe.nome, 'Cliente #' || p.cliente_id) AS cliente,
                   p.valor_produtos,
                   p.valor_desconto,
                   p.valor_total,
                   p.status,
                   COUNT(pi.id) AS quantidade_pedidos
            FROM %1$sbc_ven_pedido p
            LEFT JOIN %1$sbc_cad_cliente c ON c.id = p.cliente_id
            LEFT JOIN %1$sbc_cad_pessoa  pe ON pe.id = c.pessoa_id
            LEFT JOIN %1$sbc_ven_pedido_item pi ON pi.pedido_id = p.id
            WHERE p.empresa_id = ? AND p.deleted_at IS NULL
            
             GROUP BY p.numero, p.data_emissao, pe.nome, p.cliente_id,
                      p.valor_produtos, p.valor_desconto, p.valor_total, p.status
             """.formatted(SCHEMA);
        return consultar(sql, empresaId, inicio, fim, "p.data_emissao");
    }

    private List<Map<String, Object>> relatorioFinanceiro(Long empresaId, LocalDate inicio, LocalDate fim) {
        String sql = """
            SELECT t.numero_documento,
                   t.data_emissao,
                   t.data_vencimento,
                   t.descricao,
                   t.tipo,
                   t.valor_original,
                   t.valor_saldo,
                   t.status
            FROM %1$sbc_fin_titulo t
            WHERE t.empresa_id = ? AND t.deleted_at IS NULL
            """.formatted(SCHEMA);
        return consultar(sql, empresaId, inicio, fim, "t.data_emissao");
    }

    private List<Map<String, Object>> relatorioProducao(Long empresaId, LocalDate inicio, LocalDate fim) {
        String sql = """
            SELECT o.numero,
                   o.data_inicio,
                   o.data_fim,
                   COALESCE(pr.nome, 'Produto #' || o.produto_final_id) AS produto_final,
                   o.quantidade_planejada,
                   o.custo_total,
                   o.status,
                   o.tipo_producao
            FROM %1$sbc_prod_ordem o
            LEFT JOIN %1$sbc_cad_produto pr ON pr.id = o.produto_final_id
            WHERE o.empresa_id = ? AND o.deleted_at IS NULL
            """.formatted(SCHEMA);
        return consultar(sql, empresaId, inicio, fim, "o.data_inicio");
    }

    private List<Map<String, Object>> relatorioFiscal(Long empresaId, LocalDate inicio, LocalDate fim) {
        String sql = """
            SELECT n.numero,
                   n.serie,
                   n.natureza_operacao,
                   n.data_emissao,
                   n.status,
                   n.valor_total,
                   COUNT(1) AS quantidade_notas
            FROM %1$sbc_fis_nfe n
            WHERE n.empresa_id = ? AND n.deleted_at IS NULL
            GROUP BY n.id, n.numero, n.serie, n.natureza_operacao, n.data_emissao, n.status, n.valor_total
            """.formatted(SCHEMA);
        return consultar(sql, empresaId, inicio, fim, "n.data_emissao");
    }

    private List<Map<String, Object>> consultar(String baseSql, Long empresaId,
                                                 LocalDate inicio, LocalDate fim, String colunaData) {
        StringBuilder sql = new StringBuilder(baseSql);
        List<Object> params = new ArrayList<>();
        params.add(empresaId);
        StringBuilder filtros = new StringBuilder();

        if (inicio != null) {
            filtros.append(" AND ").append(colunaData).append(" >= ? ");
            params.add(Date.valueOf(inicio));
        }
        if (fim != null) {
            filtros.append(" AND ").append(colunaData).append(" <= ? ");
            params.add(Date.valueOf(fim));
        }
        int groupBy = sql.indexOf(" GROUP BY ");
        if (groupBy >= 0) sql.insert(groupBy, filtros);
        else sql.append(filtros);
        sql.append(" ORDER BY ").append(colunaData).append(" DESC LIMIT 1000");

        try {
            return jdbcTemplate.queryForList(sql.toString(), params.toArray());
        } catch (Exception e) {
            log.error("Falha ao executar relatório para empresa {}: {}", empresaId, e.getMessage());
            return List.of();
        }
    }

    private LocalDate data(Map<String, Object> filtros, String chave) {
        Object valor = filtros.get(chave);
        if (valor == null) return null;
        try {
            return LocalDate.parse(String.valueOf(valor));
        } catch (DateTimeParseException e) {
            log.warn("Data inválida em {}: {}", chave, valor);
            return null;
        }
    }

    private Double total(List<Map<String, Object>> dados, String campo) {
        return total(dados, campo, null);
    }

    private Double total(List<Map<String, Object>> dados, String campo, String tipoTitulo) {
        double soma = 0;
        for (Map<String, Object> linha : dados) {
            if (tipoTitulo != null) {
                Object tipo = linha.get("tipo");
                if (tipo == null || !tipoTitulo.equals(String.valueOf(tipo))) continue;
            }
            soma += numero(linha.get(campo));
        }
        return soma;
    }

    private double numero(Object valor) {
        if (valor instanceof BigDecimal bd) return bd.doubleValue();
        if (valor instanceof Number n) return n.doubleValue();
        return 0.0;
    }
}
