package br.com.brasil_saas.financeiro.controller;
import br.com.brasil_saas.financeiro.service.CreditoService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController @RequestMapping("/api/financeiro/credito") @RequiredArgsConstructor
public class CreditoController {
    private final CreditoService svc;
    @GetMapping("/analise") @PreAuthorize("hasAuthority('financeiro:titulo:leitura')")
    public Map<String, Object> analisar(@AuthenticationPrincipal AuthenticatedUser u, @RequestParam Long clienteId) { return svc.analisar(u.getEmpresaId(), clienteId); }
}
