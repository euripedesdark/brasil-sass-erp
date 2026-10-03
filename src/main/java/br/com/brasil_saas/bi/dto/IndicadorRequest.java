package br.com.brasil_saas.bi.dto;

import java.math.BigDecimal;

public record IndicadorRequest(
    String nome,
    String descricao,
    String categoria,
    String formula,
    BigDecimal valorMeta,
    String unidadeMedida,
    String corValorBaixo,
    String corValorMedio,
    String corValorAlto,
    Boolean ativo,
    String frequenciaAtualizacao,
    Boolean visivelDashboard,
    Integer ordemExibicao
) {}
