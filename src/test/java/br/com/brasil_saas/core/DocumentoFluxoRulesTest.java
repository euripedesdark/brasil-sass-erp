package br.com.brasil_saas.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DocumentoFluxoRulesTest {
    static String chave(String tipo, Long id) {
        return tipo + ":" + id;
    }

    @Test
    void chaveUnica() {
        assertEquals("PEDIDO_VENDA:10", chave("PEDIDO_VENDA", 10L));
        assertNotEquals(chave("ORCAMENTO", 1L), chave("PEDIDO_VENDA", 1L));
    }
}
