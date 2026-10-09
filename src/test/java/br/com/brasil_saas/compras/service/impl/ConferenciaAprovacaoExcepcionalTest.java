package br.com.brasil_saas.compras.service.impl;

import br.com.brasil_saas.compras.model.ConferenciaFaturaCompra;
import br.com.brasil_saas.compras.repository.ConferenciaFaturaCompraItemRepository;
import br.com.brasil_saas.compras.repository.ConferenciaFaturaCompraRepository;
import br.com.brasil_saas.compras.repository.PedidoCompraRepository;
import br.com.brasil_saas.compras.repository.RecebimentoCompraItemRepository;
import br.com.brasil_saas.compras.repository.RecebimentoCompraRepository;
import br.com.brasil_saas.core.service.DocumentoFluxoService;
import br.com.brasil_saas.financeiro.repository.TituloRepository;
import br.com.brasil_saas.fiscal.repository.NfeItemRepository;
import br.com.brasil_saas.fiscal.repository.NfeRepository;
import br.com.brasil_saas.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConferenciaAprovacaoExcepcionalTest {
    @Mock ConferenciaFaturaCompraRepository repository;
    @Mock ConferenciaFaturaCompraItemRepository itemRepository;
    @Mock PedidoCompraRepository pedidoRepository;
    @Mock RecebimentoCompraRepository recebimentoRepository;
    @Mock RecebimentoCompraItemRepository recebimentoItemRepository;
    @Mock TituloRepository tituloRepository;
    @Mock NfeRepository nfeRepository;
    @Mock NfeItemRepository nfeItemRepository;
    @Mock DocumentoFluxoService documentoFluxoService;
    @InjectMocks ConferenciaFaturaCompraServiceImpl service;

    private ConferenciaFaturaCompra divergente() {
        var c = new ConferenciaFaturaCompra();
        c.setId(11L); c.setEmpresaId(2L); c.setPedidoId(1L);
        c.setRecebimentoId(4L); c.setNfeId(5L);
        c.setStatus("DIVERGENTE");
        c.setDivergencia("total divergente");
        return c;
    }

    @Test
    void aprovaDivergenteComMotivoEAudita() {
        when(repository.findByIdForUpdate(11L, 2L)).thenReturn(Optional.of(divergente()));
        when(repository.countPosterioresMesmoPar(2L, 1L, 5L, 4L, 11L)).thenReturn(0L);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        var c = service.aprovarExcepcional(2L, 7L, 11L, "Preco aceito pela gerencia apos negociacao com o fornecedor");
        assertEquals("APROVADA", c.getStatus());
        assertTrue(c.getDivergencia().contains("Preco aceito pela gerencia apos negociacao com o fornecedor"));
        assertTrue(c.getDivergencia().contains("APROVACAO EXCEPCIONAL"));
        verify(documentoFluxoService).ligar(eq(2L), eq(7L),
                eq("CONFERENCIA_COMPRA"), eq(11L), any(),
                eq("PEDIDO_COMPRA"), eq(1L), any(), eq("APROVACAO_EXCEPCIONAL"));
    }

    @Test
    void rejeitaMotivoCurtoOuAusente() {
        assertThrows(BusinessException.class, () -> service.aprovarExcepcional(2L, 7L, 11L, "curto"));
        assertThrows(BusinessException.class, () -> service.aprovarExcepcional(2L, 7L, 11L, null));
        assertThrows(BusinessException.class, () -> service.aprovarExcepcional(2L, 7L, 11L, "   "));
        verifyNoInteractions(repository, documentoFluxoService);
    }

    @Test
    void rejeitaNaoDivergente() {
        var c = divergente(); c.setStatus("APROVADA");
        when(repository.findByIdForUpdate(11L, 2L)).thenReturn(Optional.of(c));
        assertThrows(BusinessException.class, () -> service.aprovarExcepcional(2L, 7L, 11L, "Preco aceito pela gerencia apos negociacao com o fornecedor"));
        verify(repository, never()).save(any());
        verifyNoInteractions(documentoFluxoService);
    }

    @Test
    void rejeitaSuperadaPorReavaliacao() {
        when(repository.findByIdForUpdate(11L, 2L)).thenReturn(Optional.of(divergente()));
        when(repository.countPosterioresMesmoPar(2L, 1L, 5L, 4L, 11L)).thenReturn(2L);
        assertThrows(BusinessException.class, () -> service.aprovarExcepcional(2L, 7L, 11L, "Preco aceito pela gerencia apos negociacao com o fornecedor"));
        verify(repository, never()).save(any());
        verifyNoInteractions(documentoFluxoService);
    }

    @Test
    void usaMesmaTravaDoTitulo() {
        when(repository.findByIdForUpdate(11L, 2L)).thenReturn(Optional.of(divergente()));
        when(repository.countPosterioresMesmoPar(2L, 1L, 5L, 4L, 11L)).thenReturn(0L);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        service.aprovarExcepcional(2L, 7L, 11L, "Preco aceito pela gerencia apos negociacao com o fornecedor");
        verify(repository).findByIdForUpdate(11L, 2L);
    }
}
