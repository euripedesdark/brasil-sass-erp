package br.com.brasil_saas.financeiro;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.math.RoundingMode;
import static org.junit.jupiter.api.Assertions.*;

class CopaRulesTest {
    static BigDecimal custoEstimado(BigDecimal receita) {
        return receita.multiply(new BigDecimal("0.70")).setScale(2, RoundingMode.HALF_UP);
    }

    @Test
    void margem() {
        BigDecimal rec = new BigDecimal("1000.00");
        BigDecimal custo = custoEstimado(rec);
        assertEquals(new BigDecimal("700.00"), custo);
        assertEquals(new BigDecimal("300.00"), rec.subtract(custo));
    }
}
