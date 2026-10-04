package br.com.brasil_saas.rh.controller;
import br.com.brasil_saas.rh.service.RescisaoService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.Map;
@RestController @RequestMapping("/api/rh/rescisao") @RequiredArgsConstructor
public class RescisaoController {
    private final RescisaoService svc;
    @GetMapping("/calcular") @PreAuthorize("hasAuthority('rh:funcionario:leitura')")
    public Map<String, Object> calcular(@AuthenticationPrincipal AuthenticatedUser u, @RequestParam Long funcionarioId, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desligamento, @RequestParam(required = false) String motivo) { return svc.calcular(u.getEmpresaId(), funcionarioId, desligamento, motivo); }
}
