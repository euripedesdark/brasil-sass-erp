package br.com.brasil_saas.financeiro.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bc_fin_stripe_webhook_event", schema = "brasil_saas")
@Getter @Setter @NoArgsConstructor
public class StripeWebhookEvent extends TenantEntity {
    @Column(name = "stripe_event_id", nullable = false, unique = true, length = 128)
    private String stripeEventId;
    @Column(name = "event_type", nullable = false, length = 128)
    private String eventType;
}
