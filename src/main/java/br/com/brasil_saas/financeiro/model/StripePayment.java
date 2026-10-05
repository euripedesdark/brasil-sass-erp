package br.com.brasil_saas.financeiro.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bc_fin_stripe_payment", schema = "brasil_saas")
@Getter @Setter @NoArgsConstructor
public class StripePayment extends TenantEntity {
    @Column(name = "titulo_id", nullable = false)
    private Long tituloId;
    @Column(name = "stripe_customer_id", length = 64)
    private String stripeCustomerId;
    @Column(name = "checkout_session_id", length = 128, unique = true)
    private String checkoutSessionId;
    @Column(name = "payment_intent_id", length = 128)
    private String paymentIntentId;
    @Column(name = "invoice_id", length = 128)
    private String invoiceId;
    @Column(name = "amount", nullable = false)
    private Long amount;
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;
    @Column(name = "status", nullable = false, length = 40)
    private String status;
    @Column(name = "checkout_url", columnDefinition = "text")
    private String checkoutUrl;
}
