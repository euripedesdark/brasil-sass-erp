package br.com.brasil_saas.cadastro.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record PessoaRequest(
    @NotBlank @Size(max = 10) String tipo,
    @NotBlank @Size(max = 200) String nome,
    @Size(max = 14) String documento,
    @Size(max = 150) String email,
    @Size(max = 20) String telefone,
    @Size(max = 20) String status,
    String observacao,
    @Valid PessoaFisicaRequest fisica,
    @Valid PessoaJuridicaRequest juridica,
    @Valid List<EnderecoRequest> enderecos,
    @Valid List<ContatoRequest> contatos
) {}
