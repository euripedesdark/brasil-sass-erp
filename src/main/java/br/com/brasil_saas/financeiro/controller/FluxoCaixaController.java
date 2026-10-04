package br.com.brasil_saas.financeiro.controller;
import br.com.brasil_saas.financeiro.service.FluxoCaixaService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
@RestController @RequestMapping("/api/financeiro/fluxo-caixa") @RequiredArgsConstructor
public class FluxoCaixaController {
    private final FluxoCaixaService svc;
    @GetMapping @PreAuthorize("hasAuthority('financeiro:titulo:leitura')")
    public List<Map<String, Object>> projetar(@AuthenticationPrincipal AuthenticatedUser u, @RequestParam(required = false, defaultValue = "90") int dias) { return svc.projetar(u.getEmpresaId(), dias); }
}
