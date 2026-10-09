package br.com.brasil_saas.fiscal.service;

import br.com.brasil_saas.financeiro.service.BoletoService;
import br.com.brasil_saas.financeiro.service.StripeFinanceService;
import br.com.brasil_saas.fiscal.repository.ApuracaoRepository;
import br.com.brasil_saas.fiscal.repository.ReinfRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Checklist operacional: o que falta para o fiscal/cobrança funcionar de ponta a ponta.
 * Não substitui homologação SEFAZ/RFB — só o que o ERP consegue verificar.
 */
@Service
@RequiredArgsConstructor
public class FiscalProntidaoService {

    private final CertificadoDigitalService certificadoDigitalService;
    private final RegraTributariaService regraTributariaService;
    private final ReinfRepository reinfRepository;
    private final ApuracaoRepository apuracaoRepository;
    private final StripeFinanceService stripeFinanceService;
    private final BoletoService boletoService;

    public Map<String, Object> checklist(Long empresaId) {
        List<Map<String, Object>> itens = new ArrayList<>();
        int ok = 0, warn = 0, fail = 0;

        // Certificado
        try {
            var certs = certificadoDigitalService.certificadosSalvos(empresaId);
            boolean has = certs != null && !certs.isEmpty();
            itens.add(item("certificado", "Certificado digital A1", has ? "OK" : "PENDENTE",
                    has ? certs.size() + " arquivo(s)" : "Envie em Fiscal → Certificado digital"));
            if (has) ok++; else fail++;
        } catch (Exception e) {
            itens.add(item("certificado", "Certificado digital A1", "ERRO", e.getMessage()));
            fail++;
        }

        // Regras
        try {
            long regras = regraTributariaService.listar(empresaId).stream()
                    .filter(r -> Boolean.TRUE.equals(r.getAtiva())).count();
            boolean has = regras > 0;
            itens.add(item("regras", "Regras tributárias ativas", has ? "OK" : "PENDENTE",
                    has ? regras + " regra(s)" : "Cadastre NCM/CFOP em Fiscal → Regras"));
            if (has) ok++; else warn++;
        } catch (Exception e) {
            itens.add(item("regras", "Regras tributárias", "ERRO", e.getMessage()));
            fail++;
        }

        // REINF eventos recentes
        try {
            var reinf = reinfRepository.findByEmpresaIdOrderByCompetenciaDescGeradoAtDesc(empresaId);
            itens.add(item("reinf", "EFD-Reinf (eventos gerados)", reinf.isEmpty() ? "INFO" : "OK",
                    reinf.isEmpty() ? "Nenhum evento ainda" : reinf.size() + " evento(s) no histórico"));
            if (!reinf.isEmpty()) ok++; else warn++;
        } catch (Exception e) {
            itens.add(item("reinf", "EFD-Reinf", "ERRO", e.getMessage()));
            fail++;
        }

        // Apurações
        try {
            // listar sem competencia - use empty check via repository if exists
            var aps = apuracaoRepository.findByEmpresaIdOrderByCompetenciaDesc(empresaId);
            long enc = aps.stream().filter(a -> "ENCERRADA".equalsIgnoreCase(a.getStatus())).count();
            itens.add(item("apuracao", "Apurações PIS/COFINS/ICMS", aps.isEmpty() ? "INFO" : "OK",
                    aps.isEmpty() ? "Nenhuma apuração" : aps.size() + " total, " + enc + " encerrada(s)"));
            if (!aps.isEmpty()) ok++; else warn++;
        } catch (Exception e) {
            itens.add(item("apuracao", "Apurações", "INFO", "Consulta indisponível: " + e.getMessage()));
            warn++;
        }

        // Stripe
        try {
            Map<String, Object> st = stripeFinanceService.statusConfig(empresaId);
            boolean hab = Boolean.TRUE.equals(st.get("habilitado"));
            itens.add(item("stripe", "Stripe cobrança", hab ? "OK" : "PENDENTE",
                    hab ? String.valueOf(st.get("chaveMascarada")) : String.valueOf(st.getOrDefault("motivo", "Configure em Empresa"))));
            if (hab) ok++; else warn++;
        } catch (Exception e) {
            itens.add(item("stripe", "Stripe", "PENDENTE", e.getMessage()));
            warn++;
        }

        // CNAB / boleto
        try {
            boolean cnab = boletoService.servicoDisponivel();
            itens.add(item("cnab", "Microsserviço boleto/CNAB", cnab ? "OK" : "PENDENTE",
                    cnab ? boletoService.urlServico() : "Serviço CNAB indisponível"));
            if (cnab) ok++; else warn++;
        } catch (Exception e) {
            itens.add(item("cnab", "Boleto/CNAB", "PENDENTE", e.getMessage()));
            warn++;
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("empresaId", empresaId);
        out.put("ok", ok);
        out.put("warn", warn);
        out.put("fail", fail);
        out.put("prontoOperacional", fail == 0 && ok >= 2);
        out.put("itens", itens);
        out.put("observacao", "Homologação SEFAZ/RFB e chaves de produção continuam manuais.");
        return out;
    }

    private static Map<String, Object> item(String id, String nome, String status, String detalhe) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("nome", nome);
        m.put("status", status);
        m.put("detalhe", detalhe);
        return m;
    }
}
