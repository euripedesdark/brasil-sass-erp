package br.com.brasil_saas.cadastro.service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public final class TransportadoraDtos {

    public record Request(@NotNull Long pessoaId, @Size(max = 30) String codigo,
                          @Size(max = 30) String registroAntt, Boolean ativo) {}

    public record Response(Long id, UUID uuid, String codigo, PessoaDtos.Resumo pessoa,
                           String registroAntt, Boolean ativo) {}

    private TransportadoraDtos() {}
}
