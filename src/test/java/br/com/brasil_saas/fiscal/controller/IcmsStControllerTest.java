package br.com.brasil_saas.fiscal.controller;

import br.com.brasil_saas.fiscal.service.IcmsStService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class IcmsStControllerTest {
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new IcmsStController(new IcmsStService())).build();

    @Test
    void rejeitaCamposAusentesAntesDeCalcular() throws Exception {
        mvc.perform(post("/api/fiscal/icms-st/calcular").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content("{\"baseOperacao\":1000,\"aliqInterna\":18,\"mva\":40}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejeitaReducaoAcimaDeCem() throws Exception {
        mvc.perform(post("/api/fiscal/icms-st/calcular").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content("{\"baseOperacao\":1000,\"aliqInterestadual\":12,\"aliqInterna\":18,\"mva\":40,\"reducaoBasePct\":101}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void aceitaReducaoOpcionalEMantemContratoDoResultado() throws Exception {
        mvc.perform(post("/api/fiscal/icms-st/calcular").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .content("{\"baseOperacao\":1000,\"aliqInterestadual\":12,\"aliqInterna\":18,\"mva\":40}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.icmsSt").value(132.00))
                .andExpect(jsonPath("$.baseSt").value(1400.00));
    }
}
