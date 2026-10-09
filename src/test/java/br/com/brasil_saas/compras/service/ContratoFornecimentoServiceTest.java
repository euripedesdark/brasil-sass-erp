package br.com.brasil_saas.compras.service;

import br.com.brasil_saas.compras.model.ContratoFornecimento;
import br.com.brasil_saas.compras.model.ContratoFornecimentoItem;
import br.com.brasil_saas.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ContratoFornecimentoServiceTest {

    private static ContratoFornecimento contrato(String status, LocalDate ini, LocalDate fim) {
        ContratoFornecimento c = new ContratoFornecimento();
        c.setStatus(status);
        c.setVigenciaInicio(ini);
        c.setVigenciaFim(fim);
        return c;
    }

    @Test
    void liberaContratoAtivoDentroDaVigencia() {
        LocalDate hoje = LocalDate.of(2026, 10, 8);
        assertDoesNotThrow(() -> ContratoFornecimentoService.validarLiberavel(
                contrato(ContratoFornecimento.ATIVO, hoje.minusDays(1), hoje.plusDays(1)), hoje));
    }

    @Test
    void recusaRascunhoEForaDaVigencia() {
        LocalDate hoje = LocalDate.of(2026, 10, 8);
        assertThrows(BusinessException.class, () -> ContratoFornecimentoService.validarLiberavel(
                contrato(ContratoFornecimento.RASCUNHO, hoje.minusDays(1), hoje.plusDays(1)), hoje));
        assertThrows(BusinessException.class, () -> ContratoFornecimentoService.validarLiberavel(
                contrato(ContratoFornecimento.ATIVO, hoje.plusDays(1), hoje.plusDays(9)), hoje));
        assertThrows(BusinessException.class, () -> ContratoFornecimentoService.validarLiberavel(
                contrato(ContratoFornecimento.ATIVO, hoje.minusDays(9), hoje.minusDays(1)), hoje));
    }

    @Test
    void saldoDoItemEContratadoMenosLiberado() {
        ContratoFornecimentoItem i = new ContratoFornecimentoItem();
        i.setQuantidadeContratada(new BigDecimal("100"));
        i.setQuantidadeLiberada(new BigDecimal("30"));
        assertEquals(0, new BigDecimal("70").compareTo(i.saldo()));
    }
}
