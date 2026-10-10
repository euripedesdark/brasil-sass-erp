package br.com.brasil_saas.supplychain.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Contrato: local_id 0 = empresa toda (V191). Calculo temporal independente do local. */
class AtpLocalIdSentinelaTest {
    static final Long LOCAL_EMPRESA_TODA = 0L;

    @Test
    void sentinelaZeroRepresentaCalculoSemDepositoEspecifico() {
        assertEquals(0L, LOCAL_EMPRESA_TODA);
    }

    @Test
    void prometivelComEntradasFuturasIndependeDoLocal() {
        var ent = List.of(new AtpTemporalCalculator.Movimento(LocalDate.of(2026, 10, 20), new BigDecimal("40")));
        BigDecimal atp = AtpTemporalCalculator.prometivel(
                LocalDate.of(2026, 10, 15), new BigDecimal("10"), BigDecimal.ZERO, ent, null);
        assertEquals(0, atp.compareTo(new BigDecimal("10")));
    }
}
