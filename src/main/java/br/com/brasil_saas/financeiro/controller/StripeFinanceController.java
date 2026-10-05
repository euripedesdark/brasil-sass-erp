package br.com.brasil_saas.financeiro.controller;

import br.com.brasil_saas.financeiro.dto.StripeDtos.CheckoutResponse;
import br.com.brasil_saas.financeiro.dto.StripeDtos.InvoiceResponse;
import br.com.brasil_saas.financeiro.dto.StripeDtos.WebhookResponse;
import br.com.brasil_saas.financeiro.service.StripeFinanceService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/financeiro/stripe")
@RequiredArgsConstructor
public class StripeFinanceController {
    private final StripeFinanceService service;

    @PostMapping("/titulos/{tituloId}/checkout")
    @PreAuthorize("hasAuthority('financeiro:titulo:escrita')")
    public CheckoutResponse checkout(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long tituloId) {
        return service.criarCheckout(user.getEmpresaId(), tituloId);
    }

    @PostMapping("/titulos/{tituloId}/invoice")
    @PreAuthorize("hasAuthority('financeiro:titulo:escrita')")
    public InvoiceResponse invoice(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long tituloId) {
        return service.criarInvoice(user.getEmpresaId(), tituloId);
    }

    @PostMapping("/webhook")
    public ResponseEntity<WebhookResponse> webhook(
        @RequestHeader(value = "Stripe-Signature", required = false) String signature,
        @RequestBody String payload) {
        service.processarWebhook(payload, signature);
        return ResponseEntity.ok(new WebhookResponse("received"));
    }
}
