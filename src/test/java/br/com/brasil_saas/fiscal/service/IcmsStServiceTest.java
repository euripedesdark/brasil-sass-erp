package br.com.brasil_saas.fiscal.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class IcmsStServiceTest {
    private final IcmsStService svc = new IcmsStService();

    @Test
    void stComMva() {
        // base 1000, inter 12%, interna 18%, MVA 40%
        // baseST = 1400; icmsOp = 120; icmsST = 1400*0.18 - 120 = 132
        Map<String, Object> r = svc.calcular(new BigDecimal("1000"), new BigDecimal("12"),
                new BigDecimal("18"), new BigDecimal("40"), BigDecimal.ZERO);
        assertEquals(new BigDecimal("120.00"), r.get("icmsOperacao"));
        assertEquals(new BigDecimal("1400.00"), r.get("baseSt"));
        assertEquals(new BigDecimal("132.00"), r.get("icmsSt"));
    }

    @Test
    void stZeroSeNegativo() {
        Map<String, Object> r = svc.calcular(new BigDecimal("100"), new BigDecimal("18"),
                new BigDecimal("12"), new BigDecimal("0"), BigDecimal.ZERO);
        assertEquals(new BigDecimal("0.00"), r.get("icmsSt"));
    }
}
