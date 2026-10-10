package br.com.brasil_saas.plm.service.impl;

import br.com.brasil_saas.core.service.DocumentoFluxoService;
import br.com.brasil_saas.plm.model.*;
import br.com.brasil_saas.plm.repository.*;
import br.com.brasil_saas.plm.service.PlmService;
import br.com.brasil_saas.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlmServiceImplTest {
    @Mock PlmMudancaRepository mudancas;
    @Mock PlmEfeitoRepository efeitos;
    @Mock PlmAprovacaoRepository aprovacoes;
    @Mock PlmRevisaoRepository revisoes;
    @Mock DocumentoFluxoService documentoFluxo;
    @InjectMocks PlmServiceImpl service;

    private PlmMudanca mudanca(String status) {
        var m = new PlmMudanca();
        m.setId(11L); m.setEmpresaId(2L); m.setNumero("ECO-1");
        m.setStatus(status);
        return m;
    }

    @Test
    void criarAplicaPadroes() {
        when(mudancas.save(any())).thenAnswer(i -> i.getArgument(0));
        var m = service.criarMudanca(2L, 7L,
                new PlmService.MudancaRequest("ECO-1", null, "Troca de fornecedor", null, null));
        assertEquals("ENGENHARIA", m.getTipo());
        assertEquals("NORMAL", m.getPrioridade());
        assertEquals("ABERTA", m.getStatus());
        assertEquals(7L, m.getSolicitanteId());
    }

    @Test
    void enviarExigeEfeitos() {
        when(mudancas.findByIdForUpdate(11L, 2L)).thenReturn(Optional.of(mudanca("ABERTA")));
        when(efeitos.findByEmpresaIdAndMudancaIdOrderByOrdemExecucaoAscIdAsc(2L, 11L))
                .thenReturn(List.of());
        assertThrows(BusinessException.class, () -> service.enviarAprovacao(2L, 7L, 11L, 9L));
        verify(mudancas, never()).save(any());
    }

    @Test
    void decidirAprovaMudaStatus() {
        when(mudancas.findByIdForUpdate(11L, 2L)).thenReturn(Optional.of(mudanca("EM_APROVACAO")));
        var etapa = new PlmAprovacao();
        etapa.setDecisao("PENDENTE");
        when(aprovacoes.findByEmpresaIdAndMudancaIdAndEtapa(2L, 11L, 1)).thenReturn(Optional.of(etapa));
        when(mudancas.save(any())).thenAnswer(i -> i.getArgument(0));
        var m = service.decidirEtapa(2L, 7L, 11L, true, "Justificativa tecnica detalhada");
        assertEquals("APROVADA", m.getStatus());
        assertEquals("APROVADA", etapa.getDecisao());
        assertNotNull(etapa.getDecididoEm());
        verify(documentoFluxo).ligar(eq(2L), eq(7L), eq("MUDANCA_PLM"), eq(11L), any(),
                eq("MUDANCA_PLM"), eq(11L), any(), eq("APROVADA"));
    }

    @Test
    void decidirRejeitadaNaoImplementa() {
        when(mudancas.findByIdForUpdate(11L, 2L)).thenReturn(Optional.of(mudanca("EM_APROVACAO")));
        var etapa = new PlmAprovacao();
        etapa.setDecisao("PENDENTE");
        when(aprovacoes.findByEmpresaIdAndMudancaIdAndEtapa(2L, 11L, 1)).thenReturn(Optional.of(etapa));
        when(mudancas.save(any())).thenAnswer(i -> i.getArgument(0));
        var m = service.decidirEtapa(2L, 7L, 11L, false, "Justificativa tecnica detalhada");
        assertEquals("REJEITADA", m.getStatus());
        assertThrows(BusinessException.class, () -> service.implementar(2L, 7L, 11L));
    }

    @Test
    void implementarAplicaEfeitosEmOrdem() {
        var m = mudanca("APROVADA");
        when(mudancas.findByIdForUpdate(11L, 2L)).thenReturn(Optional.of(m));
        var e1 = new PlmEfeito();
        e1.setId(21L); e1.setStatus("PENDENTE"); e1.setAcao("REGISTRAR"); e1.setOrdemExecucao(2);
        var e2 = new PlmEfeito();
        e2.setId(22L); e2.setStatus("PENDENTE"); e2.setAcao("REGISTRAR"); e2.setOrdemExecucao(1);
        when(efeitos.findByEmpresaIdAndMudancaIdOrderByOrdemExecucaoAscIdAsc(2L, 11L))
                .thenReturn(List.of(e2, e1));
        when(mudancas.save(any())).thenAnswer(i -> i.getArgument(0));
        var r = service.implementar(2L, 7L, 11L);
        assertEquals("IMPLEMENTADA", r.getStatus());
        assertNotNull(r.getImplementadoEm());
        assertEquals("APLICADA", e1.getStatus());
        assertEquals("APLICADA", e2.getStatus());
        verify(efeitos, times(2)).save(any());
    }

    @Test
    void implementarIdempotente() {
        when(mudancas.findByIdForUpdate(11L, 2L)).thenReturn(Optional.of(mudanca("IMPLEMENTADA")));
        assertEquals("IMPLEMENTADA", service.implementar(2L, 7L, 11L).getStatus());
        verifyNoInteractions(efeitos, aprovacoes, documentoFluxo);
        verify(mudancas, never()).save(any());
    }

    @Test
    void vigorarSubstituiAnterior() {
        var atual = new PlmRevisao();
        atual.setId(31L); atual.setEmpresaId(2L); atual.setProdutoId(6L);
        atual.setStatus("EM_DESENVOLVIMENTO");
        var anterior = new PlmRevisao();
        anterior.setId(30L); anterior.setStatus("VIGENTE");
        when(revisoes.findByIdForUpdate(31L, 2L)).thenReturn(Optional.of(atual));
        when(revisoes.findByEmpresaIdAndProdutoIdOrderByIdDesc(2L, 6L))
                .thenReturn(List.of(atual, anterior));
        when(revisoes.save(any())).thenAnswer(i -> i.getArgument(0));
        var r = service.vigorarRevisao(2L, 7L, 31L);
        assertEquals("VIGENTE", r.getStatus());
        assertEquals("SUBSTITUIDA", anterior.getStatus());
        assertNotNull(anterior.getVigenteAte());
    }

    @Test
    void efeitoComAcaoDesconhecidaRejeitadoNaAdicao() {
        var req = new PlmService.EfeitoRequest(11L, "PRODUTO", 6L, "X", null, null, null, null, null);
        var aberta = mudanca("ABERTA");
        when(mudancas.findByIdAndEmpresaIdAndDeletedAtIsNull(11L, 2L)).thenReturn(Optional.of(aberta));
        assertThrows(BusinessException.class, () -> service.adicionarEfeito(2L, req));
        verify(efeitos, never()).save(any());
    }
}
