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
        when(titulos.findByIdAndEmpresaIdAndDeletedAtIsNull(7L, 2L)).thenReturn(Optional.of(t));
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
        when(titulos.findByIdAndEmpresaIdAndDeletedAtIsNull(7L, 2L)).thenReturn(Optional.of(t));
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
