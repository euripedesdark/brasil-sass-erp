package br.com.brasil_saas.financeiro;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class CreditoLimiteRulesTest {
    static boolean limiteValido(BigDecimal l) {
        return l != null && l.signum() >= 0;
    }
    @Test
    void regras() {
        assertTrue(limiteValido(BigDecimal.ZERO));
        assertTrue(limiteValido(new BigDecimal("1000.00")));
        assertFalse(limiteValido(new BigDecimal("-1")));
        assertFalse(limiteValido(null));
    }
}
