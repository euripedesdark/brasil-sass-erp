package br.com.brasil_saas.producao;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ApontamentoIniciaOpRulesTest {
    static String aposApontar(String statusOp) {
        if (!"ABERTO".equals(statusOp) && !"EM_PROCESSO".equals(statusOp))
            throw new IllegalStateException("OP fechada");
        return "ABERTO".equals(statusOp) ? "EM_PROCESSO" : statusOp;
    }

    @Test
    void abertoViraEmProcesso() {
        assertEquals("EM_PROCESSO", aposApontar("ABERTO"));
        assertEquals("EM_PROCESSO", aposApontar("EM_PROCESSO"));
    }

    @Test
    void finalizadoBloqueia() {
        assertThrows(IllegalStateException.class, () -> aposApontar("FINALIZADO"));
        assertThrows(IllegalStateException.class, () -> aposApontar("CANCELADO"));
    }
}
