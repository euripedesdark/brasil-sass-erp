package br.com.brasil_saas.fiscal.service;

import br.com.brasil_saas.fiscal.model.RegraTributaria;
import br.com.brasil_saas.fiscal.repository.RegraTributariaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TributacaoSimuladorServiceTest {

    @Mock RegraTributariaRepository repo;
    @Mock RegraTributariaService regras;
    @Mock DifalService difalService;
    @Mock IcmsStService icmsStService;
    @InjectMocks TributacaoSimuladorService svc;

    @Test
    void semRegra() {
        when(regras.resolverOpcional(any(), any(), any(), any(), any())).thenReturn(Optional.empty());
        Map<String, Object> r = svc.simular(1L, new BigDecimal("100"), "1234", "5102", "SP", "RJ", true, false);
        assertEquals(false, r.get("regraEncontrada"));
    }

    @Test
    void comRegraBasica() {
        RegraTributaria rt = new RegraTributaria();
        rt.setId(9L);
        rt.setNome("Teste");
        rt.setAliquotaIcms(new BigDecimal("18"));
        rt.setAliquotaPis(new BigDecimal("1.65"));
        rt.setAliquotaCofins(new BigDecimal("7.6"));
        when(regras.resolverOpcional(any(), any(), any(), any(), any())).thenReturn(Optional.of(rt));
        Map<String, Object> r = svc.simular(1L, new BigDecimal("1000"), "1234", "5102", "SP", "SP", false, true);
        assertEquals(true, r.get("regraEncontrada"));
        assertEquals(new BigDecimal("180.00"), r.get("icms"));
    }
}
