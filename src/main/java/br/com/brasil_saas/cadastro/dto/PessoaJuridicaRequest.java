package br.com.brasil_saas.cadastro.dto;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record PessoaJuridicaRequest(
    @Size(max = 14) String cnpj,
    @Size(max = 30) String inscricaoEstadual,
    @Size(max = 30) String inscricaoMunicipal,
    LocalDate dataAbertura,
    @Size(max = 20) String porte,
    @Size(max = 100) String naturezaJuridica
) {}
