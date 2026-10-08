package br.com.brasil_saas.compras.controller;

import br.com.brasil_saas.compras.model.ConferenciaFaturaCompra;
import br.com.brasil_saas.compras.service.ConferenciaFaturaCompraService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.*;
import java.math.BigDecimal;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ConferenciaFaturaCompraHttpTest {
    @Mock ConferenciaFaturaCompraService service;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.standaloneSetup(new ConferenciaFaturaCompraController(service))
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    public boolean supportsParameter(MethodParameter p) {
                        return p.getParameterType() == AuthenticatedUser.class;
                    }
                    public Object resolveArgument(MethodParameter p, ModelAndViewContainer m,
                            NativeWebRequest r, org.springframework.web.bind.support.WebDataBinderFactory b) {
                        return new AuthenticatedUser(1L, "teste", "senha", true, 2L, List.of());
                    }
                }).build();
    }

    @Test
    void postExecutaServicoComEmpresaDoPrincipal() throws Exception {
        var c = new ConferenciaFaturaCompra(); c.setId(7L); c.setStatus("DIVERGENTE");
        when(service.conferir(eq(2L), any())).thenReturn(c);
        mvc.perform(post("/api/compras/conferencia-faturas").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content("{\"pedidoId\":1,\"valorFatura\":10,\"recebimentoId\":4,\"nfeId\":5}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("DIVERGENTE"));
        verify(service).conferir(eq(2L), argThat(r -> r.pedidoId()==1L
                && r.valorFatura().compareTo(BigDecimal.TEN)==0 && r.recebimentoId()==4L && r.nfeId()==5L));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "{\"pedidoId\":1,\"valorFatura\":10,\"nfeId\":5}",
        "{\"pedidoId\":1,\"valorFatura\":10,\"recebimentoId\":4}",
        "{\"pedidoId\":1,\"valorFatura\":0,\"recebimentoId\":4,\"nfeId\":5}",
        "{\"pedidoId\":1,\"valorFatura\":10,\"recebimentoId\":4,\"nfeId\":5,\"tolerancia\":-1}",
        "{\"pedidoId\":-1,\"valorFatura\":10,\"recebimentoId\":4,\"nfeId\":5}"
    })
    void rejeitaEntradaInvalidaAntesDoServico(String json) throws Exception {
        mvc.perform(post("/api/compras/conferencia-faturas").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content(json)).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void itensUsamEmpresaDoPrincipal() throws Exception {
        when(service.listarItens(2L, 7L)).thenReturn(List.of());
        mvc.perform(get("/api/compras/conferencia-faturas/7/itens").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
        verify(service).listarItens(2L, 7L);
    }
}
