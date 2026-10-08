package br.com.brasil_saas.producao;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProducaoStatusRulesTest {
    static boolean podeIniciar(String s) { return "ABERTO".equals(s); }
    static boolean podeCancelar(String s) { return "ABERTO".equals(s); }
    static boolean podeFinalizar(String s) { return "ABERTO".equals(s) || "EM_PROCESSO".equals(s); }

    @Test
    void ciclo() {
        assertTrue(podeIniciar("ABERTO"));
        assertFalse(podeIniciar("EM_PROCESSO"));
        assertTrue(podeFinalizar("ABERTO"));
        assertTrue(podeFinalizar("EM_PROCESSO"));
        assertFalse(podeFinalizar("FINALIZADO"));
        assertTrue(podeCancelar("ABERTO"));
        assertFalse(podeCancelar("EM_PROCESSO"));
    }
}
