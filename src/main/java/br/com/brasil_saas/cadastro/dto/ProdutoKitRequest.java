package br.com.brasil_saas.cadastro.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ProdutoKitRequest(
    @NotNull Long itemId,
    @NotNull BigDecimal quantidade
) {}
