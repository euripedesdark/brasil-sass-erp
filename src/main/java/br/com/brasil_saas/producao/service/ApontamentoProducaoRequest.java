package br.com.brasil_saas.producao.service;

import java.math.BigDecimal;

public record ApontamentoProducaoRequest(
    Long producaoId,
    Long itemProducaoId,
    Long funcionarioId,
    BigDecimal horasTrabalhadas,
    BigDecimal quantidadeProduzida,
    BigDecimal quantidadeRefugo,
    String observacoes,
    Long maquinaEquipamentoId,
    String turno
) {}
