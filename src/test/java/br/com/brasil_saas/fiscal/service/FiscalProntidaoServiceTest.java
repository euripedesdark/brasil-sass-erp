package br.com.brasil_saas.fiscal.service;

import br.com.brasil_saas.financeiro.service.BoletoService;
import br.com.brasil_saas.financeiro.service.StripeFinanceService;
import br.com.brasil_saas.fiscal.repository.ApuracaoRepository;
import br.com.brasil_saas.fiscal.repository.ReinfRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FiscalProntidaoServiceTest {

    @Mock CertificadoDigitalService certificadoDigitalService;
    @Mock RegraTributariaService regraTributariaService;
    @Mock ReinfRepository reinfRepository;
    @Mock ApuracaoRepository apuracaoRepository;
    @Mock StripeFinanceService stripeFinanceService;
    @Mock BoletoService boletoService;
    @InjectMocks FiscalProntidaoService svc;

    @Test
    void checklistRetornaItens() {
        when(certificadoDigitalService.certificadosSalvos(anyLong())).thenReturn(List.of());
        when(regraTributariaService.listar(anyLong())).thenReturn(List.of());
        when(reinfRepository.findByEmpresaIdOrderByCompetenciaDescGeradoAtDesc(anyLong())).thenReturn(List.of());
        when(apuracaoRepository.findByEmpresaIdOrderByCompetenciaDesc(anyLong())).thenReturn(List.of());
        when(stripeFinanceService.statusConfig(anyLong())).thenReturn(Map.of("habilitado", false, "motivo", "off"));
        when(boletoService.servicoDisponivel()).thenReturn(false);

        Map<String, Object> r = svc.checklist(1L);
        assertNotNull(r.get("itens"));
        assertEquals(6, ((List<?>) r.get("itens")).size());
        assertFalse(Boolean.TRUE.equals(r.get("prontoOperacional")));
    }
}
