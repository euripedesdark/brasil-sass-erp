package br.com.brasil_saas.cadastro.service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class ProdutoDtos {

    public record Resumo(Long id, UUID uuid, String codigo, String nome,
                         BigDecimal precoVenda, Boolean ativo) {}

    public record VariacaoRequest(@NotBlank @Size(max = 100) String nome, @Size(max = 50) String sku,
                                  @Size(max = 30) String codigoBarras, BigDecimal preco, Boolean ativo) {}

    public record VariacaoResponse(Long id, String nome, String sku, String codigoBarras,
                                   BigDecimal preco, Boolean ativo) {}

    public record Request(@NotBlank @Size(max = 30) String codigo,
                          @NotBlank @Size(max = 200) String nome,
                          String descricao, Long categoriaId, Long marcaId, Long unidadeMedidaId,
                          @Size(max = 8) String ncm, @Size(max = 4) String cfopPadrao,
                          @Size(max = 30) String codigoBarras,
                          BigDecimal precoCusto, BigDecimal precoVenda,
                          BigDecimal estoqueMinimo, BigDecimal estoqueMaximo, BigDecimal peso,
                          String tipo, Boolean ativo,
                          @Valid List<VariacaoRequest> variacoes) {}

    public record Response(Long id, UUID uuid, String codigo, String nome, String descricao,
                           Long categoriaId, String categoriaNome, Long marcaId, String marcaNome,
                           Long unidadeMedidaId, String unidadeSigla,
                           String ncm, String cfopPadrao, String codigoBarras,
                           BigDecimal precoCusto, BigDecimal precoVenda,
                           BigDecimal estoqueMinimo, BigDecimal estoqueMaximo, BigDecimal peso,
                           String tipo, Boolean ativo, List<VariacaoResponse> variacoes) {}

    private ProdutoDtos() {}
}
