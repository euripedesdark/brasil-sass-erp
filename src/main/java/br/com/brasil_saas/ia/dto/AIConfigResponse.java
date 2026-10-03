package br.com.brasil_saas.ia.dto;

public record AIConfigResponse(
    Long id,
    String apiKey,
    String apiEndpoint,
    String defaultModel,
    Double temperature,
    Integer maxTokens,
    Integer timeoutSeconds,
    Boolean isEnabled,
    Long maxDailyTokens,
    Long dailyTokenUsage,
    String lastResetDate
) {}
