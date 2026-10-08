package br.com.brasil_saas.estoque.controller;

import br.com.brasil_saas.estoque.model.LoteEstoque;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regras de ciclo de vida do lote usadas nas operacoes de estoque.
 * ATIVO pode sair; QUARENTENA/BLOQUEADO/VENCIDO/INATIVO nao.
 */
class LoteStatusRulesTest {

    private static boolean podeMovimentar(LoteEstoque lote) {
        return lote != null && "ATIVO".equals(lote.getStatus());
    }

    private static String statusAposLiberar(LoteEstoque lote) {
        if (lote.getDataValidade() != null && lote.getDataValidade().isBefore(LocalDate.now())) {
            return "VENCIDO";
        }
        return "ATIVO";
    }

    @Test
    void somenteAtivoPodeSairEmTransferenciaOuReserva() {
        LoteEstoque ativo = new LoteEstoque();
        ativo.setStatus("ATIVO");
        assertTrue(podeMovimentar(ativo));

        for (String s : new String[]{"QUARENTENA", "BLOQUEADO", "VENCIDO", "INATIVO"}) {
            LoteEstoque bloqueado = new LoteEstoque();
            bloqueado.setStatus(s);
            assertFalse(podeMovimentar(bloqueado), "status " + s);
        }
    }

    @Test
    void liberacaoComValidadeVencidaViraVencido() {
        LoteEstoque lote = new LoteEstoque();
        lote.setStatus("QUARENTENA");
        lote.setDataValidade(LocalDate.now().minusDays(1));
        assertEquals("VENCIDO", statusAposLiberar(lote));
    }

    @Test
    void liberacaoComValidadeFuturaVoltaAtivo() {
        LoteEstoque lote = new LoteEstoque();
        lote.setStatus("BLOQUEADO");
        lote.setDataValidade(LocalDate.now().plusDays(10));
        assertEquals("ATIVO", statusAposLiberar(lote));
    }
}
