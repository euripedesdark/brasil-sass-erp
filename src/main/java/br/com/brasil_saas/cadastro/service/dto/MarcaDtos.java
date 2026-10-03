package br.com.brasil_saas.cadastro.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class MarcaDtos {

    public record Request(@NotBlank @Size(max = 100) String nome, @Size(max = 255) String descricao) {}

    public record Response(Long id, String nome, String descricao) {}

    private MarcaDtos() {}
}
