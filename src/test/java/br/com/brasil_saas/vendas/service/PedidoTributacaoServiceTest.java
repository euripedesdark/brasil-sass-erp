package br.com.brasil_saas.vendas.service;

import br.com.brasil_saas.cadastro.repository.ProdutoRepository;
import br.com.brasil_saas.core.model.Empresa;
import br.com.brasil_saas.core.repository.EmpresaRepository;
import br.com.brasil_saas.fiscal.service.TributacaoSimuladorService;
import br.com.brasil_saas.vendas.dto.ItemPedidoVendaRequest;
import br.com.brasil_saas.vendas.dto.PedidoVendaRequest;
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
class PedidoTributacaoServiceTest {

    @Mock TributacaoSimuladorService tributacao;
    @Mock ProdutoRepository produtoRepository;
    @Mock EmpresaRepository empresaRepository;
    @InjectMocks PedidoTributacaoService svc;

    @Test
    void preverDelegaAoSimulador() {
        Empresa e = new Empresa();
        e.setId(1L);
        e.setUf("SP");
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(e));
        when(tributacao.simularItens(any(), any(), any(), any(), any(), any()))
                .thenReturn(Map.of("totalImpostos", new BigDecimal("10.00"), "linhas", List.of()));
        PedidoVendaRequest req = new PedidoVendaRequest(
                1L, 2L, null, null, null, null, null, null, null, null, null, null, null,
                null, null,
                List.of(new ItemPedidoVendaRequest(1, 99L, null, "x", new BigDecimal("2"), "UN", new BigDecimal("50"), null))
        );
        Map<String, Object> r = svc.prever(1L, req, "RJ", true, false);
        assertEquals("SP", r.get("ufOrigem"));
        assertEquals("RJ", r.get("ufDestino"));
        assertEquals(new BigDecimal("10.00"), r.get("totalImpostos"));
    }
}
