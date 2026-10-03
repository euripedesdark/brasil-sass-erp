package br.com.brasil_saas.ia.dto;

public record AIConfigRequest(
    String apiKey,
    String apiEndpoint,
    String defaultModel,
    Double temperature,
    Integer maxTokens,
    Integer timeoutSeconds,
    Boolean isEnabled,
    Long maxDailyTokens
) {}
