package br.com.brasil_saas.estoque.service;

import static org.junit.jupiter.api.Assertions.*;

import br.com.brasil_saas.estoque.service.LoteSelector.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class LoteSelectorTest {
    static final LocalDate HOJE = LocalDate.of(2026, 10, 10);
    static BigDecimal n(String v) { return new BigDecimal(v); }
    static LoteDisponivel l(long id, String st, String val, String fab, String q) {
        return new LoteDisponivel(id, st, val == null ? null : LocalDate.parse(val),
                fab == null ? null : LocalDate.parse(fab), n(q));
    }

    @Test void fefoConsomeOQueVencePrimeiroEDividePeloLotes() {
        var r = LoteSelector.selecionar(List.of(
                l(1, "ATIVO", "2027-03-01", "2026-01-01", "10"),
                l(2, "ATIVO", "2026-12-01", "2026-06-01", "4")),
                n("7"), Estrategia.FEFO, HOJE);
        assertEquals(List.of(new Alocacao(2L, n("4")), new Alocacao(1L, n("3"))), r.alocacoes());
        assertTrue(r.completo());
    }

    @Test void fefoLoteSemValidadeVaiPorUltimo() {
        var r = LoteSelector.selecionar(List.of(
                l(1, "ATIVO", null, "2026-01-01", "10"),
                l(2, "ATIVO", "2027-01-01", "2026-06-01", "10")),
                n("5"), Estrategia.FEFO, HOJE);
        assertEquals(2L, r.alocacoes().get(0).loteId());
    }

    @Test void fifoConsomeOMaisAntigoMesmoComValidadeMaisLonge() {
        var r = LoteSelector.selecionar(List.of(
                l(1, "ATIVO", "2027-03-01", "2026-01-01", "10"),
                l(2, "ATIVO", "2026-12-01", "2026-06-01", "10")),
                n("5"), Estrategia.FIFO, HOJE);
        assertEquals(1L, r.alocacoes().get(0).loteId());
    }

    @Test void nuncaSugereQuarentenaBloqueadoOuVencido() {
        var r = LoteSelector.selecionar(List.of(
                l(1, "QUARENTENA", "2027-01-01", null, "10"),
                l(2, "BLOQUEADO", "2027-01-01", null, "10"),
                l(3, "ATIVO", "2026-10-09", null, "10"),
                l(4, "ATIVO", "2026-10-10", null, "2")),
                n("5"), Estrategia.FEFO, HOJE);
        assertEquals(List.of(new Alocacao(4L, n("2"))), r.alocacoes());
        assertEquals(0, r.faltante().compareTo(n("3")));
        assertFalse(r.completo());
    }

    @Test void semLotesRetornaTudoComoFaltante() {
        var r = LoteSelector.selecionar(List.of(), n("5"), Estrategia.FEFO, HOJE);
        assertTrue(r.alocacoes().isEmpty());
        assertEquals(0, r.faltante().compareTo(n("5")));
    }

    @Test void quantidadeNegativaERecusada() {
        assertThrows(IllegalArgumentException.class,
                () -> LoteSelector.selecionar(List.of(), n("-1"), Estrategia.FEFO, HOJE));
    }
}
