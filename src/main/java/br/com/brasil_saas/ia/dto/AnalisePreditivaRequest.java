package br.com.brasil_saas.ia.dto;

import java.math.BigDecimal;

public record AnalisePreditivaRequest(
    String nome,
    String descricao,
    String tipo,
    String entidade,
    Long entidadeId,
    String periodo,
    Integer diasPrevisao,
    BigDecimal valorAtual,
    BigDecimal valorPrevisto,
    BigDecimal valorMinimo,
    BigDecimal valorMaximo,
    Long criadoPor
) {}
