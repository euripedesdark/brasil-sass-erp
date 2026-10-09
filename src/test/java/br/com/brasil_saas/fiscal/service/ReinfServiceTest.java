package br.com.brasil_saas.fiscal.service;

import br.com.brasil_saas.fiscal.model.Nfse;
import br.com.brasil_saas.fiscal.model.Reinf;
import br.com.brasil_saas.fiscal.repository.NfseRepository;
import br.com.brasil_saas.fiscal.repository.ReinfRepository;
import br.com.brasil_saas.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReinfServiceTest {

    @Mock ReinfRepository reinfRepo;
    @Mock NfseRepository nfses;
    @InjectMocks ReinfService svc;

    @Test
    void geraR2020ComNfseDeSaida() {
        empresaExistente();
        Nfse n = nfse("S", "1000.00", "50.00");
        when(nfses.findNoPeriodo(eq(1L), any(), any()))
                .thenReturn(List.of(n));
        when(reinfRepo.findByEmpresaIdAndCompetenciaAndEvento(eq(1L), eq("03/2026"), anyString()))
                .thenReturn(Optional.empty());
        when(reinfRepo.save(any())).thenAnswer(inv -> {
            Reinf r = inv.getArgument(0);
            if (r.getId() == null) r.setId(10L);
            return r;
        });

        List<Reinf> out = svc.gerarPeriodo(1L, "03/2026");

        assertEquals(2, out.size());
        Reinf r2020 = out.stream().filter(e -> "R-2020".equals(e.getEvento())).findFirst().orElseThrow();
        assertEquals(Reinf.GERADO, r2020.getStatus());
        assertEquals(1, r2020.getTotalDocs());
        assertEquals(new BigDecimal("1000.00"), r2020.getValorTotal());
        assertNotNull(r2020.getPayload());
        assertTrue(r2020.getPayload().contains("R-2020"));
        assertTrue(r2020.getProtocolo().startsWith("LOCAL-"));
    }

    @Test
    void payloadComQuebraDeLinhaPermaneceJsonValido() throws Exception {
        empresaExistente();
        Nfse n = nfse("S", "100.00", "5.00");
        n.setSerieRps("A\n\tB");
        when(nfses.findNoPeriodo(eq(1L), any(), any())).thenReturn(List.of(n));
        when(reinfRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        var eventos = svc.gerarPeriodo(1L, "03/2026");
        var json = new com.fasterxml.jackson.databind.ObjectMapper().readTree(eventos.get(0).getPayload());
        assertEquals("A\n\tB", json.get("nfs").get(0).get("serieRps").asText());
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"3/2026", "03/2026/extra", "03/26", "00/2026", "13/2026"})
    void competenciaDeveSerExatamenteMesEAno(String competencia) {
        assertThrows(BusinessException.class, () -> svc.gerarPeriodo(1L, competencia));
        verifyNoInteractions(nfses, reinfRepo);
    }

    @Test
    void fecharSemEventosFalha() {
        empresaExistente();
        when(reinfRepo.findByEmpresaIdAndCompetencia(1L, "03/2026")).thenReturn(List.of());
        assertThrows(BusinessException.class, () -> svc.fechar(1L, "03/2026"));
    }

    @Test
    void competenciaInvalida() {
        assertThrows(BusinessException.class, () -> svc.gerarPeriodo(1L, "2026-03"));
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"DIGITADA", "EMITINDO", "FALHA_EMISSAO", "REJEITADA", "CANCELADA"})
    void naoIncluiNotasSemEmissaoConfirmada(String status) {
        empresaExistente();
        Nfse n = nfse("S", "100.00", "5.00");
        n.setStatus(status);
        when(nfses.findNoPeriodo(eq(1L), any(), any())).thenReturn(List.of(n));
        when(reinfRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        assertTrue(svc.gerarPeriodo(1L, "03/2026").stream().allMatch(e -> e.getTotalDocs() == 0));
    }

    @Test
    void naoRegeneraCompetenciaFechada() {
        empresaExistente();
        Reinf fechado = new Reinf();
        fechado.setEvento("R-2099");
        fechado.setStatus(Reinf.FECHADO);
        when(reinfRepo.findByEmpresaIdAndCompetencia(1L, "03/2026")).thenReturn(List.of(fechado));
        assertThrows(BusinessException.class, () -> svc.gerarPeriodo(1L, "03/2026"));
        verifyNoInteractions(nfses);
        verify(reinfRepo, never()).save(any());
    }

    @Test
    void repetirFechamentoRetornaMesmoEvento() {
        empresaExistente();
        Reinf fechado = new Reinf();
        fechado.setEvento("R-2099");
        fechado.setStatus(Reinf.FECHADO);
        when(reinfRepo.findByEmpresaIdAndCompetencia(1L, "03/2026")).thenReturn(List.of(fechado));
        assertSame(fechado, svc.fechar(1L, "03/2026"));
        verify(reinfRepo, never()).save(any());
    }

    @Test
    void detalheConsultaEmpresaEExclusaoLogica() {
        assertThrows(br.com.brasil_saas.shared.exception.ResourceNotFoundException.class, () -> svc.detalhe(1L, 99L));
        verify(reinfRepo).findByIdAndEmpresaIdAndDeletedAtIsNull(99L, 1L);
        verify(reinfRepo, never()).findById(anyLong());
    }

    private void empresaExistente() {
        when(reinfRepo.bloquearEmpresa(1L)).thenReturn(Optional.of(new br.com.brasil_saas.core.model.Empresa()));
    }

    private Nfse nfse(String tipo, String total, String iss) {
        Nfse n = new Nfse();
        n.setId(1L);
        n.setTipoOperacao(tipo);
        n.setStatus("AUTORIZADA");
        n.setValorTotal(new BigDecimal(total));
        n.setBaseCalculo(new BigDecimal(total));
        n.setValorIss(new BigDecimal(iss));
        n.setAliquotaIss(new BigDecimal("5.00"));
        n.setDataEmissao(LocalDateTime.of(2026, 3, 10, 12, 0));
        n.setNumero(100L);
        return n;
    }
}
