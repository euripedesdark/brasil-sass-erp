package br.com.brasil_saas.shared.service;

import br.com.brasil_saas.shared.config.IntegrationAmqpConfig;
import br.com.brasil_saas.shared.model.IntegrationEvent;
import br.com.brasil_saas.shared.repository.IntegrationEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Outbox minimo: grava PENDING na mesma transacao do negocio; o relay
 * publica no RabbitMQ e marca PUBLISHED/FAILED. Se o broker cair, o evento
 * continua salvo e e reprocessado no proximo ciclo.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IntegrationEventPublisher {

    private final IntegrationEventRepository repository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public IntegrationEvent publish(String eventType, String aggregateType,
                                    Long aggregateId, String payload, UUID correlationId) {
        IntegrationEvent e = new IntegrationEvent();
        e.setEventType(eventType);
        e.setSourceSystem("erp");
        e.setAggregateType(aggregateType);
        e.setAggregateId(aggregateId);
        e.setPayload(payload == null ? "{}" : payload);
        e.setStatus(IntegrationEvent.PENDING);
        e.setCorrelationId(correlationId);
        return repository.save(e);
    }

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void relay() {
        List<IntegrationEvent> batch = repository.claimPending(50);
        for (IntegrationEvent e : batch) {
            try {
                Map<String, Object> msg = new LinkedHashMap<>();
                msg.put("event_id", e.getId());
                msg.put("event_type", e.getEventType());
                msg.put("origin", e.getSourceSystem());
                msg.put("aggregate_type", e.getAggregateType());
                msg.put("aggregate_id", e.getAggregateId());
                msg.put("payload", e.getPayload());
                msg.put("correlation_id", e.getCorrelationId());
                rabbitTemplate.convertAndSend(IntegrationAmqpConfig.EXCHANGE,
                        e.getEventType(), msg);
                e.setStatus(IntegrationEvent.PUBLISHED);
                e.setProcessedAt(LocalDateTime.now());
            } catch (Exception ex) {
                log.warn("Outbox: falha ao publicar evento {} ({}), fica para o proximo ciclo: {}",
                        e.getId(), e.getEventType(), ex.getMessage());
                e.setStatus(IntegrationEvent.FAILED);
            }
        }
    }
}
