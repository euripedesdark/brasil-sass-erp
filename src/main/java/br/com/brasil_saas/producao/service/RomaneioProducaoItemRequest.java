package br.com.brasil_saas.producao.service;

import java.math.BigDecimal;

public record RomaneioProducaoItemRequest(
    Long produtoId,
    String descricao,
    BigDecimal quantidade,
    String unidadeMedida,
    String lote,
    String observacoes
) {}
