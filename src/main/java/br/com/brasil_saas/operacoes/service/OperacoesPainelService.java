package br.com.brasil_saas.operacoes.service;

import br.com.brasil_saas.agenda.service.AgendaService;
import br.com.brasil_saas.helpdesk.service.HelpdeskService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Painel concentrado de módulos operacionais (não fiscal).
 */
@Service
@RequiredArgsConstructor
public class OperacoesPainelService {

    private final JdbcTemplate jdbc;
    private final HelpdeskService helpdeskService;
    private final AgendaService agendaService;

    public Map<String, Object> resumo(Long empresaId) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("empresaId", empresaId);
        out.put("helpdesk", helpdeskService.resumo(empresaId));
        out.put("agendaAgendados", agendaService.agendados(empresaId));
        out.put("agendaHoje", agendaService.doDia(empresaId, java.time.LocalDate.now()).size());

        List<Map<String, Object>> mods = new ArrayList<>();
        mods.add(mod("CRM", "crm", cnt(empresaId, "bc_crm_lead"), "/crm"));
        mods.add(mod("Projetos", "projetos", cnt(empresaId, "bc_prj_projeto"), "/projetos"));
        mods.add(mod("DMS", "dms", cnt(empresaId, "bc_dms_documento"), "/dms"));
        mods.add(mod("Workflow", "workflow", cnt(empresaId, "bc_wkf_instance"), "/workflow"));
        mods.add(mod("Qualidade", "qualidade", cnt(empresaId, "bc_qual_inspecao"), "/qualidade"));
        mods.add(mod("Ativos", "ativos", cnt(empresaId, "bc_ativo_imobilizado"), "/ativos"));
        mods.add(mod("Helpdesk", "helpdesk", cnt(empresaId, "bc_hdp_chamado"), "/helpdesk"));
        mods.add(mod("Agenda", "agenda", cnt(empresaId, "bc_agd_evento"), "/agenda"));
        out.put("modulos", mods);
        return out;
    }

    private Map<String, Object> mod(String nome, String id, long qtd, String rota) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("nome", nome);
        m.put("registros", qtd);
        m.put("rota", rota);
        m.put("status", qtd > 0 ? "ATIVO" : "VAZIO");
        return m;
    }

    private long cnt(Long empresaId, String table) {
        try {
            Long v = jdbc.queryForObject(
                    "select count(*) from brasil_saas." + table + " where empresa_id=? and deleted_at is null",
                    Long.class, empresaId);
            return v == null ? 0 : v;
        } catch (Exception e) {
            return -1; // tabela ausente / nome diferente
        }
    }
}
