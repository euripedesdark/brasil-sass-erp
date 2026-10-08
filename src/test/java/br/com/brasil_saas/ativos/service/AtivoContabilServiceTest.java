package br.com.brasil_saas.ativos.service;

import br.com.brasil_saas.ativos.model.*;
import br.com.brasil_saas.ativos.repository.*;
import br.com.brasil_saas.contabilidade.model.CtbLancamento;
import br.com.brasil_saas.contabilidade.model.CtbPartida;
import br.com.brasil_saas.contabilidade.service.ContabilidadeService;
import br.com.brasil_saas.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AtivoContabilServiceTest {

    private static final Long EMPRESA = 1L;

    private AtivoImobilizadoRepository ativos;
    private ClasseAtivoRepository classes;
    private AtivoMovimentoRepository movimentos;
    private DepreciacaoExecucaoRepository execucoes;
    private ContabilidadeService contabilidade;
    private AtivoContabilService svc;
    private final Map<Long, AtivoImobilizado> banco = new HashMap<>();

    @BeforeEach
    void setUp() {
        ativos = mock(AtivoImobilizadoRepository.class);
        classes = mock(ClasseAtivoRepository.class);
        movimentos = mock(AtivoMovimentoRepository.class);
        execucoes = mock(DepreciacaoExecucaoRepository.class);
        contabilidade = mock(ContabilidadeService.class);
        svc = new AtivoContabilService(ativos, classes, movimentos, execucoes, contabilidade);
        when(ativos.findById(anyLong())).thenAnswer(i -> Optional.ofNullable(banco.get((Long) i.getArgument(0))));
        when(ativos.findAllByEmpresaIdAndDeletedAtIsNullOrderByCodigo(EMPRESA)).thenAnswer(i -> new ArrayList<>(banco.values()));
        when(ativos.save(any())).thenAnswer(i -> i.getArgument(0));
        when(movimentos.save(any())).thenAnswer(i -> i.getArgument(0));
        when(execucoes.save(any())).thenAnswer(i -> {
            DepreciacaoExecucao e = i.getArgument(0);
            if (e.getId() == null) e.setId(99L);
            return e;
        });
        when(contabilidade.salvar(eq(EMPRESA), any())).thenAnswer(i -> {
            CtbLancamento l = i.getArgument(1);
            l.setId(500L);
            return l;
        });
        when(contabilidade.lancar(EMPRESA, 500L)).thenAnswer(i -> {
            CtbLancamento l = new CtbLancamento();
            l.setId(500L);
            return l;
        });
    }

    private AtivoImobilizado ativo(Long id, String aquisicao, String depreciado, Long classeId) {
        AtivoImobilizado a = new AtivoImobilizado();
        a.setId(id);
        a.setEmpresaId(EMPRESA);
        a.setCodigo("AT-" + id);
        a.setDescricao("Ativo " + id);
        a.setValorAquisicao(new BigDecimal(aquisicao));
        a.setValorDepreciado(new BigDecimal(depreciado));
        a.setValorResidual(BigDecimal.ZERO);
        a.setVidaUtilMeses(100);
        a.setDataAquisicao(LocalDate.of(2026, 1, 10));
        a.setClasseId(classeId);
        a.setStatus("ATIVO");
        banco.put(id, a);
        return a;
    }

    private ClasseAtivo classeComContas() {
        ClasseAtivo c = new ClasseAtivo();
        c.setId(7L);
        c.setEmpresaId(EMPRESA);
        c.setCodigo("MAQ");
        c.setDescricao("Máquinas");
        c.setContaAtivoId(10L);
        c.setContaDepreciacaoAcumuladaId(11L);
        c.setContaDespesaDepreciacaoId(12L);
        c.setContaGanhoBaixaId(13L);
        c.setContaPerdaBaixaId(14L);
        when(classes.findById(7L)).thenReturn(Optional.of(c));
        return c;
    }

    @Test
    void baixaParcialReduzValoresProporcionalmenteEApuraGanho() {
        AtivoImobilizado a = ativo(1L, "10000", "4000", null);

        AtivoMovimento m = svc.baixar(EMPRESA, 3L, 1L, new AtivoContabilService.BaixaReq(LocalDate.of(2026, 6, 1), new BigDecimal("50"), new BigDecimal("4000"), null, "venda"));

        assertEquals("BAIXA_PARCIAL", m.getTipo());
        assertEquals(0, new BigDecimal("1000").compareTo(m.getResultado()));
        assertEquals(0, new BigDecimal("5000").compareTo(a.getValorAquisicao()));
        assertEquals(0, new BigDecimal("2000").compareTo(a.getValorDepreciado()));
        assertEquals("ATIVO", a.getStatus());
        verifyNoInteractions(contabilidade);
    }

    @Test
    void baixaTotalComClasseContabilizaBaixaVendaEGanho() {
        classeComContas();
        AtivoImobilizado a = ativo(1L, "10000", "4000", 7L);

        AtivoMovimento m = svc.baixar(EMPRESA, 3L, 1L, new AtivoContabilService.BaixaReq(LocalDate.of(2026, 6, 1), null, new BigDecimal("7000"), 20L, "venda"));

        assertEquals("BAIXADO", a.getStatus());
        assertEquals(500L, m.getLancamentoId());
        ArgumentCaptor<CtbPartida> partidas = ArgumentCaptor.forClass(CtbPartida.class);
        verify(contabilidade, times(4)).addPartida(eq(EMPRESA), eq(500L), partidas.capture());
        BigDecimal debitos = partidas.getAllValues().stream().map(CtbPartida::getDebito).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal creditos = partidas.getAllValues().stream().map(CtbPartida::getCredito).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, debitos.compareTo(creditos));
        assertTrue(partidas.getAllValues().stream().anyMatch(p -> p.getContaId() == 13L && p.getCredito().compareTo(new BigDecimal("1000")) == 0));
    }

    @Test
    void naoBaixaAtivoJaBaixado() {
        ativo(1L, "10000", "0", null).setStatus("BAIXADO");
        assertThrows(BusinessException.class, () -> svc.baixar(EMPRESA, 3L, 1L, null));
    }

    @Test
    void ativoDeOutraEmpresaNaoEEncontrado() {
        ativo(1L, "10000", "0", null).setEmpresaId(2L);
        assertThrows(NoSuchElementException.class, () -> svc.baixar(EMPRESA, 3L, 1L, null));
    }

    @Test
    void impairmentNaoPodePassarDoValorContabil() {
        ativo(1L, "10000", "9000", null);
        assertThrows(BusinessException.class, () -> svc.impairment(EMPRESA, 3L, 1L, new AtivoContabilService.ValorReq(null, new BigDecimal("1500"), null)));
    }

    @Test
    void execucaoMensalDepreciaTodosEContabilizaSoQuemTemContas() {
        classeComContas();
        AtivoImobilizado comClasse = ativo(1L, "10000", "0", 7L);
        AtivoImobilizado semClasse = ativo(2L, "5000", "0", null);
        when(execucoes.findAllByEmpresaIdAndDeletedAtIsNullOrderByPeriodoDescIdDesc(EMPRESA)).thenReturn(List.of());

        DepreciacaoExecucao e = svc.executar(EMPRESA, 3L, YearMonth.of(2026, 1));

        assertEquals(2, e.getQuantidadeAtivos());
        assertEquals(0, new BigDecimal("150.00").compareTo(e.getValorTotal()));
        assertEquals(0, new BigDecimal("100.00").compareTo(e.getValorContabilizado()));
        assertEquals(500L, e.getLancamentoId());
        assertEquals("2026-01", comClasse.getUltimoPeriodoDepreciado());
        assertEquals(0, new BigDecimal("50.00").compareTo(semClasse.getValorDepreciado()));
        verify(contabilidade, times(2)).addPartida(eq(EMPRESA), eq(500L), any());
    }

    @Test
    void naoExecutaOMesmoPeriodoDuasVezes() {
        ativo(1L, "10000", "0", null);
        DepreciacaoExecucao anterior = new DepreciacaoExecucao();
        anterior.setPeriodo("2026-02");
        anterior.setStatus("EFETIVADA");
        when(execucoes.findAllByEmpresaIdAndDeletedAtIsNullOrderByPeriodoDescIdDesc(EMPRESA)).thenReturn(List.of(anterior));
        assertThrows(BusinessException.class, () -> svc.executar(EMPRESA, 3L, YearMonth.of(2026, 2)));
    }

    @Test
    void simulacaoIgnoraPeriodoJaDepreciado() {
        ativo(1L, "10000", "100", null).setUltimoPeriodoDepreciado("2026-02");
        assertTrue(svc.simular(EMPRESA, YearMonth.of(2026, 2)).isEmpty());
        assertEquals(1, svc.simular(EMPRESA, YearMonth.of(2026, 3)).size());
    }

    @Test
    void estornoDevolveADepreciacaoEReabreOPeriodo() {
        AtivoImobilizado a = ativo(1L, "10000", "200", null);
        a.setUltimoPeriodoDepreciado("2026-02");
        DepreciacaoExecucao e = new DepreciacaoExecucao();
        e.setId(99L);
        e.setEmpresaId(EMPRESA);
        e.setPeriodo("2026-02");
        e.setStatus("EFETIVADA");
        AtivoMovimento m = new AtivoMovimento();
        m.setAtivoId(1L);
        m.setValor(new BigDecimal("100"));
        m.setStatus("ATIVO");
        when(execucoes.findById(99L)).thenReturn(Optional.of(e));
        when(execucoes.findAllByEmpresaIdAndDeletedAtIsNullOrderByPeriodoDescIdDesc(EMPRESA)).thenReturn(List.of(e));
        when(movimentos.findAllByExecucaoIdAndDeletedAtIsNull(99L)).thenReturn(List.of(m));

        svc.estornar(EMPRESA, 99L);

        assertEquals("ESTORNADA", e.getStatus());
        assertEquals("ESTORNADO", m.getStatus());
        assertEquals("2026-01", a.getUltimoPeriodoDepreciado());
        assertEquals(0, new BigDecimal("100").compareTo(a.getValorDepreciado()));
    }

    @Test
    void transferenciaRegistraOrigemEDestino() {
        AtivoImobilizado a = ativo(1L, "10000", "0", null);
        a.setCentroCustoId(1L);
        a.setLocalizacao("Galpão A");

        AtivoMovimento m = svc.transferir(EMPRESA, 3L, 1L, new AtivoContabilService.TransferenciaReq(null, 2L, "Galpão B", null, null));

        assertEquals(1L, m.getCentroCustoOrigemId());
        assertEquals(2L, m.getCentroCustoDestinoId());
        assertEquals("Galpão B", a.getLocalizacao());
    }
}
