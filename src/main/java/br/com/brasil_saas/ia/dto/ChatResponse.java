package br.com.brasil_saas.ia.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ChatResponse(
    Long sessionId,
    String message,
    String model,
    Long tokenCount,
    Long responseTimeMs,
    LocalDateTime timestamp,
    List<ChatMessage> history,
    Boolean success,
    String errorMessage
) {
    public record ChatMessage(
        String role,
        String content,
        Long tokenCount
    ) {}
}
