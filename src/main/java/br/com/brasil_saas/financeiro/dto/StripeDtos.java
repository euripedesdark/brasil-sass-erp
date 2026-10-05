package br.com.brasil_saas.financeiro.dto;

public final class StripeDtos {
    public record CheckoutResponse(String sessionId, String url, String status) {}
    public record InvoiceResponse(String invoiceId, String hostedInvoiceUrl, String status) {}
    public record WebhookResponse(String status) {}
    private StripeDtos() {}
}
