package br.com.brasil_saas.cadastro.dto;

import jakarta.validation.constraints.Size;

public record ContatoRequest(
    @Size(max = 30) String tipo,
    @Size(max = 150) String nome,
    @Size(max = 150) String email,
    @Size(max = 20) String telefone,
    @Size(max = 255) String observacao
) {}
