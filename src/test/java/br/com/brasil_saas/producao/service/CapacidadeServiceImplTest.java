package br.com.brasil_saas.producao.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import br.com.brasil_saas.producao.model.CentroTrabalho;
import br.com.brasil_saas.producao.model.OperacaoRoteiro;
import br.com.brasil_saas.producao.model.RoteiroProducao;
import br.com.brasil_saas.producao.repository.CentroTrabalhoRepository;
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
    private CapacidadeServiceImpl svc;

    @BeforeEach
    void setUp() {
        roteiros = mock(RoteiroProducaoRepository.class);
        ops = mock(OperacaoRoteiroRepository.class);
        centros = mock(CentroTrabalhoRepository.class);
        svc = new CapacidadeServiceImpl(roteiros, ops, centros);
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
}
