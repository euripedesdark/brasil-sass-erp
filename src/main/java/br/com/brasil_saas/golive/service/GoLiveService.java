package br.com.brasil_saas.golive.service;

import br.com.brasil_saas.core.service.EmpresaStripeService;
import br.com.brasil_saas.fiscal.service.CertificadoDigitalService;
import br.com.brasil_saas.helpdesk.service.HelpdeskService;
import br.com.brasil_saas.metas.service.MetaComercialService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Checklist de go-live operacional (sem reabrir emissão de nota). */
@Service
@RequiredArgsConstructor
public class GoLiveService {

    private final JdbcTemplate jdbc;
    private final EmpresaStripeService stripeService;
    private final CertificadoDigitalService certificadoDigitalService;
    private final HelpdeskService helpdeskService;
    private final MetaComercialService metaComercialService;

    public Map<String, Object> checklist(Long empresaId) {
        List<Map<String, Object>> itens = new ArrayList<>();
        int ok = 0, pend = 0;

        // Empresa dados básicos
        boolean emp = count(empresaId, "bc_core_empresa", true) > 0
                || existsEmpresa(empresaId);
        itens.add(item("empresa", "Cadastro da empresa", emp, "/configurar-empresa"));
        if (emp) ok++; else pend++;

        boolean stripe = false;
        try {
            var s = stripeService.obter(empresaId);
            stripe = s.configurada() && s.habilitada();
        } catch (Exception ignored) {}
        itens.add(item("stripe", "Stripe habilitado", stripe, "/configurar-empresa"));
        if (stripe) ok++; else pend++;

        boolean cert = false;
        try {
            var c = certificadoDigitalService.certificadosSalvos(empresaId);
            cert = c != null && !c.isEmpty();
        } catch (Exception ignored) {}
        itens.add(item("certificado", "Certificado A1", cert, "/configurar-empresa"));
        if (cert) ok++; else pend++;

        boolean clientes = count(empresaId, "bc_cad_cliente") > 0 || count(empresaId, "bc_cad_pessoa") > 0;
        itens.add(item("clientes", "Clientes/pessoas", clientes, "/cadastros"));
        if (clientes) ok++; else pend++;

        boolean produtos = count(empresaId, "bc_cad_produto") > 0;
        itens.add(item("produtos", "Produtos", produtos, "/cadastros"));
        if (produtos) ok++; else pend++;

        boolean crm = count(empresaId, "bc_crm_lead") >= 0; // table exists check
        long leads = count(empresaId, "bc_crm_lead");
        itens.add(item("crm", "CRM (leads)", leads >= 0, "/crm"));
        if (leads >= 0) ok++; else pend++;

        long contratos = count(empresaId, "bc_ven_contrato");
        itens.add(item("contratos", "Contratos de venda", contratos >= 0, "/vendas/contratos"));
        if (contratos >= 0) ok++; else pend++;

        YearMonth ym = YearMonth.now();
        var meta = metaComercialService.resumo(empresaId, ym.getYear(), ym.getMonthValue());
        boolean temMeta = ((Number) meta.get("qtd")).intValue() > 0;
        itens.add(item("metas", "Metas do mês", temMeta, "/vendas/metas"));
        if (temMeta) ok++; else pend++;

        itens.add(item("helpdesk", "Helpdesk disponível", true, "/helpdesk"));
        ok++;

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("empresaId", empresaId);
        out.put("ok", ok);
        out.put("pendente", pend);
        out.put("pronto", pend == 0);
        out.put("itens", itens);
        out.put("helpdesk", helpdeskService.resumo(empresaId));
        out.put("observacao", "Homologação SEFAZ/RFB/eSocial continua fora do checklist de software.");
        return out;
    }

    private Map<String, Object> item(String id, String nome, boolean ok, String rota) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("nome", nome);
        m.put("status", ok ? "OK" : "PENDENTE");
        m.put("rota", rota);
        return m;
    }

    private boolean existsEmpresa(Long id) {
        try {
            Integer n = jdbc.queryForObject("select count(*) from brasil_saas.bc_core_empresa where id=? and deleted_at is null", Integer.class, id);
            return n != null && n > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private long count(Long empresaId, String table) {
        return count(empresaId, table, false);
    }

    private long count(Long empresaId, String table, boolean isEmpresaRoot) {
        try {
            if (isEmpresaRoot) {
                Integer n = jdbc.queryForObject("select count(*) from brasil_saas." + table + " where id=? and deleted_at is null", Integer.class, empresaId);
                return n == null ? 0 : n;
            }
            Long n = jdbc.queryForObject(
                    "select count(*) from brasil_saas." + table + " where empresa_id=? and deleted_at is null",
                    Long.class, empresaId);
            return n == null ? 0 : n;
        } catch (Exception e) {
            return -1;
        }
    }
}
