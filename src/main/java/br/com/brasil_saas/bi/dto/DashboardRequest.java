package br.com.brasil_saas.bi.dto;

import java.util.List;

public record DashboardRequest(
    String nome,
    String descricao,
    String tipo,
    String layout,
    String filtros,
    Boolean ativo,
    Boolean publico,
    List<Long> widgetIds
) {}
