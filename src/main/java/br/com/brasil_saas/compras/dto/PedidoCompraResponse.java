package br.com.brasil_saas.compras.dto;

import br.com.brasil_saas.compras.model.ItemPedidoCompra;
import br.com.brasil_saas.compras.model.PedidoCompra;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PedidoCompraResponse(Long id, String numero, String status, Long fornecedorId,
        LocalDate dataEmissao, LocalDate dataPrevisaoEntrega, BigDecimal valorTotal,
        Long tituloId, String observacao, String statusAprovacao, List<Item> itens) {
    public static PedidoCompraResponse from(PedidoCompra pedido) {
        return new PedidoCompraResponse(pedido.getId(), pedido.getNumero(), pedido.getStatus(),
                pedido.getFornecedorId(), pedido.getDataEmissao(), pedido.getDataPrevisaoEntrega(),
                pedido.getValorTotal(), pedido.getTituloId(), pedido.getObservacao(),
                pedido.getStatusAprovacao(),
                pedido.getItens().stream().map(Item::from).toList());
    }
    public record Item(Long id, Integer numeroItem, Long produtoId, String descricao,
                       BigDecimal quantidade, String unidade, BigDecimal valorUnitario,
                       BigDecimal valorDesconto, BigDecimal valorTotal) {
        static Item from(ItemPedidoCompra item) {
            return new Item(item.getId(), item.getNumeroItem(), item.getProdutoId(), item.getDescricao(),
                    item.getQuantidade(), item.getUnidade(), item.getValorUnitario(),
                    item.getValorDesconto(), item.getValorTotal());
        }
    }
}
