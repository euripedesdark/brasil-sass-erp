package br.com.brasil_saas.financeiro.service;

import br.com.brasil_saas.financeiro.service.CmvCalculadora.Origem;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class CmvCalculadoraTest {
    private static BigDecimal v(String s) { return new BigDecimal(s); }

    @Test void usaMedioDeComprasQuandoExiste() {
        var c = CmvCalculadora.custoLinha(v("10"), v("1000"), v("40.00"), v("55.00"));
        assertEquals(Origem.MEDIO_COMPRAS, c.origem());
        assertEquals(0, v("400.00").compareTo(c.valor()));
    }
    @Test void caiNoCadastroSemCompras() {
        var c = CmvCalculadora.custoLinha(v("10"), v("1000"), null, v("55.00"));
        assertEquals(Origem.CADASTRO, c.origem());
        assertEquals(0, v("550.00").compareTo(c.valor()));
    }
    @Test void estimaQuandoNaoHaCustoOuEZero() {
        var c = CmvCalculadora.custoLinha(v("10"), v("1000"), BigDecimal.ZERO, BigDecimal.ZERO);
        assertEquals(Origem.ESTIMADO, c.origem());
        assertEquals(0, v("700.00").compareTo(c.valor()));
        assertFalse(c.real());
    }
    @Test void itemSemQuantidadeEstima() {
        assertEquals(Origem.ESTIMADO, CmvCalculadora.custoLinha(null, v("100"), v("5"), v("5")).origem());
    }
    @Test void coberturaEPercentualDaReceitaComCustoReal() {
        assertEquals(0, v("25.00").compareTo(CmvCalculadora.cobertura(v("250"), v("1000"))));
        assertEquals(0, BigDecimal.ZERO.compareTo(CmvCalculadora.cobertura(v("0"), BigDecimal.ZERO)));
    }
}
