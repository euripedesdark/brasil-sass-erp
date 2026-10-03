package br.com.brasil_saas.cadastro.dto;

import br.com.brasil_saas.cadastro.model.ProdutoVariacao;
import java.math.BigDecimal;

public record ProdutoVariacaoResponse(
    Long id, String uuid, String nome, String sku, String codigoBarras, BigDecimal preco, Boolean ativo
) {
    public static ProdutoVariacaoResponse from(ProdutoVariacao v) {
        return new ProdutoVariacaoResponse(v.getId(), v.getUuid() != null ? v.getUuid().toString() : null,
            v.getNome(), v.getSku(), v.getCodigoBarras(), v.getPreco(), v.getAtivo());
    }
}
