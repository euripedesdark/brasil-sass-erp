package br.com.brasil_saas.cadastro.dto;

import br.com.brasil_saas.cadastro.model.Marca;

public record MarcaResponse(Long id, String uuid, String nome, String descricao) {
    public static MarcaResponse from(Marca m) {
        return new MarcaResponse(m.getId(), m.getUuid() != null ? m.getUuid().toString() : null,
            m.getNome(), m.getDescricao());
    }
}
