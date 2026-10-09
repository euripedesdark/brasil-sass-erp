package br.com.brasil_saas.fiscal.service;

import br.com.brasil_saas.fiscal.model.RegraTributaria;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Simula impostos de uma linha (base) com regra resolvida + DIFAL + ST.
 * Não emite documento fiscal — apoio a pedido/orçamento.
 */
@Service
@RequiredArgsConstructor
public class TributacaoSimuladorService {

    private final RegraTributariaService regras;
    private final DifalService difalService;
    private final IcmsStService icmsStService;

    public Map<String, Object> simular(Long empresaId, BigDecimal base, String ncm, String cfop,
                                       String ufOrigem, String ufDestino,
                                       Boolean consumidorFinal, Boolean contribuinte) {
        if (base == null || base.signum() < 0) {
            throw new IllegalArgumentException("Base inválida");
        }
        Optional<RegraTributaria> opt = regras.resolverOpcional(empresaId, ncm, cfop, ufOrigem, ufDestino);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("base", base.setScale(2, RoundingMode.HALF_UP));
        out.put("ncm", ncm);
        out.put("cfop", cfop);
        out.put("ufOrigem", ufOrigem);
        out.put("ufDestino", ufDestino);

        if (opt.isEmpty()) {
            out.put("regraEncontrada", false);
            out.put("mensagem", "Nenhuma regra ativa — informe alíquotas manuais ou cadastre regra");
            return out;
        }
        RegraTributaria r = opt.get();
        out.put("regraEncontrada", true);
        out.put("regraId", r.getId());
        out.put("regraNome", r.getNome());
        out.put("cstIcms", r.getCstIcms());

        BigDecimal aliqIcms = nz(r.getAliquotaIcms());
        BigDecimal icms = pct(base, aliqIcms);
        out.put("aliquotaIcms", aliqIcms);
        out.put("icms", icms);
        out.put("ipi", pct(base, nz(r.getAliquotaIpi())));
        out.put("pis", pct(base, nz(r.getAliquotaPis())));
        out.put("cofins", pct(base, nz(r.getAliquotaCofins())));

        boolean interestadual = ufOrigem != null && ufDestino != null
                && !ufOrigem.equalsIgnoreCase(ufDestino);
        boolean cf = Boolean.TRUE.equals(consumidorFinal);
        boolean naoContrib = contribuinte == null || !contribuinte;

        if (interestadual && cf && naoContrib) {
            BigDecimal inter = aliqIcms;
            BigDecimal interna = r.getAliquotaInterna() != null ? r.getAliquotaInterna() : aliqIcms;
            Map<String, Object> d = difalService.calcular(base, inter, interna, nz(r.getAliquotaFcp()));
            out.put("difal", d);
        }
        if (r.getMva() != null && r.getMva().signum() > 0) {
            BigDecimal inter = aliqIcms;
            BigDecimal interna = r.getAliquotaInterna() != null ? r.getAliquotaInterna()
                    : (r.getAliquotaSt() != null ? r.getAliquotaSt() : aliqIcms);
            Map<String, Object> st = icmsStService.calcular(base, inter, interna, r.getMva(), nz(r.getReducaoBasePct()));
            out.put("icmsSt", st);
        }
        return out;
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static BigDecimal pct(BigDecimal base, BigDecimal aliq) {
        return base.multiply(aliq).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
