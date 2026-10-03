package br.com.brasil_saas.cadastro.service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public final class ClienteDtos {

    public record Request(@NotNull Long pessoaId, @Size(max = 30) String codigo,
                          BigDecimal limiteCredito, @Size(max = 10) String classificacao,
                          Boolean ativo) {}

    public record Response(Long id, UUID uuid, String codigo, PessoaDtos.Resumo pessoa,
                           BigDecimal limiteCredito, String classificacao, Boolean ativo) {}

    private ClienteDtos() {}
}
