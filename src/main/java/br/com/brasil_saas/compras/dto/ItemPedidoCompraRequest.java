package br.com.brasil_saas.compras.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ItemPedidoCompraRequest(
    Integer numeroItem,
    Long produtoId,
    String descricao,
    @NotNull BigDecimal quantidade,
    String unidade,
    @NotNull BigDecimal valorUnitario,
    BigDecimal valorDesconto) {}
