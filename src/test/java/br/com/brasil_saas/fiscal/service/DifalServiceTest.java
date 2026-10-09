package br.com.brasil_saas.fiscal.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DifalServiceTest {
    private final DifalService svc = new DifalService();

    @Test
    void difalBasico() {
        Map<String, Object> r = svc.calcular(new BigDecimal("1000.00"),
                new BigDecimal("12"), new BigDecimal("18"), new BigDecimal("2"));
        assertEquals(new BigDecimal("120.00"), r.get("icmsOrigem"));
        assertEquals(new BigDecimal("60.00"), r.get("difal"));
        assertEquals(new BigDecimal("20.00"), r.get("fcp"));
        assertEquals(new BigDecimal("80.00"), r.get("totalUfDestino"));
    }

    @Test
    void semDifalQuandoInternaMenor() {
        Map<String, Object> r = svc.calcular(new BigDecimal("100"),
                new BigDecimal("12"), new BigDecimal("7"), BigDecimal.ZERO);
        assertEquals(new BigDecimal("0.00"), r.get("difal"));
    }
}
