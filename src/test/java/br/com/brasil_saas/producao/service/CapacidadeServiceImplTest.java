package br.com.brasil_saas.producao.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import br.com.brasil_saas.producao.model.AlocacaoCapacidade;
import br.com.brasil_saas.producao.model.CentroTrabalho;
import br.com.brasil_saas.producao.model.OperacaoRoteiro;
import br.com.brasil_saas.producao.model.RoteiroProducao;
import br.com.brasil_saas.producao.repository.AlocacaoCapacidadeRepository;
import br.com.brasil_saas.producao.repository.CentroTrabalhoRepository;
import br.com.brasil_saas.producao.repository.ProducaoRepository;
import br.com.brasil_saas.producao.repository.OperacaoRoteiroRepository;
import br.com.brasil_saas.producao.repository.RoteiroProducaoRepository;
import br.com.brasil_saas.producao.service.impl.CapacidadeServiceImpl;
import br.com.brasil_saas.shared.exception.BusinessException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CapacidadeServiceImplTest {

    private static final Long EMP = 1L;
    // 2026-10-05 e segunda-feira
    private static final LocalDate SEGUNDA = LocalDate.of(2026, 10, 5);

    private RoteiroProducaoRepository roteiros;
    private OperacaoRoteiroRepository ops;
    private CentroTrabalhoRepository centros;
    private AlocacaoCapacidadeRepository alocacoes;
    private CapacidadeServiceImpl svc;

    @BeforeEach
    void setUp() {
        roteiros = mock(RoteiroProducaoRepository.class);
        ops = mock(OperacaoRoteiroRepository.class);
        centros = mock(CentroTrabalhoRepository.class);
        alocacoes = mock(AlocacaoCapacidadeRepository.class);
        when(alocacoes.findByEmpresaIdAndCentroTrabalhoIdAndDataBetweenAndDeletedAtIsNull(
                anyLong(), anyLong(), any(), any())).thenReturn(List.of());
        when(alocacoes.findByEmpresaIdAndOrdemProducaoIdAndDeletedAtIsNull(anyLong(), anyLong())).thenReturn(List.of());
        when(alocacoes.saveAll(any())).thenAnswer(i -> i.getArgument(0));
        svc = new CapacidadeServiceImpl(roteiros, ops, centros, alocacoes, mock(ProducaoRepository.class));
    }

    private RoteiroProducao roteiro(long id) {
        RoteiroProducao r = new RoteiroProducao();
        r.setCodigo("R1");
        r.setNome("Roteiro");
        r.setVersao(1);
        r.setAtivo(true);
        r.setId(id);
        when(roteiros.findByEmpresaIdAndProdutoIdAndDeletedAtIsNullOrderByVersaoDesc(EMP, 1L)).thenReturn(List.of(r));
        return r;
    }

    private OperacaoRoteiro op(int seq, long ct, String setup, String maquina) {
        OperacaoRoteiro o = new OperacaoRoteiro();
        o.setSequencia(seq);
        o.setCodigo("OP" + seq);
        o.setNome("Operacao " + seq);
        o.setCentroTrabalhoId(ct);
        o.setSetupMinutos(new BigDecimal(setup));
        o.setMaquinaMinutos(new BigDecimal(maquina));
        o.setAtivo(true);
        return o;
    }

    private void centro(long id, String horasDia) {
        CentroTrabalho c = new CentroTrabalho();
        c.setCodigo("CT" + id);
        c.setNome("Centro " + id);
        c.setCapacidadeHorasDia(new BigDecimal(horasDia));
        c.setAtivo(true);
        c.setId(id);
        when(centros.findByIdAndEmpresaIdAndDeletedAtIsNull(id, EMP)).thenReturn(Optional.of(c));
    }

    @Test
    @DisplayName("Horas = setup + maquina x quantidade; dias = teto(horas / capacidade)")
    void horasEDias() {
        roteiro(10);
        centro(5, "8");
        // setup 60min + 6min x 100 = 660min = 11h => 2 dias em centro de 8h/dia
        when(ops.findByEmpresaIdAndRoteiroIdAndDeletedAtIsNullOrderBySequenciaAsc(anyLong(), anyLong()))
                .thenReturn(List.of(op(1, 5, "60", "6")));
        var r = svc.simular(EMP, new CapacidadeService.Request(1L, new BigDecimal("100"), SEGUNDA));
        assertEquals(0, new BigDecimal("11").compareTo((BigDecimal) r.get("horasTotais")));
        assertEquals(LocalDate.of(2026, 10, 6), r.get("dataTermino"));
    }

    @Test
    @DisplayName("Fim de semana nao conta como dia util")
    void pulaFimDeSemana() {
        roteiro(10);
        centro(5, "8");
        // 40h em 8h/dia = 5 dias uteis: seg..sex
        when(ops.findByEmpresaIdAndRoteiroIdAndDeletedAtIsNullOrderBySequenciaAsc(anyLong(), anyLong()))
                .thenReturn(List.of(op(1, 5, "0", "60")));
        var r = svc.simular(EMP, new CapacidadeService.Request(1L, new BigDecimal("40"), SEGUNDA));
        assertEquals(LocalDate.of(2026, 10, 9), r.get("dataTermino"));
        // 41h => 6 dias, termina na segunda seguinte
        r = svc.simular(EMP, new CapacidadeService.Request(1L, new BigDecimal("41"), SEGUNDA));
        assertEquals(LocalDate.of(2026, 10, 12), r.get("dataTermino"));
    }

    @Test
    @DisplayName("Inicio num sabado e empurrado para segunda")
    void inicioNoSabado() {
        roteiro(10);
        centro(5, "8");
        when(ops.findByEmpresaIdAndRoteiroIdAndDeletedAtIsNullOrderBySequenciaAsc(anyLong(), anyLong()))
                .thenReturn(List.of(op(1, 5, "0", "60")));
        var r = svc.simular(EMP, new CapacidadeService.Request(1L, BigDecimal.ONE, LocalDate.of(2026, 10, 3)));
        assertEquals(SEGUNDA, r.get("dataInicio"));
    }

    @Test
    @DisplayName("Produto sem roteiro vigente e recusado")
    void semRoteiro() {
        when(roteiros.findByEmpresaIdAndProdutoIdAndDeletedAtIsNullOrderByVersaoDesc(EMP, 1L)).thenReturn(List.of());
        assertThrows(BusinessException.class,
                () -> svc.simular(EMP, new CapacidadeService.Request(1L, BigDecimal.ONE, SEGUNDA)));
    }

    private AlocacaoCapacidade aloc(long centro, LocalDate data, String horas, Long ordem) {
        AlocacaoCapacidade a = new AlocacaoCapacidade();
        a.setCentroTrabalhoId(centro);
        a.setData(data);
        a.setHoras(new BigDecimal(horas));
        a.setOrdemProducaoId(ordem);
        return a;
    }

    @Test
    @DisplayName("Horas ja alocadas no calendario empurram o termino (capacidade finita)")
    void respeitaCalendario() {
        roteiro(10);
        centro(5, "8");
        when(ops.findByEmpresaIdAndRoteiroIdAndDeletedAtIsNullOrderBySequenciaAsc(anyLong(), anyLong()))
                .thenReturn(List.of(op(1, 5, "0", "60")));
        // segunda ja tem 6h ocupadas: so 2h livres
        when(alocacoes.findByEmpresaIdAndCentroTrabalhoIdAndDataBetweenAndDeletedAtIsNull(
                eq(EMP), eq(5L), any(), any())).thenReturn(List.of(aloc(5, SEGUNDA, "6", 99L)));
        // 10h: 2h na segunda + 8h na terca => termina terca
        var r = svc.simular(EMP, new CapacidadeService.Request(1L, new BigDecimal("10"), SEGUNDA));
        assertEquals(LocalDate.of(2026, 10, 6), r.get("dataTermino"));
        // 11h: 2 + 8 + 1 => quarta
        r = svc.simular(EMP, new CapacidadeService.Request(1L, new BigDecimal("11"), SEGUNDA));
        assertEquals(LocalDate.of(2026, 10, 7), r.get("dataTermino"));
    }

    @Test
    @DisplayName("Reagendar a mesma OP ignora a propria reserva anterior")
    void reagendarIgnoraPropriaReserva() {
        roteiro(10);
        centro(5, "8");
        when(ops.findByEmpresaIdAndRoteiroIdAndDeletedAtIsNullOrderBySequenciaAsc(anyLong(), anyLong()))
                .thenReturn(List.of(op(1, 5, "0", "60")));
        // a propria OP 7 ocupa a segunda inteira; ao reagendar, a segunda volta a ficar livre
        var antiga = aloc(5, SEGUNDA, "8", 7L);
        when(alocacoes.findByEmpresaIdAndOrdemProducaoIdAndDeletedAtIsNull(EMP, 7L)).thenReturn(List.of(antiga));
        when(alocacoes.findByEmpresaIdAndCentroTrabalhoIdAndDataBetweenAndDeletedAtIsNull(
                eq(EMP), eq(5L), any(), any())).thenReturn(List.of(antiga));
        var r = svc.agendar(EMP, new CapacidadeService.AgendarRequest(1L, new BigDecimal("8"), SEGUNDA, 7L));
        assertEquals(SEGUNDA, r.get("dataTermino"));
        assertNotNull(antiga.getDeletedAt(), "reserva anterior deve ser liberada");
    }

    @Test
    @DisplayName("Agendar grava uma linha por dia usado, com a soma igual as horas")
    void agendarGrava() {
        roteiro(10);
        centro(5, "8");
        when(ops.findByEmpresaIdAndRoteiroIdAndDeletedAtIsNullOrderBySequenciaAsc(anyLong(), anyLong()))
                .thenReturn(List.of(op(1, 5, "0", "60")));
        var r = svc.agendar(EMP, new CapacidadeService.AgendarRequest(1L, new BigDecimal("12"), SEGUNDA, null));
        assertEquals(0, new BigDecimal("12").compareTo((BigDecimal) r.get("horasReservadas")));
        @SuppressWarnings("unchecked")
        var ids = (List<Long>) r.get("alocacaoIds");
        assertEquals(2, ids.size());
        verify(alocacoes).saveAll(any());
    }

    @Test
    @DisplayName("Calendario mostra alocado, livre e marca sobrecarga")
    void calendario() {
        centro(5, "8");
        when(alocacoes.findByEmpresaIdAndCentroTrabalhoIdAndDataBetweenAndDeletedAtIsNull(
                eq(EMP), eq(5L), any(), any()))
                .thenReturn(List.of(aloc(5, SEGUNDA, "10", 1L), aloc(5, SEGUNDA.plusDays(1), "3", 2L)));
        var rows = svc.calendario(EMP, 5L, SEGUNDA, SEGUNDA.plusDays(6));
        assertEquals(7, rows.size());
        assertEquals(Boolean.TRUE, rows.get(0).get("sobrecarga"));
        assertEquals(0, new BigDecimal("5").compareTo((BigDecimal) rows.get(1).get("livreHoras")));
        assertEquals(Boolean.FALSE, rows.get(5).get("diaUtil"));     // sabado
        assertEquals(0, BigDecimal.ZERO.compareTo((BigDecimal) rows.get(5).get("capacidadeHoras")));
    }
}
