package br.com.brasil_saas.enterprise.service;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.sql.Date;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class IntercompanyServiceTest {
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final IntercompanyService service = new IntercompanyService(jdbc);

    @Test
    void consultaMesInteiroIsolandoEmpresaEMoedas() {
        when(jdbc.queryForList(anyString(), eq(7L), eq(Date.valueOf("2026-10-01")), eq(Date.valueOf("2026-11-01"))))
                .thenReturn(List.of(Map.of("valor", new BigDecimal("100.00"), "moeda", "BRL"),
                        Map.of("valor", new BigDecimal("20.00"), "moeda", "USD"),
                        Map.of("valor", new BigDecimal("50.00"), "moeda", "BRL")));
        Map<String, Object> r = service.eliminacoes(7L, LocalDate.of(2026, 10, 15));
        assertEquals(Map.of("BRL", new BigDecimal("150.00"), "USD", new BigDecimal("20.00")), r.get("totaisPorMoeda"));
        assertNull(r.get("total"));
        assertEquals("2026-10-01", r.get("competencia"));
        verify(jdbc).queryForList(contains("empresa_id=? and reconciliado=true and deleted_at is null and competencia>=? and competencia<?"),
                eq(7L), eq(Date.valueOf("2026-10-01")), eq(Date.valueOf("2026-11-01")));
    }

    @Test
    void preservaTotalParaMoedaUnica() {
        when(jdbc.queryForList(anyString(), any(Object[].class))).thenReturn(List.of(
                Map.of("valor", new BigDecimal("5.00"), "moeda", "USD")));
        assertEquals(new BigDecimal("5.00"), service.eliminacoes(7L, LocalDate.of(2026, 10, 1)).get("total"));
    }

    @Test
    void semLinhasRetornaZero() {
        when(jdbc.queryForList(anyString(), any(Object[].class))).thenReturn(List.of());
        Map<String, Object> r = service.eliminacoes(7L, LocalDate.of(2026, 12, 31));
        assertEquals(BigDecimal.ZERO, r.get("total"));
        assertEquals(Map.of(), r.get("totaisPorMoeda"));
        verify(jdbc).queryForList(anyString(), eq(7L), eq(Date.valueOf("2026-12-01")), eq(Date.valueOf("2027-01-01")));
    }
    @Test
    void reconciliacaoExigeCompetenciaIgualNoEspelho() {
        Date competencia = Date.valueOf("2026-10-01");
        when(jdbc.queryForList(anyString(), eq(7L), eq(99L))).thenReturn(List.of(
                Map.of("empresa_parceira_id", 8L, "numero", "IC-99", "valor", new BigDecimal("100"),
                        "moeda", "BRL", "competencia", competencia)));
        when(jdbc.queryForList(anyString(), eq(8L), eq(7L), eq("IC-99"), eq(competencia))).thenReturn(List.of());
        when(jdbc.queryForMap(anyString(), eq(7L), eq(99L))).thenReturn(Map.of("status", "SEM_CONTRAPARTIDA"));
        assertEquals("SEM_CONTRAPARTIDA", service.reconciliar(7L, 10L, 99L).get("status"));
        verify(jdbc).queryForList(contains("competencia is not distinct from ?"), eq(8L), eq(7L), eq("IC-99"), eq(competencia));
        verify(jdbc).update(anyString(), eq("SEM_CONTRAPARTIDA"), eq(false), isNull(), isNull(), eq(false), eq(10L), eq(7L), eq(99L));
    }
}
