package br.com.brasil_saas.estoque.controller;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InventarioCancelarRulesTest {
    private static boolean podeCancelar(String status) {
        return "ABERTO".equals(status);
    }
    @Test
    void soAbertoPodeCancelar() {
        assertTrue(podeCancelar("ABERTO"));
        assertFalse(podeCancelar("FECHADO"));
        assertFalse(podeCancelar("CANCELADO"));
    }
}
