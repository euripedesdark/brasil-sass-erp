package br.com.brasil_saas.cadastro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
    @NotBlank @Size(max = 100) String nome,
    @Size(max = 255) String descricao,
    Long categoriaPaiId,
    @Size(max = 20) String tipo
) {}
