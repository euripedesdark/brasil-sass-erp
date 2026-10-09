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
        Nfse n = nfse("S", "1000.00", "50.00");
        when(nfses.findByEmpresaIdAndDataEmissaoBetweenAndDeletedAtIsNull(eq(1L), any(), any()))
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
    void fecharSemEventosFalha() {
        when(reinfRepo.findByEmpresaIdAndCompetencia(1L, "03/2026")).thenReturn(List.of());
        assertThrows(BusinessException.class, () -> svc.fechar(1L, "03/2026"));
    }

    @Test
    void competenciaInvalida() {
        assertThrows(BusinessException.class, () -> svc.gerarPeriodo(1L, "2026-03"));
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
