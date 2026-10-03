package br.com.brasil_saas.ia.dto;

import java.util.List;

public record ChatRequest(
    Long sessionId,
    String message,
    String model,
    Double temperature,
    Integer maxTokens,
    List<ChatMessage> history,
    String systemPrompt
) {
    public record ChatMessage(
        String role, // system, user, assistant
        String content
    ) {}
}
