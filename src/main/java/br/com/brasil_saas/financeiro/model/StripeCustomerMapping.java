package br.com.brasil_saas.financeiro.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bc_fin_stripe_customer", schema = "brasil_saas")
@Getter @Setter @NoArgsConstructor
public class StripeCustomerMapping extends TenantEntity {
    @Column(name = "pessoa_id", nullable = false)
    private Long pessoaId;

    @Column(name = "stripe_customer_id", nullable = false, length = 64, unique = true)
    private String stripeCustomerId;
}
