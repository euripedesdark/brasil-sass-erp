package br.com.brasil_saas.enterprise.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
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
                "select id, empresa_parceira_id, numero, valor, moeda from brasil_saas.bc_fin_intercompany " +
                "where empresa_id=? and id=? and deleted_at is null", empresaId, id)
                .stream().findFirst().orElseThrow(() -> new NoSuchElementException("Lançamento intercompany não encontrado"));
        Long parceira = ((Number) meu.get("empresa_parceira_id")).longValue();
        List<Map<String, Object>> espelho = jdbc.queryForList(
                "select id, valor, moeda from brasil_saas.bc_fin_intercompany " +
                "where empresa_id=? and empresa_parceira_id=? and numero=? and deleted_at is null",
                parceira, empresaId, meu.get("numero"));
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

    /** Total reconciliado com a parceira no período: base para as eliminações da consolidação. */
    @Transactional(readOnly = true)
    public BigDecimal eliminacoes(Long empresaId, java.time.LocalDate competencia) {
        BigDecimal v = jdbc.queryForObject("select coalesce(sum(valor),0) from brasil_saas.bc_fin_intercompany " +
                "where empresa_id=? and reconciliado=true and deleted_at is null and competencia=?",
                BigDecimal.class, empresaId, java.sql.Date.valueOf(competencia));
        return v == null ? BigDecimal.ZERO : v;
    }
}
