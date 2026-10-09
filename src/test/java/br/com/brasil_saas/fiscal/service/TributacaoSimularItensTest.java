package br.com.brasil_saas.fiscal.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TributacaoSimularItensTest {

    @Mock RegraTributariaService regras;
    @Mock DifalService difalService;
    @Mock IcmsStService icmsStService;
    @InjectMocks TributacaoSimuladorService svc;

    @Test
    void loteSomaTotaisSemRegra() {
        when(regras.resolverOpcional(any(), any(), any(), any(), any())).thenReturn(Optional.empty());
        Map<String, Object> r = svc.simularItens(1L, "SP", "RJ", true, false, List.of(
                Map.of("ref", "A", "base", new BigDecimal("100"), "ncm", "1234", "cfop", "6102"),
                Map.of("ref", "B", "base", new BigDecimal("50"), "ncm", "1234", "cfop", "6102")
        ));
        assertEquals(2, ((List<?>) r.get("linhas")).size());
        assertNotNull(r.get("totalImpostos"));
    }
}
