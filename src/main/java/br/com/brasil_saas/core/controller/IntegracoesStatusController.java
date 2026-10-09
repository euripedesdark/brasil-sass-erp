package br.com.brasil_saas.core.controller;

import br.com.brasil_saas.financeiro.service.StripeFinanceService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/** Status consolidado de integrações da empresa (Stripe, etc.). */
@RestController
@RequestMapping("/api/core/integracoes-status")
@RequiredArgsConstructor
public class IntegracoesStatusController {

    private final StripeFinanceService stripeFinanceService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> status(@AuthenticationPrincipal AuthenticatedUser u) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("empresaId", u.getEmpresaId());
        try {
            out.put("stripe", stripeFinanceService.statusConfig(u.getEmpresaId()));
        } catch (Exception e) {
            out.put("stripe", Map.of("habilitado", false, "motivo", e.getMessage()));
        }
        return out;
    }
}
