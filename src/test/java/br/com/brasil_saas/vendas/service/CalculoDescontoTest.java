package br.com.brasil_saas.vendas.service;

import br.com.brasil_saas.shared.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A conta de desconto do pedido de venda.
 *
 * <p>O caso do come��o e' o que quebrava em producao: 2 x 27,90 com 5,58 de
 * desconto no item E no pedido devolvia 44,64 no pedido e 50,22 no item. O
 * 5,58 era descontado duas vezes. Aqui o resultado tem que ser 50,22 nos dois
 * lugares, com 5,58 de desconto total.
 */
class CalculoDescontoTest {

    private static final BigDecimal Z = BigDecimal.ZERO;

    private static BigDecimal b(String v) {
        return new BigDecimal(v);
    }

    @Test
    @DisplayName("o exemplo do enunciado: 5,58 de desconto so no item, total 50,22")
    void exemploDoEnunciado() {
        CalculoDesconto c = CalculoDesconto.de(
                List.of(new CalculoDesconto.Item(b("2"), b("27.90"), b("5.58"))),
                null, null, null);

        assertEquals(0, b("55.80").compareTo(c.getValorProdutos()));
        assertEquals(0, b("5.58").compareTo(c.getValorDescontoItens()));
        assertEquals(0, b("0.00").compareTo(c.getValorDescontoPedido()));
        assertEquals(0, b("5.58").compareTo(c.getValorDescontoTotal()));
        assertEquals(0, b("50.22").compareTo(c.getValorTotal()));
    }

    @Test
    @DisplayName("o mesmo 5,58 em item E pedido conta duas vezes, e nao e' desconto duplo")
    void mesmoDescontoNoItemENoPedido() {
        // Antes da correcao, o service subtraia o desconto do pedido de um
        // subtotal que ja tinha o do item deduzido. Com 5,58 nos dois, o
        // resultado era 44,64 e o cliente pagava menos do que o desconto
        // que ele autorizou. Agora os dois somam explicitamente.
        CalculoDesconto c = CalculoDesconto.de(
                List.of(new CalculoDesconto.Item(b("2"), b("27.90"), b("5.58"))),
                null, b("5.58"), null);

        assertEquals(0, b("5.58").compareTo(c.getValorDescontoItens()));
        assertEquals(0, b("5.58").compareTo(c.getValorDescontoPedido()));
        assertEquals(0, b("11.16").compareTo(c.getValorDescontoTotal()));
        assertEquals(0, b("44.64").compareTo(c.getValorTotal()));
    }

    @Test
    @DisplayName("desconto so no item: total e' bruto menos o item")
    void descontoSoNoItem() {
        CalculoDesconto c = CalculoDesconto.de(
                List.of(new CalculoDesconto.Item(b("3"), b("10.00"), b("5.00"))),
                null, null, null);

        assertEquals(0, b("30.00").compareTo(c.getValorProdutos()));
        assertEquals(0, b("25.00").compareTo(c.getValorTotal()));
    }

    @Test
    @DisplayName("percentual incide sobre o subtotal JA liquido dos itens, nao sobre o bruto")
    void percentualSobreSubtotalLiquido() {
        // 2 x 50,00 = 100,00; item com 10,00 de desconto -> base 90,00;
        // 10% de 90,00 = 9,00 (e nao 10% de 100,00 = 10,00).
        CalculoDesconto c = CalculoDesconto.de(
                List.of(new CalculoDesconto.Item(b("2"), b("50.00"), b("10.00"))),
                b("10"), null, null);

        assertEquals(0, b("9.00").compareTo(c.getValorDescontoPedido()),
                "10% deve incidir sobre 90,00, nao sobre 100,00");
        assertEquals(0, b("81.00").compareTo(c.getValorTotal()));
    }

    @Test
    @DisplayName("item e pedido somam no desconto total")
    void itemMaisPedido() {
        CalculoDesconto c = CalculoDesconto.de(
                List.of(new CalculoDesconto.Item(b("2"), b("27.90"), b("5.58"))),
                null, b("4.42"), null);

        assertEquals(0, b("4.42").compareTo(c.getValorDescontoPedido()));
        assertEquals(0, b("10.00").compareTo(c.getValorDescontoTotal()));
        assertEquals(0, b("45.80").compareTo(c.getValorTotal()));
    }

