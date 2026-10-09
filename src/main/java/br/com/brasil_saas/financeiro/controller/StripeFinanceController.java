package br.com.brasil_saas.financeiro.controller;

import br.com.brasil_saas.financeiro.dto.StripeDtos.CheckoutResponse;
import br.com.brasil_saas.financeiro.dto.StripeDtos.InvoiceResponse;
import br.com.brasil_saas.financeiro.dto.StripeDtos.WebhookResponse;
import br.com.brasil_saas.financeiro.model.StripePayment;
import br.com.brasil_saas.financeiro.model.StripeWebhookEvent;
import br.com.brasil_saas.financeiro.service.StripeFinanceService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @GetMapping("/pagamentos")
    @PreAuthorize("hasAuthority('financeiro:titulo:leitura') or hasAuthority('financeiro:titulo:escrita')")
    public List<StripePayment> pagamentos(@AuthenticationPrincipal AuthenticatedUser user) {
        return service.listarPagamentos(user.getEmpresaId());
    }

    @GetMapping("/titulos/{tituloId}/pagamentos")
    @PreAuthorize("hasAuthority('financeiro:titulo:leitura') or hasAuthority('financeiro:titulo:escrita')")
    public List<StripePayment> pagamentosTitulo(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long tituloId) {
        return service.listarPorTitulo(user.getEmpresaId(), tituloId);
    }

    @GetMapping("/status")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> status(@AuthenticationPrincipal AuthenticatedUser user) {
        return service.statusConfig(user.getEmpresaId());
    }

    @GetMapping("/webhooks")
    @PreAuthorize("hasAuthority('financeiro:titulo:leitura') or isAuthenticated()")
    public List<StripeWebhookEvent> webhooks(@AuthenticationPrincipal AuthenticatedUser user) {
        return service.listarWebhooks(user.getEmpresaId());
    }

    @PostMapping("/webhook/{empresaId}")
    public ResponseEntity<WebhookResponse> webhook(
        @PathVariable Long empresaId,
        @RequestHeader(value = "Stripe-Signature", required = false) String signature,
        @RequestBody String payload) {
        service.processarWebhook(empresaId, payload, signature);
        return ResponseEntity.ok(new WebhookResponse("received"));
    }
}
