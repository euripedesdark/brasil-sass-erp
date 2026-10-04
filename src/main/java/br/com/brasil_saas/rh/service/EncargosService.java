package br.com.brasil_saas.rh.service;
import br.com.brasil_saas.rh.model.FolhaPagamento;
import br.com.brasil_saas.rh.repository.FolhaPagamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
@Service @RequiredArgsConstructor
public class EncargosService {
    private final FolhaPagamentoRepository folhas;
    @Transactional(readOnly = true) public Map<String, Object> calcular(Long empresaId, Long folhaId, BigDecimal aliqInss, BigDecimal aliqFgts, BigDecimal aliqRat) {
        FolhaPagamento f = folhas.findById(folhaId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Folha inexistente"));
        if (f.getEmpresaId() == null || f.getEmpresaId().equals(empresaId) == false) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Folha de outra empresa");
        BigDecimal base = BigDecimal.ZERO;
        if (f.getItens() != null) for (var it : f.getItens())
            if ("PROVENTO".equals(it.getTipo())) base = base.add(it.getValor() == null ? BigDecimal.ZERO : it.getValor());
        BigDecimal inss = pct(base, aliqInss == null ? new BigDecimal("20") : aliqInss);
        BigDecimal fgts = pct(base, aliqFgts == null ? new BigDecimal("8") : aliqFgts);
        BigDecimal rat = pct(base, aliqRat == null ? new BigDecimal("2") : aliqRat);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("folhaId", f.getId());
        m.put("competencia", f.getCompetencia());
        m.put("base", base);
        m.put("inssPatronal", inss);
        m.put("fgts", fgts);
        m.put("rat", rat);
        m.put("total", inss.add(fgts).add(rat));
        return m;
    }
    private BigDecimal pct(BigDecimal base, BigDecimal aliq) {
        if (aliq == null || aliq.signum() <= 0) return BigDecimal.ZERO;
        return base.multiply(aliq).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
