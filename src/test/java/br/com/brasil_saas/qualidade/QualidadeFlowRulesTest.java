package br.com.brasil_saas.qualidade;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class QualidadeFlowRulesTest {
    private static boolean podeConcluir(String status) {
        return "ABERTA".equals(status);
    }
    private static boolean resultadoValido(String r) {
        return "APROVADO".equals(r) || "REPROVADO".equals(r);
    }
    private static boolean podeEncerrarNc(String status, String acaoCorretiva) {
        return !"ENCERRADA".equals(status) && acaoCorretiva != null && !acaoCorretiva.isBlank();
    }

    @Test
    void inspecaoSoAberta() {
        assertTrue(podeConcluir("ABERTA"));
        assertFalse(podeConcluir("CONCLUIDA"));
    }

    @Test
    void resultados() {
        assertTrue(resultadoValido("APROVADO"));
        assertTrue(resultadoValido("REPROVADO"));
        assertFalse(resultadoValido("PENDENTE"));
    }

    @Test
    void encerrarExigeAcaoCorretiva() {
        assertTrue(podeEncerrarNc("EM_ACAO", "Troca de lote"));
        assertFalse(podeEncerrarNc("EM_ACAO", "  "));
        assertFalse(podeEncerrarNc("ENCERRADA", "x"));
    }
}
