package br.com.brasil_saas.cadastro.dto;

import br.com.brasil_saas.cadastro.model.PessoaFisica;
import java.time.LocalDate;

public record PessoaFisicaResponse(
    Long id, String cpf, String rg, String orgaoExpedidor,
    LocalDate dataNascimento, String sexo, String estadoCivil
) {
    public static PessoaFisicaResponse from(PessoaFisica f) {
        return new PessoaFisicaResponse(f.getId(), f.getCpf(), f.getRg(), f.getOrgaoExpedidor(),
            f.getDataNascimento(), f.getSexo(), f.getEstadoCivil());
    }
}
