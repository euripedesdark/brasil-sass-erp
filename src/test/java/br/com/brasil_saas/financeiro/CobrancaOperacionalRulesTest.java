package br.com.brasil_saas.financeiro;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CobrancaOperacionalRulesTest {
    static int nivelSugerido(long diasAtraso) {
        if (diasAtraso <= 0) return 0;
        if (diasAtraso <= 15) return 1;
        if (diasAtraso <= 30) return 2;
        if (diasAtraso <= 60) return 3;
        return 4;
    }

    @Test
    void niveis() {
        assertEquals(0, nivelSugerido(-5));
        assertEquals(0, nivelSugerido(0));
        assertEquals(1, nivelSugerido(10));
        assertEquals(2, nivelSugerido(20));
        assertEquals(3, nivelSugerido(45));
        assertEquals(4, nivelSugerido(90));
    }
}
