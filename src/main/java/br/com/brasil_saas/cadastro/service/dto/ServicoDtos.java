package br.com.brasil_saas.cadastro.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public final class ServicoDtos {

    public record Request(@NotBlank @Size(max = 30) String codigo, @NotBlank @Size(max = 200) String nome,
                          String descricao, @Size(max = 10) String lc116Codigo,
                          @Size(max = 10) String codigoTributacaoMunicipal,
                          @Size(max = 12) String nbs,
                          BigDecimal aliquotaIss, BigDecimal preco, Long unidadeMedidaId, Boolean ativo) {}

    public record Response(Long id, UUID uuid, String codigo, String nome, String descricao,
                           String lc116Codigo, String codigoTributacaoMunicipal, String nbs,
                           BigDecimal aliquotaIss, BigDecimal preco,
                           Long unidadeMedidaId, String unidadeSigla, Boolean ativo) {}

    private ServicoDtos() {}
}