    @Test
    @DisplayName("percentualDesconto e valorDesconto juntos: 400, nao 422")
    void osDoisDescontosJuntosSaoRecusados() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> CalculoDesconto.de(
                        List.of(new CalculoDesconto.Item(b("1"), b("10.00"), Z)),
                        b("10"), b("5.00"), null));

        assertEquals(true, erro.getMessage().contains("nunca os dois"), erro.getMessage());
    }

    @Test
    @DisplayName("desconto do pedido maior que o subtotal: recusado, nao total negativo")
    void descontoMaiorQueOSubtotal() {
        assertThrows(BusinessException.class, () -> CalculoDesconto.de(
                List.of(new CalculoDesconto.Item(b("1"), b("10.00"), Z)),
                null, b("99.00"), null));
    }

    @Test
    @DisplayName("multiplos itens: soma tudo certo")
    void variosItens() {
        CalculoDesconto c = CalculoDesconto.de(List.of(
                        new CalculoDesconto.Item(b("2"), b("27.90"), b("5.58")),
                        new CalculoDesconto.Item(b("1"), b("11.00"), Z),
                        new CalculoDesconto.Item(b("3"), b("5.00"), b("1.00"))),
                null, null, null);

        assertEquals(0, b("81.80").compareTo(c.getValorProdutos()),   "55,80 + 11,00 + 15,00");
        assertEquals(0, b("6.58").compareTo(c.getValorDescontoItens()));
        assertEquals(0, b("75.22").compareTo(c.getValorTotal()));
    }

    @Test
    @DisplayName("frete soma depois dos descontos")
    void freteSomaNoFinal() {
        CalculoDesconto c = CalculoDesconto.de(
                List.of(new CalculoDesconto.Item(b("1"), b("100.00"), b("10.00"))),
                null, null, b("15.00"));

        assertEquals(0, b("105.00").compareTo(c.getValorTotal()));
    }

    @Test
    @DisplayName("venda sem desconto: total igual ao bruto")
    void semDesconto() {
        CalculoDesconto c = CalculoDesconto.de(
                List.of(new CalculoDesconto.Item(b("4"), b("12.50"), Z)), null, null, null);

        assertEquals(0, b("50.00").compareTo(c.getValorTotal()));
    }

    @Test
    void descontoNegativoNaoPodeSerCompensadoPorOutroItem() {
        assertThrows(BusinessException.class, () -> CalculoDesconto.de(List.of(
                new CalculoDesconto.Item(b("1"), b("10"), b("-2")),
                new CalculoDesconto.Item(b("1"), b("10"), b("3"))), null, null, null));
    }

    @Test
    void descontoAcimaDoItemNaoPodeSerCompensadoPorOutroItem() {
        assertThrows(BusinessException.class, () -> CalculoDesconto.de(List.of(
                new CalculoDesconto.Item(b("1"), b("10"), b("11")),
                new CalculoDesconto.Item(b("1"), b("100"), Z)), null, null, null));
    }

    @Test
    void freteNegativoRecusadoMesmoComTotalPositivo() {
        assertThrows(BusinessException.class, () -> CalculoDesconto.de(
                List.of(new CalculoDesconto.Item(b("1"), b("100"), Z)), null, null, b("-1")));
    }

    @Test
    void percentualAcimaDeCemRecusadoMesmoComBaseZero() {
        assertThrows(BusinessException.class, () -> CalculoDesconto.de(
                List.of(new CalculoDesconto.Item(b("1"), Z, Z)), b("101"), null, null));
    }

    @Test
    void descontoIntegralPermitido() {
        CalculoDesconto calculo = CalculoDesconto.de(
                List.of(new CalculoDesconto.Item(b("1"), b("100"), Z)), b("100"), null, b("5"));
        assertEquals(0, b("5").compareTo(calculo.getValorTotal()));
    }

    @Test
    void quantidadeZeroRecusadaNoCalculoCompartilhado() {
        assertThrows(BusinessException.class, () -> CalculoDesconto.de(
                List.of(new CalculoDesconto.Item(Z, b("100"), Z)), null, null, null));
    }
}
