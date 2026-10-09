package br.com.brasil_saas.compras.dto;

import jakarta.validation.constraints.NotEmpty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/** Liberação (release order): quantidades por id do item do contrato. */
public record ContratoLiberacaoRequest(@NotEmpty Map<Long, BigDecimal> quantidades,
                                       LocalDate dataPrevisaoEntrega, String observacao) {}
