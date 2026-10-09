package br.com.brasil_saas.rh;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.math.RoundingMode;
import static org.junit.jupiter.api.Assertions.*;

class EncargosRulesTest {
    static BigDecimal pct(BigDecimal base, BigDecimal aliq) {
        if (aliq == null || aliq.signum() <= 0) return BigDecimal.ZERO;
        return base.multiply(aliq).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    @Test
    void regras() {
        BigDecimal base = new BigDecimal("10000.00");
        assertEquals(new BigDecimal("2000.00"), pct(base, new BigDecimal("20")));
        assertEquals(new BigDecimal("800.00"), pct(base, new BigDecimal("8")));
        assertEquals(new BigDecimal("200.00"), pct(base, new BigDecimal("2")));
        assertEquals(BigDecimal.ZERO, pct(base, BigDecimal.ZERO));
    }
}
