package br.com.brasil_saas.core.controller;

import br.com.brasil_saas.core.service.EmpresaStripeService;
import br.com.brasil_saas.fiscal.service.CertificadoDigitalService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Resumo das credenciais da empresa logada (matriz/filial): Stripe + certificado.
 * A configuração detalhada fica em /configurar-empresa.
 */
@RestController
@RequestMapping("/api/core/minha-empresa/credenciais")
@RequiredArgsConstructor
public class EmpresaCredenciaisController {

    private final EmpresaStripeService stripeService;
    private final CertificadoDigitalService certificadoDigitalService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> resumo(@AuthenticationPrincipal AuthenticatedUser u) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("empresaId", u.getEmpresaId());
        try {
            var s = stripeService.obter(u.getEmpresaId());
            out.put("stripe", Map.of(
                    "configurada", s.configurada(),
                    "habilitada", s.habilitada(),
                    "webhookConfigurado", s.webhookConfigurado(),
                    "stripeAccountId", s.stripeAccountId() == null ? "" : s.stripeAccountId()
            ));
        } catch (Exception e) {
            out.put("stripe", Map.of("configurada", false, "erro", e.getMessage()));
        }
        try {
            var certs = certificadoDigitalService.certificadosSalvos(u.getEmpresaId());
            out.put("certificado", Map.of(
                    "quantidade", certs == null ? 0 : certs.size(),
                    "configurado", certs != null && !certs.isEmpty()
            ));
        } catch (Exception e) {
            out.put("certificado", Map.of("configurado", false, "erro", e.getMessage()));
        }
        return out;
    }
}
