package br.com.brasil_saas.ativos.service;

import br.com.brasil_saas.ativos.model.*;
import br.com.brasil_saas.ativos.repository.*;
import br.com.brasil_saas.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ManutencaoServiceTest {

    private static final Long EMPRESA = 1L;

    private AtivoImobilizadoRepository ativos;
    private ManutencaoRepository ordens;
    private ManutencaoMaterialRepository materiais;
    private ManutencaoApontamentoRepository apontamentos;
    private PlanoManutencaoRepository planos;
    private NotaManutencaoRepository notas;
    private MedicaoAtivoRepository medicoes;
    private ManutencaoService svc;
    private AtivoImobilizado ativo;

    @BeforeEach
    void setUp() {
        ativos = mock(AtivoImobilizadoRepository.class);
        ordens = mock(ManutencaoRepository.class);
        materiais = mock(ManutencaoMaterialRepository.class);
        apontamentos = mock(ManutencaoApontamentoRepository.class);
        planos = mock(PlanoManutencaoRepository.class);
        notas = mock(NotaManutencaoRepository.class);
        medicoes = mock(MedicaoAtivoRepository.class);
        svc = new ManutencaoService(ativos, ordens, materiais, apontamentos, planos, notas, medicoes,
                org.mockito.Mockito.mock(br.com.brasil_saas.cadastro.repository.ProdutoRepository.class),
                org.mockito.Mockito.mock(br.com.brasil_saas.rh.repository.FuncionarioRepository.class));
        ativo = new AtivoImobilizado();
        ativo.setId(5L);
        ativo.setEmpresaId(EMPRESA);
        ativo.setStatus("ATIVO");
        when(ativos.findById(5L)).thenReturn(Optional.of(ativo));
        when(ativos.save(any())).thenAnswer(i -> i.getArgument(0));
        when(ordens.save(any())).thenAnswer(i -> i.getArgument(0));
        when(materiais.save(any())).thenAnswer(i -> i.getArgument(0));
        when(apontamentos.save(any())).thenAnswer(i -> i.getArgument(0));
        when(planos.save(any())).thenAnswer(i -> i.getArgument(0));
        when(notas.save(any())).thenAnswer(i -> i.getArgument(0));
        when(medicoes.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    private Manutencao ordem(String status) {
        Manutencao m = new Manutencao();
        m.setId(9L);
        m.setEmpresaId(EMPRESA);
        m.setAtivoId(5L);
        m.setNumero("OM-000001");
        m.setDescricao("Troca de rolamento");
        m.setStatus(status);
        when(ordens.findById(9L)).thenReturn(Optional.of(m));
        return m;
    }

    @Test
    void numeracaoContinuaDoMaiorNumeroNoPadrao() {
        assertEquals("OM-000001", ManutencaoService.proximoNumero("OM", List.of()));
        assertEquals("OM-000013", ManutencaoService.proximoNumero("OM", List.of("OM-000012", "MAN-77", "OM-000003")));
    }

    @Test
    void criarOrdemNumeraEValidaAtivo() {
        when(ordens.findAllByEmpresaIdAndDeletedAtIsNullOrderByDataProgramadaAsc(EMPRESA)).thenReturn(List.of());
        Manutencao m = new Manutencao();
        m.setAtivoId(5L);
        m.setDescricao("Vazamento");
        Manutencao criada = svc.criarOrdem(EMPRESA, m);
        assertEquals("OM-000001", criada.getNumero());
        assertEquals("ABERTA", criada.getStatus());

        ativo.setStatus("BAIXADO");
        Manutencao outra = new Manutencao();
        outra.setAtivoId(5L);
        outra.setDescricao("x");
        assertThrows(BusinessException.class, () -> svc.criarOrdem(EMPRESA, outra));
    }

    @Test
    void materiaisEHorasCompoemOCustoDaOrdem() {
        Manutencao m = ordem("LIBERADA");
        m.setCustoServico(new BigDecimal("50"));
        ManutencaoMaterial mat = new ManutencaoMaterial();
        mat.setDescricao("Rolamento");
        mat.setQuantidade(new BigDecimal("2"));
        mat.setCustoUnitario(new BigDecimal("35.50"));
        when(materiais.findAllByEmpresaIdAndManutencaoIdAndDeletedAtIsNullOrderById(EMPRESA, 9L)).thenAnswer(i -> List.of(mat));
        ManutencaoApontamento ap = new ManutencaoApontamento();
        ap.setHoras(new BigDecimal("3"));
        ap.setCustoHora(new BigDecimal("40"));
        when(apontamentos.findAllByEmpresaIdAndManutencaoIdAndDeletedAtIsNullOrderByDataApontamentoAscIdAsc(EMPRESA, 9L)).thenReturn(List.of());

        svc.adicionarMaterial(EMPRESA, 9L, mat);
        assertEquals(0, new BigDecimal("71.00").compareTo(mat.getCustoTotal()));
        assertEquals(0, new BigDecimal("121.00").compareTo(m.getCusto()));

        when(apontamentos.findAllByEmpresaIdAndManutencaoIdAndDeletedAtIsNullOrderByDataApontamentoAscIdAsc(EMPRESA, 9L)).thenReturn(List.of(ap));
        svc.apontar(EMPRESA, 9L, ap);
        assertEquals("EM_EXECUCAO", m.getStatus());
        assertEquals(0, new BigDecimal("3").compareTo(m.getHorasTrabalhadas()));
        assertEquals(0, new BigDecimal("241.00").compareTo(m.getCusto()));
    }

    @Test
    void ordemConcluidaNaoAceitaMaterial() {
        ordem("CONCLUIDA");
        ManutencaoMaterial mat = new ManutencaoMaterial();
        mat.setDescricao("x");
        mat.setQuantidade(BigDecimal.ONE);
        assertThrows(BusinessException.class, () -> svc.adicionarMaterial(EMPRESA, 9L, mat));
    }

    @Test
    void concluirOrdemDePlanoReprogramaOProximoCiclo() {
        Manutencao m = ordem("EM_EXECUCAO");
        m.setPlanoId(3L);
        PlanoManutencao p = new PlanoManutencao();
        p.setId(3L);
        p.setEmpresaId(EMPRESA);
        p.setAtivoId(5L);
        p.setIntervaloDias(30);
        when(planos.findById(3L)).thenReturn(Optional.of(p));
        ativo.setContadorAtual(new BigDecimal("1200"));

        svc.concluir(EMPRESA, 9L, new ManutencaoService.ConclusaoReq(LocalDate.of(2026, 3, 1), "desgaste", "troca", null, null));

        assertEquals("CONCLUIDA", m.getStatus());
        assertEquals(LocalDate.of(2026, 3, 31), p.getProximaData());
        assertEquals(0, new BigDecimal("1200").compareTo(p.getContadorUltimaExecucao()));
    }

    @Test
    void planoPorTempoVenceConsiderandoAntecedencia() {
        PlanoManutencao p = new PlanoManutencao();
        p.setTipoCiclo("TEMPO");
        p.setProximaData(LocalDate.of(2026, 3, 10));
        p.setAntecedenciaDias(5);
        assertFalse(ManutencaoService.vencido(p, ativo, LocalDate.of(2026, 3, 4)));
        assertTrue(ManutencaoService.vencido(p, ativo, LocalDate.of(2026, 3, 5)));
    }

    @Test
    void planoPorContadorVenceAoAtingirOIntervalo() {
        PlanoManutencao p = new PlanoManutencao();
        p.setTipoCiclo("CONTADOR");
        p.setIntervaloContador(new BigDecimal("500"));
        p.setContadorUltimaExecucao(new BigDecimal("1000"));
        ativo.setContadorAtual(new BigDecimal("1499"));
        assertFalse(ManutencaoService.vencido(p, ativo, LocalDate.now()));
        ativo.setContadorAtual(new BigDecimal("1500"));
        assertTrue(ManutencaoService.vencido(p, ativo, LocalDate.now()));
    }

    @Test
    void medicaoNaoPodeRetrocederOContador() {
        ativo.setContadorAtual(new BigDecimal("100"));
        MedicaoAtivo med = new MedicaoAtivo();
        med.setAtivoId(5L);
        med.setValor(new BigDecimal("90"));
        assertThrows(BusinessException.class, () -> svc.registrarMedicao(EMPRESA, med));
        med.setValor(new BigDecimal("150"));
        svc.registrarMedicao(EMPRESA, med);
        assertEquals(0, new BigDecimal("150").compareTo(ativo.getContadorAtual()));
    }

    @Test
    void notaDeAvariaViraOrdemCorretiva() {
        NotaManutencao n = new NotaManutencao();
        n.setId(4L);
        n.setEmpresaId(EMPRESA);
        n.setAtivoId(5L);
        n.setNumero("NM-000001");
        n.setTipo("AVARIA");
        n.setPrioridade("ALTA");
        n.setStatus("ABERTA");
        n.setDescricao("Motor parou");
        when(notas.findById(4L)).thenReturn(Optional.of(n));
        when(ordens.findAllByEmpresaIdAndDeletedAtIsNullOrderByDataProgramadaAsc(EMPRESA)).thenReturn(List.of());

        Manutencao m = svc.gerarOrdemDaNota(EMPRESA, 4L);

        assertEquals("CORRETIVA", m.getTipo());
        assertEquals("ALTA", m.getPrioridade());
        assertEquals(4L, m.getNotaId());
        assertEquals("EM_PROCESSAMENTO", n.getStatus());
        assertThrows(BusinessException.class, () -> svc.gerarOrdemDaNota(EMPRESA, 4L));
    }
}
