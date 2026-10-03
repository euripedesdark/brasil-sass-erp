package br.com.brasil_saas.vendas.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PedidoVendaRequest(
    @NotNull Long empresaId,
    @NotNull Long clienteId,
    Long vendedorId,
    String tipo,
    String status,
    LocalDate dataEmissao,
    LocalDate dataEntrega,
    Long condicaoPagamentoId,
    Long tabelaPrecoId,
    BigDecimal percentualDesconto,
    String canalVenda,
    String origem,
    String observacao,
    BigDecimal valorDesconto,
    BigDecimal valorFrete,
    @Valid List<ItemPedidoVendaRequest> itens) {

    /**
     * Devolve uma copia com a empresa do token.
     *
     * O controller aceita o {@code empresaId} do corpo porque o contrato ja
     * existed assim, mas sobrescreve com o do token: sem isso, dava para criar
     * pedido em nome de outra empresa so mandando o id no JSON.
     *
     * Por que {@link #empresaId} continua no record: o service usa
     * {@code request.empresaId()} em varios pontos e o record e imutavel, entao
     * trocar o tipo agora mexeria em mais arquivos do que o necessario.
     */
    public PedidoVendaRequest comEmpresaDa(Long empresaIdDoToken) {
        if (empresaIdDoToken == null) {
            throw new IllegalArgumentException("Usuario sem empresa definida no token");
        }
        return new PedidoVendaRequest(
                empresaIdDoToken, clienteId, vendedorId, tipo, status, dataEmissao,
                dataEntrega, condicaoPagamentoId, tabelaPrecoId, percentualDesconto,
                canalVenda, origem, observacao, valorDesconto, valorFrete, itens);
    }
}