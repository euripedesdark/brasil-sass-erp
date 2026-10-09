package br.com.brasil_saas.contabilidade.service.impl;

import br.com.brasil_saas.contabilidade.model.*;
import br.com.brasil_saas.contabilidade.repository.*;
import br.com.brasil_saas.financeiro.model.PlanoContas;
import br.com.brasil_saas.financeiro.model.Titulo;
import br.com.brasil_saas.financeiro.repository.PlanoContasRepository;
import br.com.brasil_saas.financeiro.repository.TituloRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContabilidadeServiceImplTest {
    @Mock CtbLancamentoRepository lancamentos;
    @Mock CtbPartidaRepository partidas;
    @Mock CtbFechamentoRepository fechamentos;
    @Mock PlanoContasRepository contas;
    @Mock TituloRepository titulos;
    @InjectMocks ContabilidadeServiceImpl service;

    private String periodoAtual() {
        var h = LocalDate.now();
        return String.format("%04d-%02d", h.getYear(), h.getMonthValue());
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.CsvSource({"10,-1", "-1,10", "0,0", "1,1"})
    void partidaNaoAceitaValoresInvalidos(BigDecimal debito, BigDecimal credito) {
        var l = new CtbLancamento(); l.setId(1L); l.setStatus("RASCUNHO"); l.setPeriodo("2026-10");
        when(lancamentos.findByIdForUpdate(1L, 2L)).thenReturn(Optional.of(l));
        when(contas.findByIdAndEmpresaIdAndDeletedAtIsNull(3L, 2L)).thenReturn(Optional.of(new PlanoContas()));
        var p = new CtbPartida(); p.setContaId(3L); p.setDebito(debito); p.setCredito(credito);
        assertThrows(ResponseStatusException.class, () -> service.addPartida(2L, 1L, p));
        verify(partidas, never()).save(any());
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.CsvSource({"10,0", "0,10"})
    void partidaValidaMantemSeuValor(BigDecimal debito, BigDecimal credito) {
        var l = new CtbLancamento(); l.setId(1L); l.setStatus("RASCUNHO"); l.setPeriodo("2026-10");
        when(lancamentos.findByIdForUpdate(1L, 2L)).thenReturn(Optional.of(l));
        when(contas.findByIdAndEmpresaIdAndDeletedAtIsNull(3L, 2L)).thenReturn(Optional.of(new PlanoContas()));
        when(partidas.save(any())).thenAnswer(i -> i.getArgument(0));
        var p = new CtbPartida(); p.setContaId(3L); p.setDebito(debito); p.setCredito(credito);
        assertSame(p, service.addPartida(2L, 1L, p));
        assertEquals(debito, p.getDebito()); assertEquals(credito, p.getCredito());
    }

    @Test
    void lancamentoLegadoComPartidaNegativaNaoPodeSerContabilizado() {
        var l = new CtbLancamento(); l.setId(1L); l.setStatus("RASCUNHO"); l.setPeriodo("2026-10");
        when(lancamentos.findByIdForUpdate(1L, 2L)).thenReturn(Optional.of(l));
        var p = new CtbPartida(); p.setDebito(BigDecimal.TEN); p.setCredito(new BigDecimal("-1"));
        when(partidas.findByLancamentoIdAndEmpresaIdAndDeletedAtIsNull(1L, 2L)).thenReturn(List.of(p));
        assertThrows(ResponseStatusException.class, () -> service.lancar(2L, 1L));
        assertEquals("RASCUNHO", l.getStatus()); verify(lancamentos, never()).save(any());
    }

    @Test
    void balanceteMantemOriginalEstornadoEContraPartidaParaSaldoZero() {
        var original = new CtbLancamento(); original.setId(1L); original.setStatus("ESTORNADO"); original.setData(LocalDate.of(2026, 10, 9));
        var estorno = new CtbLancamento(); estorno.setId(2L); estorno.setStatus("LANCADO"); estorno.setData(original.getData());
        when(lancamentos.findByEmpresaIdAndDeletedAtIsNull(2L)).thenReturn(List.of(original, estorno));
        var a = new CtbPartida(); a.setContaId(3L); a.setDebito(BigDecimal.TEN); a.setCredito(BigDecimal.ZERO);
        var b = new CtbPartida(); b.setContaId(3L); b.setDebito(BigDecimal.ZERO); b.setCredito(BigDecimal.TEN);
        when(partidas.findByLancamentoIdAndEmpresaIdAndDeletedAtIsNull(1L, 2L)).thenReturn(List.of(a));
        when(partidas.findByLancamentoIdAndEmpresaIdAndDeletedAtIsNull(2L, 2L)).thenReturn(List.of(b));
        var linhas = service.balancete(2L, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));
        assertEquals(1, linhas.size());
        assertEquals(BigDecimal.TEN, linhas.get(0).get("debito")); assertEquals(BigDecimal.TEN, linhas.get(0).get("credito"));
        assertEquals(0, ((BigDecimal) linhas.get(0).get("saldo")).signum());
    }

    @Test
    void estornoNaoPodeCairEmPeriodoAtualFechado() {
        var original = new CtbLancamento();
        original.setId(1L); original.setStatus("LANCADO"); original.setPeriodo("2020-01");
        when(lancamentos.findByIdForUpdate(1L, 2L)).thenReturn(Optional.of(original));
        var fechado = new CtbFechamento();
        fechado.setPeriodo(periodoAtual()); fechado.setStatus("FECHADO");
        when(fechamentos.findByEmpresaIdAndDeletedAtIsNull(2L)).thenReturn(List.of(fechado));
        assertThrows(ResponseStatusException.class, () -> service.estornar(2L, 1L, "erro"));
        verify(lancamentos, never()).save(any());
    }

    @Test
    void tituloJaContabilizadoNaoGeraSegundoLancamento() {
        var t = new Titulo();
        t.setId(7L); t.setValorOriginal(new BigDecimal("100")); t.setValorSaldo(new BigDecimal("40"));
        when(titulos.findForUpdate(7L, 2L)).thenReturn(Optional.of(t));
        when(contas.findByIdAndEmpresaIdAndDeletedAtIsNull(any(), any())).thenReturn(Optional.of(new PlanoContas()));
        when(lancamentos.findByEmpresaIdAndOrigemTipoAndOrigemIdAndStatusInAndDeletedAtIsNull(
                eq(2L), eq("TITULO"), eq(7L), anyCollection())).thenReturn(List.of(new CtbLancamento()));
        var ex = assertThrows(ResponseStatusException.class, () -> service.gerarDeTitulo(2L, 9L, 7L, 11L, 12L));
        assertTrue(ex.getReason().contains("ja contabilizado"));
        verify(lancamentos, never()).save(any());
    }

    @Test
    void tituloSemValorNaoGeraLancamento() {
        var t = new Titulo();
        t.setId(7L); t.setValorOriginal(BigDecimal.ZERO); t.setValorSaldo(BigDecimal.ZERO);
        when(titulos.findForUpdate(7L, 2L)).thenReturn(Optional.of(t));
        when(contas.findByIdAndEmpresaIdAndDeletedAtIsNull(any(), any())).thenReturn(Optional.of(new PlanoContas()));
        assertThrows(ResponseStatusException.class, () -> service.gerarDeTitulo(2L, 9L, 7L, 11L, 12L));
        verify(lancamentos, never()).save(any());
    }

    @Test
    void fecharRejeitaPeriodoMalFormado() {
        assertEquals(400, assertThrows(ResponseStatusException.class,
                () -> service.fechar(2L, 9L, "2026-13")).getStatusCode().value());
        verifyNoInteractions(fechamentos);
    }

    @Test
    void fecharRejeitaPeriodoComRascunho() {
        var rascunho = new CtbLancamento(); rascunho.setStatus("RASCUNHO");
        when(lancamentos.findByEmpresaIdAndPeriodoAndDeletedAtIsNullOrderByDataDescIdDesc(2L, "2026-05"))
                .thenReturn(List.of(rascunho));
        assertThrows(ResponseStatusException.class, () -> service.fechar(2L, 9L, "2026-05"));
        verify(fechamentos, never()).save(any());
    }

    @Test
    void refecharPeriodoReabertoRegistraQuemFechou() {
        when(lancamentos.findByEmpresaIdAndPeriodoAndDeletedAtIsNullOrderByDataDescIdDesc(2L, "2026-05"))
                .thenReturn(List.of());
        var f = new CtbFechamento(); f.setPeriodo("2026-05"); f.setStatus("ABERTO");
        when(fechamentos.findByEmpresaIdAndPeriodoAndDeletedAtIsNull(2L, "2026-05")).thenReturn(Optional.of(f));
        when(fechamentos.save(any())).thenAnswer(i -> i.getArgument(0));
        var r = service.fechar(2L, 9L, "2026-05");
        assertEquals("FECHADO", r.getStatus());
        assertEquals(9L, r.getFechadoPor());
        assertNotNull(r.getFechadoEm());
    }
}
