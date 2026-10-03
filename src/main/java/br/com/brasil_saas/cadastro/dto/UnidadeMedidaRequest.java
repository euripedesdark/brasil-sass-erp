package br.com.brasil_saas.cadastro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UnidadeMedidaRequest(
    @NotBlank @Size(max = 10) String sigla,
    @NotBlank @Size(max = 50) String nome,
    @Size(max = 20) String tipo
) {}
