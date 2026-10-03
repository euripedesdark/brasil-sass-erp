package br.com.brasil_saas.cadastro.dto;

import br.com.brasil_saas.cadastro.model.Categoria;

public record CategoriaResponse(Long id, String uuid, String nome, String descricao, Long categoriaPaiId, String tipo) {
    public static CategoriaResponse from(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getUuid() != null ? c.getUuid().toString() : null,
            c.getNome(), c.getDescricao(),
            c.getCategoriaPai() != null ? c.getCategoriaPai().getId() : null, c.getTipo());
    }
}
