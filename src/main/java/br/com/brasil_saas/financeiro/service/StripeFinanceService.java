package br.com.brasil_saas.financeiro.service;

import br.com.brasil_saas.financeiro.dto.StripeDtos.CheckoutResponse;
import br.com.brasil_saas.financeiro.dto.StripeDtos.InvoiceResponse;
import br.com.brasil_saas.financeiro.model.StripePayment;
import br.com.brasil_saas.financeiro.model.StripeWebhookEvent;

import java.util.List;
import java.util.Map;

public interface StripeFinanceService {
    CheckoutResponse criarCheckout(Long empresaId, Long tituloId);
    InvoiceResponse criarInvoice(Long empresaId, Long tituloId);
    void processarWebhook(Long empresaId, String payload, String signature);
    List<StripePayment> listarPagamentos(Long empresaId);
    List<StripePayment> listarPorTitulo(Long empresaId, Long tituloId);
    Map<String, Object> statusConfig(Long empresaId);
    List<StripeWebhookEvent> listarWebhooks(Long empresaId);
}
