package br.com.brasil_saas.cadastro.dto;

import br.com.brasil_saas.cadastro.model.Produto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ProdutoResponse(
    Long id, String uuid, String codigo, String nome, String descricao, String urlProduto, List<String> imagens,
    String categoriaNome, String marcaNome, String unidadeSigla,
    String ncm, String cfopPadrao, String codigoBarras, String cest,
    BigDecimal precoCusto, BigDecimal precoVenda,
    BigDecimal estoqueMinimo, BigDecimal estoqueMaximo, BigDecimal peso,
    String tipo, Boolean ativo,
    LocalDateTime createdAt, LocalDateTime updatedAt,
    List<ProdutoVariacaoResponse> variacoes
) {
    public static ProdutoResponse from(Produto p) {
        return new ProdutoResponse(
            p.getId(), p.getUuid() != null ? p.getUuid().toString() : null,
            p.getCodigo(), p.getNome(), p.getDescricao(), p.getUrlProduto(),
            p.getImagens() != null ? p.getImagens().stream().filter(imagem -> imagem.getDeletedAt() == null).map(imagem -> imagem.getUrl()).toList() : List.of(),
            p.getCategoria() != null ? p.getCategoria().getNome() : null,
            p.getMarca() != null ? p.getMarca().getNome() : null,
            p.getUnidadeMedida() != null ? p.getUnidadeMedida().getSigla() : null,
            p.getNcm(), p.getCfopPadrao(), p.getCodigoBarras(), p.getCest(),
            p.getPrecoCusto(), p.getPrecoVenda(), p.getEstoqueMinimo(), p.getEstoqueMaximo(), p.getPeso(),
            p.getTipo(), p.getAtivo(), p.getCreatedAt(), p.getUpdatedAt(),
            p.getVariacoes() != null ? p.getVariacoes().stream().map(ProdutoVariacaoResponse::from).toList() : List.of()
        );
    }
}
