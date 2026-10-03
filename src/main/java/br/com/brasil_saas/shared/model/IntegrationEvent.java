package br.com.brasil_saas.shared.model;

import br.com.brasil_saas.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Fundacao do Hub de Integracao (outbox). Um registro por evento de dominio
 * produzido pelo ERP. O relay publica no RabbitMQ e marca PUBLISHED/FAILED.
 * Propositadamente sem inbox: ainda nao ha volume para isso.
 */
@Entity
@Table(name = "bc_core_integration_event", schema = "brasil_saas")
@Getter
@Setter
public class IntegrationEvent extends BaseEntity {

    public static final String PENDING = "PENDING";
    public static final String PUBLISHED = "PUBLISHED";
    public static final String FAILED = "FAILED";
    public static final String CONSUMED = "CONSUMED";

    @Column(name = "event_type", length = 80, nullable = false)
    private String eventType;

    @Column(name = "source_system", length = 40, nullable = false)
    private String sourceSystem = "erp";

    @Column(name = "aggregate_type", length = 80)
    private String aggregateType;

    @Column(name = "aggregate_id")
    private Long aggregateId;

    @Column(name = "payload", columnDefinition = "text", nullable = false)
    private String payload = "{}";

    @Column(name = "status", length = 16, nullable = false)
    private String status = PENDING;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "correlation_id")
    private UUID correlationId;
}
