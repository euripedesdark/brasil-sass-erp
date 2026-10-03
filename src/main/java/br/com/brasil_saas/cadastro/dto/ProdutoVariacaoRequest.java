package br.com.brasil_saas.cadastro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProdutoVariacaoRequest(
    @NotBlank @Size(max = 100) String nome,
    @Size(max = 50) String sku,
    @Size(max = 30) String codigoBarras,
    BigDecimal preco,
    Boolean ativo
) {}
