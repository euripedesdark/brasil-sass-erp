package br.com.brasil_saas.compras.service.impl;

import br.com.brasil_saas.compras.model.ConferenciaFaturaCompraItem;
import br.com.brasil_saas.compras.model.ItemPedidoCompra;
import br.com.brasil_saas.compras.model.RecebimentoCompraItem;
import br.com.brasil_saas.fiscal.model.NfeItem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regras da conferencia 3-way item a item. Sao as regras que decidem se a
 * fatura do fornecedor passa ou vira divergencia, entao ficam travadas aqui.
 */
class ConferenciaFaturaCompraServiceImplTest {

    private static ItemPedidoCompra pedido(Long produtoId, String quantidade, String valorUnitario) {
        ItemPedidoCompra item = new ItemPedidoCompra();
        item.setProdutoId(produtoId);
        item.setNumeroItem(1);
        item.setDescricao("Item de teste");
        item.setQuantidade(new BigDecimal(quantidade));
        item.setValorUnitario(new BigDecimal(valorUnitario));
        item.setValorTotal(BigDecimal.ZERO);
        return item;
    }

    private static RecebimentoCompraItem recebimento(Long produtoId, String quantidade, String valorUnitario) {
        RecebimentoCompraItem item = new RecebimentoCompraItem();
        item.setProdutoId(produtoId);
        item.setQuantidadePedida(BigDecimal.ZERO);
        item.setQuantidadeRecebida(new BigDecimal(quantidade));
        item.setValorUnitario(new BigDecimal(valorUnitario));
        return item;
    }

    private static NfeItem nfe(Long produtoId, String quantidade, String valorUnitario) {
        NfeItem item = new NfeItem();
        item.setProdutoId(produtoId);
        item.setNumeroItem(1);
        item.setQuantidade(new BigDecimal(quantidade));
        item.setValorUnitario(new BigDecimal(valorUnitario));
        item.setValorTotal(new BigDecimal(quantidade).multiply(new BigDecimal(valorUnitario))
                .setScale(2, java.math.RoundingMode.HALF_UP));
        return item;
    }

    @Test
    void aprovaOItemQuandoPedidoRecebimentoEFaturaCasam() {
        List<ConferenciaFaturaCompraItem> linhas = ConferenciaFaturaCompraServiceImpl.compararItens(
                1L,
                List.of(pedido(10L, "10", "5")),
                List.of(recebimento(10L, "10", "5")),
                List.of(nfe(10L, "10", "5")));

        assertEquals(1, linhas.size());
        assertTrue(linhas.get(0).getConforme());
        assertEquals("OK", linhas.get(0).getTipoDivergencia());
        assertEquals(new BigDecimal("50.00"), linhas.get(0).getValorTotalFaturado());
    }

    @Test
    void apontaFaturaAcimaDoRecebido() {
        List<ConferenciaFaturaCompraItem> linhas = ConferenciaFaturaCompraServiceImpl.compararItens(
                1L,
                List.of(pedido(10L, "10", "5")),
                List.of(recebimento(10L, "10", "5")),
                List.of(nfe(10L, "12", "5")));

        assertFalse(linhas.get(0).getConforme());
        assertEquals("QUANTIDADE_FATURADA_MAIOR_QUE_RECEBIDA", linhas.get(0).getTipoDivergencia());
    }

    @Test
    void apontaPrecoDiferenteDoPedido() {
        List<ConferenciaFaturaCompraItem> linhas = ConferenciaFaturaCompraServiceImpl.compararItens(
                1L,
                List.of(pedido(10L, "10", "5")),
                List.of(recebimento(10L, "10", "5")),
                List.of(nfe(10L, "10", "6")));

        assertFalse(linhas.get(0).getConforme());
        assertEquals("PRECO_DIVERGENTE", linhas.get(0).getTipoDivergencia());
    }

    @Test
    void apontaItemRecebidoSemLinhaNaFatura() {
        List<ConferenciaFaturaCompraItem> linhas = ConferenciaFaturaCompraServiceImpl.compararItens(
                1L,
                List.of(pedido(10L, "10", "5")),
                List.of(recebimento(10L, "10", "5")),
                List.of());

        assertFalse(linhas.get(0).getConforme());
        assertEquals("ITEM_NAO_FATURADO", linhas.get(0).getTipoDivergencia());
    }

    @Test
    void apontaItemDaFaturaSemLinhaNoPedido() {
        List<ConferenciaFaturaCompraItem> linhas = ConferenciaFaturaCompraServiceImpl.compararItens(
                1L,
                List.of(pedido(10L, "10", "5")),
                List.of(recebimento(10L, "10", "5")),
                List.of(nfe(10L, "10", "5"), nfe(99L, "1", "7")));

        assertEquals(2, linhas.size());
        assertTrue(linhas.get(0).getConforme());
        assertEquals("ITEM_NAO_PEDIDO", linhas.get(1).getTipoDivergencia());
    }
}
