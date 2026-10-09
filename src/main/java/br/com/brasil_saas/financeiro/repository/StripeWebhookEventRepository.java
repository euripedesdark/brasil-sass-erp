package br.com.brasil_saas.financeiro.repository;

import br.com.brasil_saas.financeiro.model.StripeWebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StripeWebhookEventRepository extends JpaRepository<StripeWebhookEvent, Long> {
    Optional<StripeWebhookEvent> findByStripeEventId(String stripeEventId);
    List<StripeWebhookEvent> findByEmpresaIdOrderByIdDesc(Long empresaId);
}
