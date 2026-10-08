package br.com.brasil_saas.compras.service.impl;

import br.com.brasil_saas.compras.model.PedidoCompra;
import br.com.brasil_saas.compras.repository.*;
import br.com.brasil_saas.compras.service.ConferenciaFaturaCompraService.Request;
import br.com.brasil_saas.financeiro.model.Titulo;
import br.com.brasil_saas.financeiro.repository.TituloRepository;
import br.com.brasil_saas.fiscal.repository.*;
import br.com.brasil_saas.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConferenciaPagamentoCompraTest {
    @Mock ConferenciaFaturaCompraRepository conferencias;
    @Mock ConferenciaFaturaCompraItemRepository itens;
    @Mock PedidoCompraRepository pedidos;
    @Mock RecebimentoCompraRepository recebimentos;
    @Mock RecebimentoCompraItemRepository recebidos;
    @Mock TituloRepository titulos;
    @Mock NfeRepository notas;
    @Mock NfeItemRepository notasItens;
    @InjectMocks ConferenciaFaturaCompraServiceImpl service;

    @BeforeEach
    void pedido() {
        var p = new PedidoCompra();
        p.setId(1L); p.setEmpresaId(2L); p.setTituloId(3L); p.setStatus("RECEBIDO");
        when(pedidos.findByIdForUpdate(1L)).thenReturn(Optional.of(p));
    }

    private Request request(Long titulo) {
        return new Request(1L, BigDecimal.TEN, 4L, titulo, 5L, BigDecimal.ZERO);
    }

    private Titulo titulo(String tipo, String status, String saldo) {
        var t = new Titulo(); t.setTipo(tipo); t.setStatus(status);
        t.setValorOriginal(new BigDecimal("100")); t.setValorSaldo(new BigDecimal(saldo));
        return t;
    }

    @Test
    void impedeTrocaDoTituloDoPedido() {
        assertThrows(BusinessException.class, () -> service.conferir(2L, request(99L)));
        verifyNoInteractions(titulos, conferencias, itens, recebimentos, notas);
    }

    @Test
    void travaTituloDoPedidoMesmoQuandoRequestOmiteTitulo() {
        when(titulos.findForUpdate(3L, 2L)).thenReturn(Optional.of(titulo("P", "BAIXADO", "0")));
        assertThrows(BusinessException.class, () -> service.conferir(2L, request(null)));
        verify(titulos).findForUpdate(3L, 2L);
        verifyNoInteractions(conferencias, itens, recebimentos, notas);
    }

    @Test
    void naoReconfereDepoisDePagamentoParcial() {
        when(titulos.findForUpdate(3L, 2L)).thenReturn(Optional.of(titulo("P", "ABERTO", "90")));
        assertThrows(BusinessException.class, () -> service.conferir(2L, request(3L)));
        verifyNoInteractions(conferencias, itens, recebimentos, notas);
    }

    @Test
    void recusaTituloAReceber() {
        when(titulos.findForUpdate(3L, 2L)).thenReturn(Optional.of(titulo("R", "ABERTO", "100")));
        assertThrows(BusinessException.class, () -> service.conferir(2L, request(3L)));
        verifyNoInteractions(conferencias, itens, recebimentos, notas);
    }

    @Test
    void gravaTituloDoPedidoQuandoRequestOmiteVinculo() {
        var p = new PedidoCompra();
        p.setId(1L); p.setEmpresaId(2L); p.setTituloId(3L); p.setStatus("RECEBIDO");
        p.setValorTotal(BigDecimal.TEN);
        var item = new br.com.brasil_saas.compras.model.ItemPedidoCompra();
        item.setProdutoId(6L); item.setNumeroItem(1); item.setQuantidade(BigDecimal.ONE);
        item.setValorUnitario(BigDecimal.TEN); p.getItens().add(item);
        when(pedidos.findByIdForUpdate(1L)).thenReturn(Optional.of(p));
        when(titulos.findForUpdate(3L, 2L)).thenReturn(Optional.of(titulo("P", "ABERTO", "100")));
        var rec = new br.com.brasil_saas.compras.model.RecebimentoCompra();
        rec.setId(4L); rec.setPedidoId(1L); rec.setValorTotal(BigDecimal.TEN);
        when(recebimentos.findByIdAndEmpresaIdAndDeletedAtIsNull(4L, 2L)).thenReturn(Optional.of(rec));
        var recebido = new br.com.brasil_saas.compras.model.RecebimentoCompraItem();
        recebido.setProdutoId(6L); recebido.setQuantidadeRecebida(BigDecimal.ONE);
        recebido.setValorUnitario(BigDecimal.TEN);
        when(recebidos.findByEmpresaIdAndRecebimentoIdAndDeletedAtIsNullOrderByIdAsc(2L, 4L))
                .thenReturn(java.util.List.of(recebido));
        var nf = new br.com.brasil_saas.fiscal.model.Nfe();
        nf.setId(5L); nf.setTipoOperacao("E"); nf.setPedidoCompraId(1L); nf.setStatus("AUTORIZADA");
        when(notas.findByIdAndEmpresaIdAndDeletedAtIsNull(5L, 2L)).thenReturn(Optional.of(nf));
        var ni = new br.com.brasil_saas.fiscal.model.NfeItem();
        ni.setProdutoId(6L); ni.setQuantidade(BigDecimal.ONE); ni.setValorUnitario(BigDecimal.TEN);
        when(notasItens.findByNfeIdOrderByNumeroItem(5L)).thenReturn(java.util.List.of(ni));
        when(conferencias.save(any())).thenAnswer(i -> {
            br.com.brasil_saas.compras.model.ConferenciaFaturaCompra c = i.getArgument(0);
            c.setId(7L); return c;
        });
        var salva = service.conferir(2L, request(null));
        assertEquals(3L, salva.getTituloId());
        assertEquals("APROVADA", salva.getStatus());
        var ordem = inOrder(titulos, conferencias, itens);
        ordem.verify(titulos).findForUpdate(3L, 2L);
        ordem.verify(conferencias).save(any());
        ordem.verify(itens).saveAll(any());
    }
}
