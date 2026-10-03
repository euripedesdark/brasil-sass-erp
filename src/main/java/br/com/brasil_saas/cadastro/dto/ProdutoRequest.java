package br.com.brasil_saas.cadastro.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public record ProdutoRequest(
    @NotBlank @Size(max = 30) String codigo,
    @NotBlank @Size(max = 200) String nome,
    String descricao,
    @Size(max = 500) String urlProduto,
    Long categoriaId,
    Long marcaId,
    Long unidadeMedidaId,
    @Size(max = 8) String ncm,
    @Size(max = 4) String cfopPadrao,
    // O CEST segue o produto e nao a nota: se o item veio com CEST 1300201, e o
    // mesmo nas proximas 500 compras. Guardar so em bc_fis_nfe_item obrigaria
    // a redigitar no cadastro toda vez.
    @Size(max = 7) String cest,
    @Size(max = 30) String codigoBarras,
    BigDecimal precoCusto,
    BigDecimal precoVenda,
    BigDecimal estoqueMinimo,
    BigDecimal estoqueMaximo,
    BigDecimal peso,
    @Size(max = 20) String tipo,
    Boolean ativo,
    @Valid List<ProdutoVariacaoRequest> variacoes,
    @Valid List<ProdutoKitRequest> itensKit) {

    /**
     * Copia com a empresa do token: aceitar empresaId no corpo permitiria criar
     * produto em nome de outra empresa.
     */
    public ProdutoRequest comEmpresaDa(Long empresaId) {
        return new ProdutoRequest(codigo, nome, descricao, urlProduto, categoriaId,
                marcaId, unidadeMedidaId, ncm, cfopPadrao, cest, codigoBarras, precoCusto,
                precoVenda, estoqueMinimo, estoqueMaximo, peso, tipo, ativo,
                variacoes, itensKit);
    }
}
