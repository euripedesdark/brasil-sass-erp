package br.com.brasil_saas.rh.controller;
import br.com.brasil_saas.rh.service.EncargosService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;
@RestController @RequestMapping("/api/rh/encargos") @RequiredArgsConstructor
public class EncargosController {
    private final EncargosService svc;
    @GetMapping("/folha/{id}") @PreAuthorize("hasAuthority('rh:funcionario:leitura')")
    public Map<String, Object> calcular(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id, @RequestParam(required = false) BigDecimal aliqInss, @RequestParam(required = false) BigDecimal aliqFgts, @RequestParam(required = false) BigDecimal aliqRat) { return svc.calcular(u.getEmpresaId(), id, aliqInss, aliqFgts, aliqRat); }
}
