package br.com.brasil_saas.bi.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record RelatorioAgendadoRequest(
    Long relatorioId,
    String nome,
    String descricao,
    String frequencia,
    Integer intervaloDias,
    LocalDateTime proximaExecucao,
    Boolean ativo,
    String emailDestinatarios,
    String formato,
    Map<String, Object> parametros
) {}
