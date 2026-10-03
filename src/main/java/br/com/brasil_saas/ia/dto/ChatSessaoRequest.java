package br.com.brasil_saas.ia.dto;

public record ChatSessaoRequest(
    String titulo,
    Long usuarioId,
    String modeloIa,
    Double temperatura,
    Integer maxTokens,
    Boolean ativo,
    Boolean favorito
) {}
