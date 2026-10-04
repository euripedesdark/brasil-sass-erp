package br.com.brasil_saas.producao.service;
import br.com.brasil_saas.producao.model.ApontamentoProducao;
import br.com.brasil_saas.producao.model.CentroTrabalho;
import br.com.brasil_saas.producao.repository.ApontamentoProducaoRepository;
import br.com.brasil_saas.producao.repository.CentroTrabalhoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
@Service @RequiredArgsConstructor
public class OeeService {
    private final ApontamentoProducaoRepository apontamentos;
    private final CentroTrabalhoRepository centros;
    @Transactional(readOnly = true) public List<Map<String, Object>> eficiencia(Long empresaId, LocalDate de, LocalDate ate) {
        if (de == null) de = LocalDate.now().minusDays(30);
        if (ate == null) ate = LocalDate.now();
        BigDecimal capDia = BigDecimal.ZERO;
        for (CentroTrabalho c : centros.findByEmpresaIdAndDeletedAtIsNullOrderByCodigo(empresaId)) {
            if (Boolean.TRUE.equals(c.getAtivo()) == false) continue;
            capDia = capDia.add(c.getCapacidadeHorasDia() == null ? BigDecimal.ZERO : c.getCapacidadeHorasDia());
        }
        Map<LocalDate, BigDecimal[]> porDia = new TreeMap<>();
        for (ApontamentoProducao a : apontamentos.findByEmpresaIdAndDataApontamentoBetween(empresaId, de.atStartOfDay(), ate.plusDays(1).atStartOfDay())) {
            LocalDate dia = a.getDataApontamento() == null ? null : a.getDataApontamento().toLocalDate();
            if (dia == null) continue;
            BigDecimal[] v = porDia.computeIfAbsent(dia, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO});
            v[0] = v[0].add(a.getHorasTrabalhadas() == null ? BigDecimal.ZERO : a.getHorasTrabalhadas());
            v[1] = v[1].add(a.getQuantidadeProduzida() == null ? BigDecimal.ZERO : a.getQuantidadeProduzida());
            v[2] = v[2].add(a.getQuantidadeRefugo() == null ? BigDecimal.ZERO : a.getQuantidadeRefugo());
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (var e : porDia.entrySet()) {
            BigDecimal disp = capDia.signum() <= 0 ? BigDecimal.ZERO : e.getValue()[0].divide(capDia, 4, RoundingMode.HALF_UP).min(BigDecimal.ONE);
            BigDecimal tot = e.getValue()[1].add(e.getValue()[2]);
            BigDecimal qual = tot.signum() <= 0 ? BigDecimal.ONE : e.getValue()[1].divide(tot, 4, RoundingMode.HALF_UP);
            BigDecimal thr = e.getValue()[0].signum() <= 0 ? BigDecimal.ZERO : e.getValue()[1].divide(e.getValue()[0], 3, RoundingMode.HALF_UP);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("dia", e.getKey().toString());
            m.put("horas", e.getValue()[0]);
            m.put("capacidade", capDia);
            m.put("disponibilidade", disp.multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP));
            m.put("produzida", e.getValue()[1]);
            m.put("refugo", e.getValue()[2]);
            m.put("qualidade", qual.multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP));
            m.put("pecasHora", thr);
            out.add(m);
        }
        return out;
    }
}
