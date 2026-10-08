package br.com.brasil_saas.financeiro;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class CaixaMovimentoRulesTest {
    static BigDecimal aplicar(String tipo, BigDecimal saldo, BigDecimal valor) {
        if (!"SANGRIA".equals(tipo) && !"SUPRIMENTO".equals(tipo)) throw new IllegalArgumentException("tipo");
        if (valor == null || valor.signum() <= 0) throw new IllegalArgumentException("valor");
        if ("SANGRIA".equals(tipo)) {
            if (saldo.compareTo(valor) < 0) throw new IllegalArgumentException("saldo");
            return saldo.subtract(valor);
        }
        return saldo.add(valor);
    }

    @Test
    void regras() {
        assertEquals(new BigDecimal("70"), aplicar("SANGRIA", new BigDecimal("100"), new BigDecimal("30")));
        assertEquals(new BigDecimal("130"), aplicar("SUPRIMENTO", new BigDecimal("100"), new BigDecimal("30")));
        assertThrows(IllegalArgumentException.class, () -> aplicar("SANGRIA", new BigDecimal("10"), new BigDecimal("30")));
        assertThrows(IllegalArgumentException.class, () -> aplicar("FOO", BigDecimal.TEN, BigDecimal.ONE));
        assertThrows(IllegalArgumentException.class, () -> aplicar("SANGRIA", BigDecimal.TEN, BigDecimal.ZERO));
    }
}
