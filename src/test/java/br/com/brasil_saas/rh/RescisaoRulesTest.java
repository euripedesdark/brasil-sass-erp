package br.com.brasil_saas.rh;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.math.RoundingMode;
import static org.junit.jupiter.api.Assertions.*;

class RescisaoRulesTest {
    static BigDecimal aviso(String motivo, BigDecimal sal) {
        return "SEM_JUSTA_CAUSA".equals(motivo) ? sal : BigDecimal.ZERO;
    }
    static BigDecimal multa40(String motivo, BigDecimal sal) {
        return "SEM_JUSTA_CAUSA".equals(motivo)
                ? sal.multiply(new BigDecimal("0.40")).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
    }

    @Test
    void regras() {
        BigDecimal sal = new BigDecimal("3000.00");
        assertEquals(sal, aviso("SEM_JUSTA_CAUSA", sal));
        assertEquals(BigDecimal.ZERO, aviso("COM_JUSTA_CAUSA", sal));
        assertEquals(new BigDecimal("1200.00"), multa40("SEM_JUSTA_CAUSA", sal));
        assertEquals(BigDecimal.ZERO, multa40("COM_JUSTA_CAUSA", sal));
    }
}
