package br.com.brasil_saas.producao.service;

import static org.junit.jupiter.api.Assertions.*;

import br.com.brasil_saas.producao.service.MrpPlanejadorTemporal.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class MrpPlanejadorTemporalTest {
    static final LocalDate HOJE = LocalDate.of(2026, 10, 10);
    static BigDecimal n(String v) { return new BigDecimal(v); }
    static LocalDate d(String v) { return LocalDate.parse(v); }

    @Test void liberaAntecipadoPeloLeadTime() {
        var o = MrpPlanejadorTemporal.planejar(HOJE, n("0"),
                List.of(new Evento(d("2026-10-30"), n("100"))), List.of(),
                new Parametros(7, n("0"), n("0"), n("0")));
        assertEquals(1, o.size());
        assertEquals(d("2026-10-23"), o.get(0).liberacao());
        assertEquals(d("2026-10-30"), o.get(0).necessidade());
        assertEquals(0, o.get(0).quantidade().compareTo(n("100")));
        assertFalse(o.get(0).atrasada());
    }

    @Test void estoqueDeSegurancaAumentaAQuantidade() {
        var o = MrpPlanejadorTemporal.planejar(HOJE, n("50"),
                List.of(new Evento(d("2026-10-20"), n("50"))), List.of(),
                new Parametros(0, n("20"), n("0"), n("0")));
        assertEquals(0, o.get(0).quantidade().compareTo(n("20")));
    }

    @Test void loteMinimoEMultiploArredondamParaCima() {
        var o = MrpPlanejadorTemporal.planejar(HOJE, n("0"),
                List.of(new Evento(d("2026-10-20"), n("13"))), List.of(),
                new Parametros(0, n("0"), n("10"), n("25")));
        assertEquals(0, o.get(0).quantidade().compareTo(n("25")));
    }

    @Test void recebimentoProgramadoEvitaOrdem() {
        var o = MrpPlanejadorTemporal.planejar(HOJE, n("0"),
                List.of(new Evento(d("2026-10-20"), n("30"))),
                List.of(new Evento(d("2026-10-15"), n("30"))),
                new Parametros(5, n("0"), n("0"), n("0")));
        assertTrue(o.isEmpty());
    }

    @Test void prazoNoPassadoViraOrdemAtrasadaLiberadaHoje() {
        var o = MrpPlanejadorTemporal.planejar(HOJE, n("0"),
                List.of(new Evento(d("2026-10-12"), n("10"))), List.of(),
                new Parametros(10, n("0"), n("0"), n("0")));
        assertTrue(o.get(0).atrasada());
        assertEquals(HOJE, o.get(0).liberacao());
    }

    @Test void ordemPlanejadaCobreNecessidadesSeguintesSemDuplicar() {
        var o = MrpPlanejadorTemporal.planejar(HOJE, n("0"),
                List.of(new Evento(d("2026-10-20"), n("10")), new Evento(d("2026-10-25"), n("10"))),
                List.of(), new Parametros(0, n("0"), n("50"), n("0")));
        assertEquals(1, o.size());
        assertEquals(0, o.get(0).quantidade().compareTo(n("50")));
    }
}
