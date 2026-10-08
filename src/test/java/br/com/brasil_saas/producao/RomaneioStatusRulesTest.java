package br.com.brasil_saas.producao;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RomaneioStatusRulesTest {
    static boolean podeConferir(String s) { return "ABERTO".equals(s); }
    static boolean podeLiberar(String s) { return "CONFERIDO".equals(s); }
    static boolean podeCancelar(String s) { return "ABERTO".equals(s); }

    @Test
    void ciclo() {
        assertTrue(podeConferir("ABERTO"));
        assertFalse(podeConferir("CONFERIDO"));
        assertTrue(podeLiberar("CONFERIDO"));
        assertFalse(podeLiberar("ABERTO"));
        assertTrue(podeCancelar("ABERTO"));
        assertFalse(podeCancelar("LIBERADO"));
    }
}
