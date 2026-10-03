package br.com.brasil_saas.ia.dto;

public record ChatMensagemRequest(
    Long sessaoId,
    String conteudo,
    String tipo,
    String modeloIa
) {}
