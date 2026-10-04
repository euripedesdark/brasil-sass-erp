package br.com.brasil_saas.rh.service;
import br.com.brasil_saas.rh.model.Funcionario;
import br.com.brasil_saas.rh.repository.FuncionarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
@Service @RequiredArgsConstructor
public class RescisaoService {
    private final FuncionarioRepository funcionarios;
    @Transactional(readOnly = true) public Map<String, Object> calcular(Long empresaId, Long funcionarioId, LocalDate desligamento, String motivo) {
        Funcionario f = funcionarios.findById(funcionarioId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Funcionario inexistente"));
        if (f.getEmpresaId() == null || f.getEmpresaId().equals(empresaId) == false) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Funcionario de outra empresa");
        if (f.getDataAdmissao() == null) throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Sem data de admissao");
        if (desligamento == null) desligamento = LocalDate.now();
        if (motivo == null) motivo = "SEM_JUSTA_CAUSA";
        BigDecimal sal = f.getSalario() == null ? BigDecimal.ZERO : f.getSalario();
        long meses = ChronoUnit.MONTHS.between(f.getDataAdmissao().withDayOfMonth(1), desligamento.withDayOfMonth(1));
        if (meses < 0) meses = 0;
        long diasMes = desligamento.getDayOfMonth() - 1;
        if (diasMes < 0) diasMes = 0;
        BigDecimal saldoSalario = sal.multiply(BigDecimal.valueOf(diasMes)).divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);
        BigDecimal decimo = sal.multiply(BigDecimal.valueOf(Math.min(12, meses + 1))).divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        BigDecimal ferias = sal.multiply(BigDecimal.valueOf(Math.min(12, meses + 1))).divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        BigDecimal terco = ferias.divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP);
        BigDecimal aviso = "SEM_JUSTA_CAUSA".equals(motivo) ? sal : BigDecimal.ZERO;
        BigDecimal multa = "SEM_JUSTA_CAUSA".equals(motivo) ? sal.multiply(new BigDecimal("0.4")) : BigDecimal.ZERO;
        BigDecimal total = saldoSalario.add(decimo).add(ferias).add(terco).add(aviso).add(multa);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("funcionarioId", f.getId());
        m.put("motivo", motivo);
        m.put("mesesTrabalhados", meses + 1);
        m.put("saldoSalario", saldoSalario);
        m.put("decimoTerceiro", decimo);
        m.put("ferias", ferias);
        m.put("tercoFerias", terco);
        m.put("avisoPrevio", aviso);
        m.put("multa40", multa);
        m.put("total", total);
        return m;
    }
}
