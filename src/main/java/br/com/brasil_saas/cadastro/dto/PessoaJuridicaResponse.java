package br.com.brasil_saas.cadastro.dto;

import br.com.brasil_saas.cadastro.model.PessoaJuridica;
import java.time.LocalDate;

public record PessoaJuridicaResponse(
    Long id, String cnpj, String inscricaoEstadual, String inscricaoMunicipal,
    LocalDate dataAbertura, String porte, String naturezaJuridica
) {
    public static PessoaJuridicaResponse from(PessoaJuridica j) {
        return new PessoaJuridicaResponse(j.getId(), j.getCnpj(), j.getInscricaoEstadual(),
            j.getInscricaoMunicipal(), j.getDataAbertura(), j.getPorte(), j.getNaturezaJuridica());
    }
}
