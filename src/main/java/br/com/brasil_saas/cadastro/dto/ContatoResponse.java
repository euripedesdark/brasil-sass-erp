package br.com.brasil_saas.cadastro.dto;

import br.com.brasil_saas.cadastro.model.Contato;

public record ContatoResponse(
    Long id, String tipo, String nome, String email, String telefone, String observacao
) {
    public static ContatoResponse from(Contato c) {
        return new ContatoResponse(c.getId(), c.getTipo(), c.getNome(), c.getEmail(),
            c.getTelefone(), c.getObservacao());
    }
}
