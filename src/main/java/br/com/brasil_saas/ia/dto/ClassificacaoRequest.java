package br.com.brasil_saas.ia.dto;

public record ClassificacaoRequest(
    String nome,
    String descricao,
    String tipo,
    String categoria,
    String tags,
    Long entidadeId,
    Double confianca,
    Boolean classificacaoIa,
    String classificacaoManual,
    Long criadoPor
) {}
