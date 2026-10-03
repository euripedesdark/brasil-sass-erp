package br.com.brasil_saas.cadastro.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class UnidadeMedidaDtos {

    public record Request(@NotBlank @Size(max = 10) String sigla, @NotBlank @Size(max = 50) String nome,
                          String tipo) {}

    public record Response(Long id, String sigla, String nome, String tipo) {}

    private UnidadeMedidaDtos() {}
}
