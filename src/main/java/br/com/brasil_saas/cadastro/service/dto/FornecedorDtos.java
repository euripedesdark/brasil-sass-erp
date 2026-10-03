package br.com.brasil_saas.cadastro.service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public final class FornecedorDtos {

    public record Request(@NotNull Long pessoaId, @Size(max = 30) String codigo,
                          Integer prazoMedioDias, BigDecimal avaliacao, Boolean ativo) {}

    public record Response(Long id, UUID uuid, String codigo, PessoaDtos.Resumo pessoa,
                           Integer prazoMedioDias, BigDecimal avaliacao, Boolean ativo) {}

    private FornecedorDtos() {}
}
