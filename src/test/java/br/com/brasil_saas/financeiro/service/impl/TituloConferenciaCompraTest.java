package br.com.brasil_saas.financeiro.service.impl;

import br.com.brasil_saas.compras.model.ConferenciaFaturaCompra;
import br.com.brasil_saas.compras.repository.ConferenciaFaturaCompraRepository;
import br.com.brasil_saas.financeiro.dto.FinanceiroDtos.BaixaRequest;
import br.com.brasil_saas.financeiro.model.Baixa;
import br.com.brasil_saas.financeiro.model.Titulo;
import br.com.brasil_saas.financeiro.repository.*;
import br.com.brasil_saas.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
class TituloConferenciaCompraTest {
    @Mock TituloRepository titulos;
    @Mock TituloParcelaRepository parcelas;
    @Mock BaixaRepository baixas;
    @Mock CondicaoPagamentoRepository condicoes;
    @Mock ContaBancariaRepository contas;
    @Mock ExtratoRepository extratos;
    @Mock ConferenciaFaturaCompraRepository conferencias;
    @Mock br.com.brasil_saas.compras.repository.ConferenciaFaturaCompraItemRepository conferenciaItens;
    @Mock br.com.brasil_saas.compras.repository.PedidoCompraRepository pedidosCompra;
    @Mock br.com.brasil_saas.compras.repository.RecebimentoCompraRepository recebimentosCompra;
    @Mock br.com.brasil_saas.compras.repository.RecebimentoCompraItemRepository recebidosItens;
    @Mock br.com.brasil_saas.fiscal.repository.NfeRepository nfes;
    @InjectMocks TituloServiceImpl service;
    private Titulo titulo;

    @BeforeEach
    void preparar() {
        titulo = new Titulo();
        titulo.setId(10L);
        titulo.setEmpresaId(1L);
        titulo.setTipo("P");
        titulo.setDataEmissao(LocalDate.of(2026, 1, 1));
        titulo.setValorSaldo(new BigDecimal("100.00"));
        when(titulos.findForUpdate(10L, 1L)).thenReturn(Optional.of(titulo));
    }

    private ConferenciaFaturaCompra conferencia(String status) {
        var c = new ConferenciaFaturaCompra();
        c.setStatus(status);
        return c;
    }

    private BaixaRequest request() {
        return new BaixaRequest(null, null, null, LocalDate.of(2026, 1, 2),
                new BigDecimal("10.00"), null, null, null, null);
    }

    @ParameterizedTest
    @ValueSource(strings = {"DIVERGENTE", "PENDENTE", "REJEITADA"})
    void impedirBaixaSemMoverSaldoMesmoComOutroDocumentoAprovado(String status) {
        when(conferencias.findVigentesParaTitulo(1L, 10L))
                .thenReturn(List.of(conferencia("APROVADA"), conferencia(status)));
        var erro = assertThrows(BusinessException.class, () -> service.baixar(1L, 10L, request()));
        assertTrue(erro.getMessage().contains("conferência de compra"));
        assertEquals(new BigDecimal("100.00"), titulo.getValorSaldo());
        verify(titulos, never()).save(any());
        verifyNoInteractions(baixas, parcelas, contas, extratos);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void permitirBaixaAprovadaEManterTitulosSemConferencia(boolean temConferencia) {
        when(conferencias.findVigentesParaTitulo(1L, 10L))
                .thenReturn(temConferencia ? List.of(conferencia("APROVADA")) : List.of());
        when(baixas.save(any(Baixa.class))).thenAnswer(i -> i.getArgument(0));
        var resposta = service.baixar(1L, 10L, request());
        assertEquals(new BigDecimal("90.00"), resposta.saldoRestante());
        verify(baixas).save(any(Baixa.class));
        verify(titulos).save(titulo);
    }
}
