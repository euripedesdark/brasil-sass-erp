package br.com.brasil_saas.estoque;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TransferenciaItensRulesTest {
    static boolean origemDiferenteDestino(Long a, Long b) {
        return a != null && b != null && !a.equals(b);
    }
    static boolean lotePermitido(String status) {
        return "ATIVO".equals(status);
    }

    @Test
    void regrasBasicas() {
        assertTrue(origemDiferenteDestino(1L, 2L));
        assertFalse(origemDiferenteDestino(1L, 1L));
        assertTrue(lotePermitido("ATIVO"));
        assertFalse(lotePermitido("QUARENTENA"));
        assertFalse(lotePermitido("BLOQUEADO"));
    }
}
