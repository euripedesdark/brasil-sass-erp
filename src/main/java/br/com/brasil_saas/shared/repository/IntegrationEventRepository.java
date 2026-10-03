package br.com.brasil_saas.shared.repository;

import br.com.brasil_saas.shared.model.IntegrationEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Outbox: reserva atomica de PENDING com SKIP LOCKED para (futuros)
 * multiplos relays nao publicarem o mesmo evento duas vezes.
 */
@Repository
public interface IntegrationEventRepository extends JpaRepository<IntegrationEvent, Long> {

    @Query(value = "SELECT * FROM brasil_saas.bc_core_integration_event "
            + "WHERE status = 'PENDING' ORDER BY created_at LIMIT :n "
            + "FOR UPDATE SKIP LOCKED", nativeQuery = true)
    List<IntegrationEvent> claimPending(@Param("n") int n);
}
