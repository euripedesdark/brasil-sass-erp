package br.com.brasil_saas.fiscal.service;

import br.com.brasil_saas.fiscal.model.Apuracao;
import br.com.brasil_saas.fiscal.model.Imposto;
import br.com.brasil_saas.fiscal.model.Nfe;
import br.com.brasil_saas.fiscal.repository.ApuracaoRepository;
import br.com.brasil_saas.fiscal.repository.ImpostoRepository;
import br.com.brasil_saas.fiscal.repository.NfeRepository;
import br.com.brasil_saas.fiscal.repository.NfseRepository;
import br.com.brasil_saas.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApuracaoServiceTest {

    @Mock ApuracaoRepository apuracoes;
    @Mock ImpostoRepository impostos;
    @Mock NfeRepository nfes;
    @Mock NfseRepository nfses;

    @InjectMocks ApuracaoService svc;

    Imposto pis;
    Imposto icms;

    @BeforeEach
    void setUp() {
        pis = new Imposto();
        pis.setId(1L);
        pis.setEmpresaId(10L);
        pis.setSigla("PIS");
        pis.setNome("PIS");
        pis.setAliquotaPadrao(new BigDecimal("1.65"));

        icms = new Imposto();
        icms.setId(2L);
        icms.setEmpresaId(10L);
        icms.setSigla("ICMS");
        icms.setNome("ICMS");
    }

    @Test
    void pisUsaValorDestacadoECreditoDasEntradas() {
        when(impostos.findById(1L)).thenReturn(Optional.of(pis));
        when(apuracoes.findByEmpresaIdAndImpostoIdAndCompetencia(10L, 1L, "03/2026"))
                .thenReturn(Optional.empty());
        when(apuracoes.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Nfe saida = nfe("S", "1000.00", "16.50", "0");
        Nfe entrada = nfe("E", "500.00", "8.25", "0");
        when(nfes.findByEmpresaIdAndTipoOperacaoAndDataEmissaoBetweenAndDeletedAtIsNull(
                eq(10L), eq("S"), any(), any())).thenReturn(List.of(saida));
        when(nfes.findByEmpresaIdAndTipoOperacaoAndDataEmissaoBetweenAndDeletedAtIsNull(
                eq(10L), eq("E"), any(), any())).thenReturn(List.of(entrada));

        Apuracao a = svc.calcular(10L, 1L, "03/2026");

        assertEquals(new BigDecimal("1000.00"), a.getBaseCalculo());
        assertEquals(new BigDecimal("16.50"), a.getValorDevido());
        assertEquals(new BigDecimal("8.25"), a.getValorCredito());
        assertEquals(new BigDecimal("8.25"), a.getValorPagar());
        assertEquals("ABERTA", a.getStatus());
    }

    @Test
    void pisSemValorDestacadoUsaAliquotaPadrao() {
        when(impostos.findById(1L)).thenReturn(Optional.of(pis));
        when(apuracoes.findByEmpresaIdAndImpostoIdAndCompetencia(10L, 1L, "03/2026"))
                .thenReturn(Optional.empty());
        when(apuracoes.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Nfe saida = nfe("S", "1000.00", "0", "0"); // sem PIS destacado
        when(nfes.findByEmpresaIdAndTipoOperacaoAndDataEmissaoBetweenAndDeletedAtIsNull(
                eq(10L), eq("S"), any(), any())).thenReturn(List.of(saida));
        when(nfes.findByEmpresaIdAndTipoOperacaoAndDataEmissaoBetweenAndDeletedAtIsNull(
                eq(10L), eq("E"), any(), any())).thenReturn(List.of());

        Apuracao a = svc.calcular(10L, 1L, "03/2026");

        // 1000 * 1.65% = 16.50
        assertEquals(new BigDecimal("1000.00"), a.getBaseCalculo());
        assertEquals(new BigDecimal("16.50"), a.getValorDevido());
        assertEquals(new BigDecimal("0.00"), a.getValorCredito());
        assertEquals(new BigDecimal("16.50"), a.getValorPagar());
    }

    @Test
    void icmsDebitoSaidaCreditoEntrada() {
        when(impostos.findById(2L)).thenReturn(Optional.of(icms));
        when(apuracoes.findByEmpresaIdAndImpostoIdAndCompetencia(10L, 2L, "03/2026"))
                .thenReturn(Optional.empty());
        when(apuracoes.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Nfe saida = nfeIcms("S", "2000.00", "360.00");
        Nfe entrada = nfeIcms("E", "800.00", "144.00");
        when(nfes.findByEmpresaIdAndTipoOperacaoAndDataEmissaoBetweenAndDeletedAtIsNull(
                eq(10L), eq("S"), any(), any())).thenReturn(List.of(saida));
        when(nfes.findByEmpresaIdAndTipoOperacaoAndDataEmissaoBetweenAndDeletedAtIsNull(
                eq(10L), eq("E"), any(), any())).thenReturn(List.of(entrada));

        Apuracao a = svc.calcular(10L, 2L, "03/2026");

        assertEquals(new BigDecimal("2000.00"), a.getBaseCalculo());
        assertEquals(new BigDecimal("360.00"), a.getValorDevido());
        assertEquals(new BigDecimal("144.00"), a.getValorCredito());
        assertEquals(new BigDecimal("216.00"), a.getValorPagar());
    }

    @Test
    void naoRecalculaCompetenciaEncerrada() {
        when(impostos.findById(1L)).thenReturn(Optional.of(pis));
        Apuracao existente = new Apuracao();
        existente.setStatus("ENCERRADA");
        when(apuracoes.findByEmpresaIdAndImpostoIdAndCompetencia(10L, 1L, "03/2026"))
                .thenReturn(Optional.of(existente));
        when(nfes.findByEmpresaIdAndTipoOperacaoAndDataEmissaoBetweenAndDeletedAtIsNull(
                anyLong(), anyString(), any(), any())).thenReturn(List.of());

        assertThrows(BusinessException.class, () -> svc.calcular(10L, 1L, "03/2026"));
    }

    @Test
    void competenciaInvalida() {
        when(impostos.findById(1L)).thenReturn(Optional.of(pis));
        assertThrows(BusinessException.class, () -> svc.calcular(10L, 1L, "2026-03"));
    }

    private Nfe nfe(String tipo, String produtos, String pis, String cofins) {
        Nfe n = new Nfe();
        n.setTipoOperacao(tipo);
        n.setStatus("AUTORIZADA");
        n.setValorProdutos(new BigDecimal(produtos));
        n.setValorPis(new BigDecimal(pis));
        n.setValorCofins(new BigDecimal(cofins));
        n.setValorIcms(BigDecimal.ZERO);
        n.setValorIpi(BigDecimal.ZERO);
        n.setDataEmissao(LocalDateTime.of(2026, 3, 15, 10, 0));
        return n;
    }

    private Nfe nfeIcms(String tipo, String produtos, String icms) {
        Nfe n = nfe(tipo, produtos, "0", "0");
        n.setValorIcms(new BigDecimal(icms));
        return n;
    }
}
