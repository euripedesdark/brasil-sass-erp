package br.com.brasil_saas.contabilidade.service.impl;

import br.com.brasil_saas.contabilidade.model.CtbFechamento;
import br.com.brasil_saas.contabilidade.model.CtbLancamento;
import br.com.brasil_saas.contabilidade.model.CtbPartida;
import br.com.brasil_saas.contabilidade.repository.CtbFechamentoRepository;
import br.com.brasil_saas.contabilidade.repository.CtbLancamentoRepository;
import br.com.brasil_saas.contabilidade.repository.CtbPartidaRepository;
import br.com.brasil_saas.contabilidade.service.ContabilidadeService;
import br.com.brasil_saas.financeiro.model.PlanoContas;
import br.com.brasil_saas.financeiro.repository.PlanoContasRepository;
import br.com.brasil_saas.financeiro.repository.TituloRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContabilidadeEspelhoDevolucaoTest {
    @Mock CtbLancamentoRepository lancamentos;
    @Mock CtbPartidaRepository partidas;
    @Mock CtbFechamentoRepository fechamentos;
    @Mock PlanoContasRepository contas;
    @Mock TituloRepository titulos;
    @InjectMocks ContabilidadeServiceImpl service;

    private CtbLancamento original() {
        var l = new CtbLancamento();
        l.setId(9L); l.setEmpresaId(1L);
        l.setData(LocalDate.of(2026, 1, 5)); l.setPeriodo("2026-01");
        l.setHistorico("Titulo #7");
        l.setOrigemTipo("TITULO"); l.setOrigemId(7L); l.setStatus("LANCADO");
        return l;
    }

    private CtbPartida partida(Long conta, String debito, String credito) {
        var p = new CtbPartida();
        p.setContaId(conta);
        p.setDebito(new BigDecimal(debito)); p.setCredito(new BigDecimal(credito));
        return p;
    }

    @Test
    void espelhaProporcionalInvertendoPartidas() {
        when(lancamentos.findByEmpresaIdAndOrigemTipoAndOrigemIdAndStatusInAndDeletedAtIsNull(
                eq(1L), eq("TITULO"), eq(7L), any())).thenReturn(List.of(original()));
        when(fechamentos.findByEmpresaIdAndDeletedAtIsNull(1L)).thenReturn(List.of());
        var gravadas = new ArrayList<CtbPartida>();
        when(partidas.save(any())).thenAnswer(i -> {
            CtbPartida p = i.getArgument(0); gravadas.add(p); return p;
        });
        when(lancamentos.save(any())).thenAnswer(i -> {
            CtbLancamento l = i.getArgument(0); l.setId(10L); return l;
        });
        when(lancamentos.findByIdForUpdate(10L, 1L)).thenAnswer(i -> {
            var l = new CtbLancamento();
            l.setId(10L); l.setStatus("RASCUNHO"); l.setPeriodo(lancadoPeriodo()); l.setOrigemId(8L);
            return Optional.of(l);
        });
        when(partidas.findByLancamentoIdAndEmpresaIdAndDeletedAtIsNull(10L, 1L))
                .thenAnswer(i -> List.copyOf(gravadas));
        when(partidas.findByLancamentoIdAndEmpresaIdAndDeletedAtIsNull(9L, 1L))
                .thenReturn(List.of(partida(100L, "100.00", "0"), partida(200L, "0", "100.00")));
        var r = service.espelharAjusteDevolucao(1L, 7L, 8L, new BigDecimal("0.40"), "Ajuste devolucao DV-1");
        assertEquals("APLICADO", r.situacao());
        assertEquals(1, r.lancamentos().size());
        assertEquals("LANCADO", r.lancamentos().get(0).getStatus());
        assertEquals(2, gravadas.size());
        var porConta = new java.util.HashMap<Long, CtbPartida>();
        for (var g : gravadas) porConta.put(g.getContaId(), g);
        assertEquals(0, new BigDecimal("40.00").compareTo(porConta.get(100L).getCredito()));
        assertEquals(0, new BigDecimal("40.00").compareTo(porConta.get(200L).getDebito()));
        assertEquals(8L, r.lancamentos().get(0).getOrigemId());
    }

    private String lancadoPeriodo() {
        var agora = LocalDate.now();
        return String.format("%04d-%02d", agora.getYear(), agora.getMonthValue());
    }

    @Test
    void semLancamentoOriginalRetornaSituacao() {
        when(lancamentos.findByEmpresaIdAndOrigemTipoAndOrigemIdAndStatusInAndDeletedAtIsNull(
                eq(1L), eq("TITULO"), eq(7L), any())).thenReturn(List.of());
        var r = service.espelharAjusteDevolucao(1L, 7L, 8L, new BigDecimal("0.40"), "Ajuste devolucao DV-1");
        assertEquals("SEM_LANCAMENTO_ORIGINAL", r.situacao());
        assertTrue(r.lancamentos().isEmpty());
        verify(lancamentos, never()).save(any());
    }

    @Test
    void periodoFechadoRetornaSituacao() {
        when(lancamentos.findByEmpresaIdAndOrigemTipoAndOrigemIdAndStatusInAndDeletedAtIsNull(
                eq(1L), eq("TITULO"), eq(7L), any())).thenReturn(List.of(original()));
        var f = new CtbFechamento();
        f.setPeriodo(lancadoPeriodo()); f.setStatus("FECHADO");
        when(fechamentos.findByEmpresaIdAndDeletedAtIsNull(1L)).thenReturn(List.of(f));
        var r = service.espelharAjusteDevolucao(1L, 7L, 8L, new BigDecimal("0.40"), "Ajuste devolucao DV-1");
        assertEquals("PERIODO_FECHADO", r.situacao());
        verify(lancamentos, never()).save(any());
    }

    @Test
    void proporcaoInvalidaRejeita() {
        assertThrows(ResponseStatusException.class,
                () -> service.espelharAjusteDevolucao(1L, 7L, 8L, BigDecimal.ZERO, "Ajuste devolucao DV-1"));
        assertThrows(ResponseStatusException.class,
                () -> service.espelharAjusteDevolucao(1L, 7L, 8L, new BigDecimal("2"), "Ajuste devolucao DV-1"));
        verifyNoInteractions(lancamentos, partidas, fechamentos);
    }
}
