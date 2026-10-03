package br.com.brasil_saas.vendas.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ItemPedidoVendaRequest(
    Integer numeroItem,
    Long produtoId,
    Long servicoId,
    String descricao,
    @NotNull BigDecimal quantidade,
    String unidade,
    @NotNull BigDecimal valorUnitario,
    BigDecimal valorDesconto) {}
