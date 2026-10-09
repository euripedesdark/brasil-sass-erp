package br.com.brasil_saas.financeiro.service.impl;

import br.com.brasil_saas.compras.repository.ConferenciaFaturaCompraRepository;
import br.com.brasil_saas.financeiro.dto.FinanceiroDtos.BaixaRequest;
import br.com.brasil_saas.financeiro.model.Baixa;
import br.com.brasil_saas.financeiro.model.CondicaoPagamento;
import br.com.brasil_saas.financeiro.model.Titulo;
import br.com.brasil_saas.financeiro.model.TituloParcela;
import br.com.brasil_saas.financeiro.repository.*;
import br.com.brasil_saas.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TituloBaixaRateioParcelasTest {
    @Mock TituloRepository titulos;
    @Mock TituloParcelaRepository parcelas;
    @Mock BaixaRepository baixas;
    @Mock CondicaoPagamentoRepository condicoes;
    @Mock ContaBancariaRepository contas;
    @Mock ExtratoRepository extratos;
    @Mock ConferenciaFaturaCompraRepository conferencias;
    @InjectMocks TituloServiceImpl service;
    private Titulo titulo;
    private TituloParcela p1;
    private TituloParcela p2;

    @BeforeEach
    void preparar() {
        titulo = new Titulo();
        titulo.setId(10L); titulo.setEmpresaId(1L); titulo.setTipo("R");
        titulo.setDataEmissao(LocalDate.of(2026, 1, 1));
        titulo.setValorSaldo(new BigDecimal("100.00"));
        lenient().when(titulos.findForUpdate(10L, 1L)).thenReturn(Optional.of(titulo));
        lenient().when(titulos.findByIdAndEmpresaIdAndDeletedAtIsNull(10L, 1L)).thenReturn(Optional.of(titulo));
        p1 = parcela(1L, 1, "50.00");
        p2 = parcela(2L, 2, "50.00");
        lenient().when(parcelas.findByTituloIdAndDeletedAtIsNullOrderByNumeroParcela(10L)).thenReturn(List.of(p1, p2));
        lenient().when(parcelas.findForUpdate(1L, 1L)).thenReturn(Optional.of(p1));
        lenient().when(baixas.save(any(Baixa.class))).thenAnswer(i -> i.getArgument(0));
    }

    private TituloParcela parcela(Long id, int numero, String saldo) {
        var p = new TituloParcela();
        p.setId(id); p.setTituloId(10L); p.setNumeroParcela(numero);
        p.setValorParcela(new BigDecimal(saldo)); p.setValorSaldo(new BigDecimal(saldo));
        p.setStatus("ABERTO");
        return p;
    }

    private BaixaRequest baixaSemParcela(String valor) {
        return new BaixaRequest(null, null, null, LocalDate.of(2026, 1, 2),
                new BigDecimal(valor), null, null, null, null);
    }

    @Test
    void baixaSemParcelaAbatePrimeiraParcelaEDepoisASegunda() {
        service.baixar(1L, 10L, baixaSemParcela("70.00"));
        assertEquals(0, p1.getValorSaldo().signum());
        assertEquals("BAIXADA", p1.getStatus());
        assertEquals(new BigDecimal("30.00"), p2.getValorSaldo());
        assertEquals("ABERTO", p2.getStatus());
        assertEquals(new BigDecimal("30.00"), titulo.getValorSaldo());
    }

    @Test
    void baixaTotalSemParcelaBaixaTodasAsParcelas() {
        when(parcelas.findForUpdate(2L, 1L)).thenReturn(Optional.of(p2));
        service.baixar(1L, 10L, baixaSemParcela("100.00"));
        assertEquals("BAIXADA", p1.getStatus());
        assertEquals("BAIXADA", p2.getStatus());
        assertEquals("BAIXADO", titulo.getStatus());
    }

    @Test
    void parcelaSemCondicaoPreservaVencimentoDoTitulo() {
        titulo.setDataVencimento(LocalDate.of(2026, 1, 31));
        when(parcelas.findByTituloIdAndDeletedAtIsNullOrderByNumeroParcela(10L)).thenReturn(List.of());
        when(parcelas.save(any())).thenAnswer(i -> i.getArgument(0));
        var geradas = service.gerarParcelas(1L, 10L, null);
        assertEquals(1, geradas.size());
        assertEquals(titulo.getDataVencimento(), geradas.get(0).dataVencimento());
        assertEquals(new BigDecimal("100.00"), geradas.get(0).valorParcela());
    }

    @Test
    void centavosNaoProduzemUltimaParcelaNegativa() {
        titulo.setValorSaldo(new BigDecimal("0.02"));
        var cond = new CondicaoPagamento(); cond.setDias("1,2,3,4");
        when(parcelas.findByTituloIdAndDeletedAtIsNullOrderByNumeroParcela(10L)).thenReturn(List.of());
        when(condicoes.findByIdAndEmpresaIdAndDeletedAtIsNull(5L, 1L)).thenReturn(Optional.of(cond));
        when(parcelas.save(any())).thenAnswer(i -> i.getArgument(0));
        var geradas = service.gerarParcelas(1L, 10L, 5L);
        assertEquals(4, geradas.size());
        assertTrue(geradas.stream().allMatch(p -> p.valorParcela().signum() >= 0));
        assertEquals(new BigDecimal("0.02"), geradas.stream().map(p -> p.valorParcela()).reduce(BigDecimal.ZERO, BigDecimal::add));
        assertEquals(List.of(new BigDecimal("0.01"), new BigDecimal("0.01"), new BigDecimal("0.00"), new BigDecimal("0.00")),
                geradas.stream().map(p -> p.valorParcela()).toList());
    }

    @Test
    void condicaoExplicitaMantemOsPrazosConfigurados() {
        titulo.setDataVencimento(LocalDate.of(2026, 1, 31));
        var cond = new CondicaoPagamento(); cond.setDias("0,15,60");
        when(parcelas.findByTituloIdAndDeletedAtIsNullOrderByNumeroParcela(10L)).thenReturn(List.of());
        when(condicoes.findByIdAndEmpresaIdAndDeletedAtIsNull(5L, 1L)).thenReturn(Optional.of(cond));
        when(parcelas.save(any())).thenAnswer(i -> i.getArgument(0));
        var geradas = service.gerarParcelas(1L, 10L, 5L);
        assertEquals(List.of(titulo.getDataEmissao(), titulo.getDataEmissao().plusDays(15), titulo.getDataEmissao().plusDays(60)),
                geradas.stream().map(p -> p.dataVencimento()).toList());
        assertEquals(new BigDecimal("100.00"), geradas.stream().map(p -> p.valorParcela()).reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    @Test
    void condicaoComPrazoInvalidoViraErroDeNegocio() {
        var cond = new CondicaoPagamento();
        cond.setDias("30,abc");
        when(parcelas.findByTituloIdAndDeletedAtIsNullOrderByNumeroParcela(10L)).thenReturn(List.of());
        when(condicoes.findByIdAndEmpresaIdAndDeletedAtIsNull(5L, 1L)).thenReturn(Optional.of(cond));
        assertThrows(BusinessException.class, () -> service.gerarParcelas(1L, 10L, 5L));
        verify(parcelas, never()).save(any());
    }
}
