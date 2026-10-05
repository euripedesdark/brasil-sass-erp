package br.com.brasil_saas.financeiro.service;

import br.com.brasil_saas.financeiro.dto.StripeDtos.CheckoutResponse;
import br.com.brasil_saas.financeiro.dto.StripeDtos.InvoiceResponse;

public interface StripeFinanceService {
    CheckoutResponse criarCheckout(Long empresaId, Long tituloId);
    InvoiceResponse criarInvoice(Long empresaId, Long tituloId);
    void processarWebhook(String payload, String signature);
}
