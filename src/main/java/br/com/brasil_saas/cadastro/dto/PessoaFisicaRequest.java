package br.com.brasil_saas.cadastro.dto;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record PessoaFisicaRequest(
    @Size(max = 11) String cpf,
    @Size(max = 20) String rg,
    @Size(max = 20) String orgaoExpedidor,
    LocalDate dataNascimento,
    @Size(max = 1) String sexo,
    @Size(max = 20) String estadoCivil
) {}
