package br.com.brasil_saas.ia.dto;

public record PromptRequest(
    String nome,
    String descricao,
    String conteudo,
    String categoria,
    String tipo,
    Double temperatura,
    Integer maxTokens,
    Boolean ativo,
    Boolean publico,
    Boolean favorito,
    Long criadoPor
) {}
