package br.com.brasil_saas.estoque.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Regras de cancelamento de reserva de estoque. */
class ReservaCancelarRulesTest {

    private static boolean podeCancelar(String status) {
        return "RESERVADA".equals(status);
    }

    @Test
    void soReservadaPodeCancelar() {
        assertTrue(podeCancelar("RESERVADA"));
        assertFalse(podeCancelar("SEPARACAO"));
        assertFalse(podeCancelar("LIBERADA"));
        assertFalse(podeCancelar("CONSUMIDA"));
        assertFalse(podeCancelar("CANCELADA"));
    }
}
