package br.com.brasil_saas.rh.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

// empresaId saiu do record de proposito: ele vinha @NotNull no corpo, o que
// obrigava a tela a mandar a empresa e permitia gravar folha em nome de outra.
// Agora vem do token, em FolhaPagamentoService.criar(empresaId, request).
public record FolhaPagamentoRequest(@NotBlank String competencia,
                                    String status, @Valid List<Item> itens) {
    public record Item(@NotNull Long funcionarioId, @NotBlank String tipo, String descricao,
                       @NotNull BigDecimal valor) { }
}
