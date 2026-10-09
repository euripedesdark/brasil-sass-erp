package br.com.brasil_saas.enterprise.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Reconcilia o lançamento da empresa com o lançamento espelho da parceira (mesmo número, partes invertidas).
 * Só grava na linha da própria empresa; da parceira apenas lê valor e moeda.
 */
@Service
@RequiredArgsConstructor
public class IntercompanyService {
    private static final BigDecimal TOLERANCIA = new BigDecimal("0.01");
    private final JdbcTemplate jdbc;

    @Transactional
    public Map<String, Object> reconciliar(Long empresaId, Long userId, Long id) {
        Map<String, Object> meu = jdbc.queryForList(
                "select id, empresa_parceira_id, numero, valor, moeda, competencia from brasil_saas.bc_fin_intercompany " +
                "where empresa_id=? and id=? and deleted_at is null", empresaId, id)
                .stream().findFirst().orElseThrow(() -> new NoSuchElementException("Lançamento intercompany não encontrado"));
        Long parceira = ((Number) meu.get("empresa_parceira_id")).longValue();
        List<Map<String, Object>> espelho = jdbc.queryForList(
                "select id, valor, moeda from brasil_saas.bc_fin_intercompany " +
                "where empresa_id=? and empresa_parceira_id=? and numero=? and deleted_at is null " +
                "and competencia is not distinct from ?",
                parceira, empresaId, meu.get("numero"), meu.get("competencia"));
        Map<String, Object> par = espelho.isEmpty() ? null : espelho.get(0);

        var r = IntercompanyConciliacao.comparar((BigDecimal) meu.get("valor"), String.valueOf(meu.get("moeda")),
                par == null ? null : (BigDecimal) par.get("valor"), par == null ? null : String.valueOf(par.get("moeda")),
                TOLERANCIA);
        jdbc.update("update brasil_saas.bc_fin_intercompany set status=?, reconciliado=?, diferenca=?, contrapartida_id=?, " +
                        "reconciliado_em=case when ? then now() else null end, updated_at=now(), updated_by=? where empresa_id=? and id=?",
                r.status(), r.reconciliado(), r.diferenca(), par == null ? null : par.get("id"),
                r.reconciliado(), userId, empresaId, id);
        return jdbc.queryForMap("select * from brasil_saas.bc_fin_intercompany where empresa_id=? and id=?", empresaId, id);
    }

    /** Linhas reconciliadas do mês; totais segregados por moeda, sem conversão implícita. */
    @Transactional(readOnly = true)
    public Map<String, Object> eliminacoes(Long empresaId, java.time.LocalDate competencia) {
        java.time.LocalDate inicio = competencia.withDayOfMonth(1);
        List<Map<String, Object>> linhas = jdbc.queryForList(
                "select id, numero, empresa_parceira_id, valor, moeda, status, reconciliado, diferenca, competencia " +
                "from brasil_saas.bc_fin_intercompany " +
                "where empresa_id=? and reconciliado=true and deleted_at is null and competencia>=? and competencia<? " +
                "order by numero", empresaId, java.sql.Date.valueOf(inicio), java.sql.Date.valueOf(inicio.plusMonths(1)));
        Map<String, BigDecimal> totaisPorMoeda = new LinkedHashMap<>();
        for (Map<String, Object> linha : linhas) {
            BigDecimal valor = (BigDecimal) linha.get("valor");
            String moeda = String.valueOf(linha.get("moeda")).trim();
            totaisPorMoeda.merge(moeda, valor == null ? BigDecimal.ZERO : valor, BigDecimal::add);
        }
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("linhas", linhas);
        resultado.put("totaisPorMoeda", totaisPorMoeda);
        // Mantém o contrato antigo somente quando há uma unidade monetária única.
        resultado.put("total", totaisPorMoeda.size() > 1 ? null :
                totaisPorMoeda.values().stream().findFirst().orElse(BigDecimal.ZERO));
        resultado.put("competencia", inicio.toString());
        return resultado;
    }
}
