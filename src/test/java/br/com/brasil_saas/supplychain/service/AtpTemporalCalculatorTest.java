package br.com.brasil_saas.supplychain.service;

import static org.junit.jupiter.api.Assertions.*;

import br.com.brasil_saas.supplychain.service.AtpTemporalCalculator.Movimento;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class AtpTemporalCalculatorTest {
    static BigDecimal n(String v) { return new BigDecimal(v); }
    static LocalDate d(String v) { return LocalDate.parse(v); }

    @Test void entradaFuturaSoContaAPartirDaSuaData() {
        var ent = List.of(new Movimento(d("2026-10-20"), n("40")));
        assertEquals(0, AtpTemporalCalculator.disponivelEm(d("2026-10-15"), n("10"), n("0"), ent, null).compareTo(n("10")));
        assertEquals(0, AtpTemporalCalculator.disponivelEm(d("2026-10-20"), n("10"), n("0"), ent, null).compareTo(n("50")));
    }

    @Test void reservasReduzemODisponivel() {
        assertEquals(0, AtpTemporalCalculator.disponivelEm(d("2026-10-15"), n("10"), n("4"), null, null).compareTo(n("6")));
    }

    @Test void prometivelNaoExcedeSaldoMinimoDeDatasPosteriores() {
        var ent = List.of(new Movimento(d("2026-10-12"), n("30")));
        var sai = List.of(new Movimento(d("2026-10-18"), n("35")));
        // Em 12/10 ha 40, mas a saida de 35 em 18/10 deixa 5: so se promete 5.
        assertEquals(0, AtpTemporalCalculator.prometivel(d("2026-10-12"), n("10"), n("0"), ent, sai).compareTo(n("5")));
    }

    @Test void prometivelNuncaENegativo() {
        var sai = List.of(new Movimento(d("2026-10-12"), n("50")));
        assertEquals(0, AtpTemporalCalculator.prometivel(d("2026-10-10"), n("10"), n("0"), null, sai).compareTo(BigDecimal.ZERO));
    }
}
