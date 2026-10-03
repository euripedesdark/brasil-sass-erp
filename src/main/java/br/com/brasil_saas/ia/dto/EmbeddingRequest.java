package br.com.brasil_saas.ia.dto;

public record EmbeddingRequest(
    String entidadeTipo,
    Long entidadeId,
    String texto,
    String modelo,
    float[] embeddingVector,
    Integer dimensoes,
    Integer tokens
) {}
