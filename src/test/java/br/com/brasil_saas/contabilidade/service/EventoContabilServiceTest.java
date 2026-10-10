package br.com.brasil_saas.contabilidade.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import br.com.brasil_saas.contabilidade.model.CtbLancamento;
import br.com.brasil_saas.contabilidade.model.CtbPartida;
import br.com.brasil_saas.contabilidade.repository.CtbLancamentoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class EventoContabilServiceTest {

    @Mock ContabilidadeService contabilidade;
    @Mock CtbLancamentoRepository lancamentos;
    @Mock JdbcTemplate jdbc;
    EventoContabilService svc;

    @BeforeEach
    void setUp() { svc = new EventoContabilService(contabilidade, lancamentos, jdbc); }

    @Test
    void geraLancamentoPartidaDobradaQuandoHaRegra() {
        when(lancamentos.findByEmpresaIdAndOrigemTipoAndOrigemIdAndStatusInAndDeletedAtIsNull(
                anyLong(), anyString(), anyLong(), anyCollection())).thenReturn(List.of());
        when(jdbc.queryForList(anyString(), eq(1L), eq(EventoContabilService.EVT_BAIXA_TITULO)))
                .thenReturn(List.of(Map.of("conta_debito_id", 10L, "conta_credito_id", 20L, "centro_custo_id", 0L, "historico", "Baixa auto")));
        when(contabilidade.salvar(eq(1L), any(CtbLancamento.class))).thenAnswer(inv -> {
            CtbLancamento l = inv.getArgument(1); l.setId(100L); return l;
        });
        when(contabilidade.addPartida(eq(1L), eq(100L), any(CtbPartida.class))).thenAnswer(inv -> inv.getArgument(2));
        CtbLancamento lancado = new CtbLancamento(); lancado.setId(100L); lancado.setStatus("LANCADO");
        when(contabilidade.lancar(1L, 100L)).thenReturn(lancado);
        when(contabilidade.totais(1L, 100L)).thenReturn(new BigDecimal[]{new BigDecimal("50.00"), new BigDecimal("50.00")});
        when(jdbc.update(anyString(), any(), any(), any(), any())).thenReturn(0);

        var r = svc.contabilizar(1L, EventoContabilService.EVT_BAIXA_TITULO, "BAIXA", 7L,
                LocalDate.of(2026, 10, 10), new BigDecimal("50.00"), "teste");
        assertTrue(r.lancamento().isPresent());
        assertFalse(r.pendente());
        ArgumentCaptor<CtbPartida> partidas = ArgumentCaptor.forClass(CtbPartida.class);
        verify(contabilidade, times(2)).addPartida(eq(1L), eq(100L), partidas.capture());
        BigDecimal deb = partidas.getAllValues().stream().map(CtbPartida::getDebito).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal cre = partidas.getAllValues().stream().map(CtbPartida::getCredito).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, deb.compareTo(cre));
        assertEquals(0, deb.compareTo(new BigDecimal("50.00")));
    }

    @Test
    void repeticaoNaoDuplica() {
        CtbLancamento existente = new CtbLancamento(); existente.setId(5L); existente.setStatus("LANCADO");
        when(lancamentos.findByEmpresaIdAndOrigemTipoAndOrigemIdAndStatusInAndDeletedAtIsNull(
                anyLong(), anyString(), anyLong(), anyCollection())).thenReturn(List.of(existente));
        var r = svc.contabilizar(1L, EventoContabilService.EVT_BAIXA_TITULO, "BAIXA", 7L,
                LocalDate.now(), new BigDecimal("10"), null);
        assertEquals("JA_EXISTIA", r.motivo());
        verify(contabilidade, never()).salvar(anyLong(), any());
    }

    @Test
    void semRegraGeraPendenciaSemFalhar() {
        when(lancamentos.findByEmpresaIdAndOrigemTipoAndOrigemIdAndStatusInAndDeletedAtIsNull(
                anyLong(), anyString(), anyLong(), anyCollection())).thenReturn(List.of());
        when(jdbc.queryForList(anyString(), eq(1L), eq(EventoContabilService.EVT_FATURAMENTO_VENDA)))
                .thenReturn(List.of());
        when(jdbc.update(anyString(), any(), any(), any(), any(), any(), any())).thenReturn(1);
        var r = svc.contabilizar(1L, EventoContabilService.EVT_FATURAMENTO_VENDA, "PEDIDO", 9L,
                LocalDate.now(), new BigDecimal("100"), null);
        assertTrue(r.pendente());
        assertTrue(r.lancamento().isEmpty());
        verify(jdbc).update(contains("bc_ctb_pendencia"), eq(1L), eq(EventoContabilService.EVT_FATURAMENTO_VENDA),
                eq("PEDIDO"), eq(9L), anyString(), any());
    }

    @Test
    void periodoFechadoPropagaExcecao() {
        when(lancamentos.findByEmpresaIdAndOrigemTipoAndOrigemIdAndStatusInAndDeletedAtIsNull(
                anyLong(), anyString(), anyLong(), anyCollection())).thenReturn(List.of());
        when(jdbc.queryForList(anyString(), eq(1L), eq(EventoContabilService.EVT_BAIXA_TITULO)))
                .thenReturn(List.of(Map.of("conta_debito_id", 1L, "conta_credito_id", 2L)));
        when(contabilidade.salvar(eq(1L), any())).thenThrow(
                new ResponseStatusException(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY, "Periodo fechado"));
        when(jdbc.update(anyString(), any(), any(), any(), any(), any(), any())).thenReturn(1);
        assertThrows(ResponseStatusException.class, () ->
                svc.contabilizar(1L, EventoContabilService.EVT_BAIXA_TITULO, "BAIXA", 1L,
                        LocalDate.now(), new BigDecimal("10"), null));
    }
}
