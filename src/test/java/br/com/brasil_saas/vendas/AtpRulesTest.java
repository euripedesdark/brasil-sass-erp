package br.com.brasil_saas.vendas;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class AtpRulesTest {
    static boolean ok(BigDecimal disponivel, BigDecimal solicitado) {
        if (solicitado == null) solicitado = BigDecimal.ZERO;
        if (disponivel == null) disponivel = BigDecimal.ZERO;
        return disponivel.compareTo(solicitado) >= 0;
    }

    @Test
    void regras() {
        assertTrue(ok(new BigDecimal("10"), new BigDecimal("5")));
        assertTrue(ok(new BigDecimal("5"), new BigDecimal("5")));
        assertFalse(ok(new BigDecimal("4"), new BigDecimal("5")));
        assertFalse(ok(null, new BigDecimal("1")));
    }
}
