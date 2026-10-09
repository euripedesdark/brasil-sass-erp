package br.com.brasil_saas.enterprise.service;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class IntercompanyConciliacaoTest {
    private static final BigDecimal TOL = new BigDecimal("0.01");
    private static BigDecimal v(String s) { return new BigDecimal(s); }

    @Test void valoresIguaisReconciliam() {
        var r = IntercompanyConciliacao.comparar(v("1000.00"), "BRL", v("1000.00"), "BRL", TOL);
        assertTrue(r.reconciliado());
    }
    @Test void diferencaDentroDaToleranciaReconcilia() {
        assertTrue(IntercompanyConciliacao.comparar(v("1000.00"), "BRL", v("999.99"), "BRL", TOL).reconciliado());
    }
    @Test void divergenciaGuardaDiferenca() {
        var r = IntercompanyConciliacao.comparar(v("1000.00"), "BRL", v("950.00"), "BRL", TOL);
        assertEquals("DIVERGENTE", r.status());
        assertEquals(0, v("50.00").compareTo(r.diferenca()));
    }
    @Test void moedasDiferentesNaoComparam() {
        assertEquals("DIVERGENTE_MOEDA", IntercompanyConciliacao.comparar(v("10"), "BRL", v("10"), "USD", TOL).status());
    }
    @Test void semEspelhoNaoReconcilia() {
        var r = IntercompanyConciliacao.comparar(v("10"), "BRL", null, null, TOL);
        assertEquals("SEM_CONTRAPARTIDA", r.status());
        assertFalse(r.reconciliado());
    }
}
