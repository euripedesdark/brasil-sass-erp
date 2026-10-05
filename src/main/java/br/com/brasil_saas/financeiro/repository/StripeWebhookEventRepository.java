package br.com.brasil_saas.financeiro.repository;

import br.com.brasil_saas.financeiro.model.StripeWebhookEvent;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StripeWebhookEventRepository extends JpaRepository<StripeWebhookEvent, Long> {
    Optional<StripeWebhookEvent> findByStripeEventId(String stripeEventId);
}
