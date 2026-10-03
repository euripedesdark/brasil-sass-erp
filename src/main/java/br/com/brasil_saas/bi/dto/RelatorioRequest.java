package br.com.brasil_saas.bi.dto;

import java.util.Map;

public record RelatorioRequest(
    String nome,
    String descricao,
    String tipo,
    String categoria,
    String sqlQuery,
    Map<String, Object> parametros,
    Boolean ativo,
    Boolean agendado,
    String frequencia,
    String emailDestinatarios,
    String formatoExportacao
) {}
