package br.com.brasil_saas.cadastro.dto;

import br.com.brasil_saas.cadastro.model.UnidadeMedida;

public record UnidadeMedidaResponse(Long id, String uuid, String sigla, String nome, String tipo) {
    public static UnidadeMedidaResponse from(UnidadeMedida u) {
        return new UnidadeMedidaResponse(u.getId(), u.getUuid() != null ? u.getUuid().toString() : null,
            u.getSigla(), u.getNome(), u.getTipo());
    }
}
