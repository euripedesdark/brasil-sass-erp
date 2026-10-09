package br.com.brasil_saas.compras.service.impl;

import br.com.brasil_saas.compras.model.*;
import br.com.brasil_saas.compras.repository.*;
import br.com.brasil_saas.compras.service.ConferenciaFaturaCompraService.Request;
import br.com.brasil_saas.financeiro.repository.TituloRepository;
import br.com.brasil_saas.fiscal.model.*;
import br.com.brasil_saas.fiscal.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecebimentoRateioMultiNfTest {
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
    void preparar() {
        var pedido = new PedidoCompra();
        pedido.setId(1L); pedido.setEmpresaId(2L); pedido.setStatus("PARCIAL");
        pedido.setValorTotal(new BigDecimal("100"));
        var ip = new ItemPedidoCompra();
        ip.setProdutoId(6L); ip.setQuantidade(new BigDecimal("10"));
        ip.setValorUnitario(BigDecimal.TEN); ip.setValorTotal(new BigDecimal("100"));
        pedido.getItens().add(ip);
        when(pedidos.findByIdForUpdate(1L)).thenReturn(Optional.of(pedido));
        var rec = new RecebimentoCompra();
        rec.setId(4L); rec.setPedidoId(1L); rec.setValorTotal(new BigDecimal("100"));
        when(recebimentos.findByIdAndEmpresaIdAndDeletedAtIsNull(4L, 2L)).thenReturn(Optional.of(rec));
        var r = new RecebimentoCompraItem();
        r.setProdutoId(6L); r.setQuantidadeRecebida(new BigDecimal("10"));
        r.setValorUnitario(BigDecimal.TEN);
        when(recebidos.findByEmpresaIdAndRecebimentoIdAndDeletedAtIsNullOrderByIdAsc(2L, 4L))
                .thenReturn(List.of(r));
        when(conferencias.save(any())).thenAnswer(i -> {
            ConferenciaFaturaCompra c = i.getArgument(0); c.setId(8L); return c;
        });
    }

    private void nf(Long nfeId, String qtd, String total) {
        var nf = new Nfe();
        nf.setId(nfeId); nf.setTipoOperacao("E"); nf.setPedidoCompraId(1L);
        nf.setStatus("AUTORIZADA"); nf.setValorTotal(new BigDecimal(total));
        when(notas.findByIdAndEmpresaIdAndDeletedAtIsNull(nfeId, 2L)).thenReturn(Optional.of(nf));
        var f = new NfeItem();
        f.setProdutoId(6L); f.setQuantidade(new BigDecimal(qtd));
        f.setValorUnitario(BigDecimal.TEN); f.setValorTotal(new BigDecimal(total));
        when(notasItens.findByNfeIdOrderByNumeroItem(nfeId)).thenReturn(List.of(f));
    }

    private ConferenciaFaturaCompra conferir(Long nfeId, String valor) {
        return service.conferir(2L, new Request(1L, new BigDecimal(valor), 4L, null, nfeId, BigDecimal.ZERO));
    }

    private ConferenciaFaturaCompra anteriorAprovada(Long id, Long nfeId, String valor) {
        var c = new ConferenciaFaturaCompra();
        c.setId(id); c.setStatus("APROVADA"); c.setRecebimentoId(4L);
        c.setNfeId(nfeId); c.setValorFatura(new BigDecimal(valor));
        return c;
    }

    private ConferenciaFaturaCompraItem itemConsumido(String qtdFat) {
        var i = new ConferenciaFaturaCompraItem();
        i.setProdutoId(6L); i.setQuantidadeFaturada(new BigDecimal(qtdFat));
        i.setQuantidadeRecebida(new BigDecimal("10"));
        return i;
    }

    @Test
    void primeiraNfDoRateioAprova() {
        nf(5L, "4", "40");
        var c = conferir(5L, "40");
        assertEquals("APROVADA", c.getStatus());
    }

    @Test
    void segundaNfAprovaDentroDoAcumulado() {
        nf(6L, "4", "40");
        when(conferencias.findConsumoAcumuladoRecebimento(2L, 4L, 6L))
                .thenReturn(List.of(anteriorAprovada(8L, 5L, "40")));
        when(itens.findByEmpresaIdAndConferenciaIdAndDeletedAtIsNullOrderByNumeroItemAsc(2L, 8L))
                .thenReturn(List.of(itemConsumido("4")));
        var c = conferir(6L, "40");
        assertEquals("APROVADA", c.getStatus());
    }

    @Test
    void terceiraNfAcimaDoAcumuladoDiverge() {
        nf(7L, "4", "40");
        when(conferencias.findConsumoAcumuladoRecebimento(2L, 4L, 7L)).thenReturn(List.of(
                anteriorAprovada(8L, 5L, "40"), anteriorAprovada(9L, 6L, "40")));
        when(itens.findByEmpresaIdAndConferenciaIdAndDeletedAtIsNullOrderByNumeroItemAsc(2L, 8L))
                .thenReturn(List.of(itemConsumido("4")));
        when(itens.findByEmpresaIdAndConferenciaIdAndDeletedAtIsNullOrderByNumeroItemAsc(2L, 9L))
                .thenReturn(List.of(itemConsumido("4")));
        var c = conferir(7L, "40");
        assertEquals("DIVERGENTE", c.getStatus());
        assertTrue(c.getDivergencia().contains("consumo acumulado excedido"));
    }

    @Test
    void mesmaNfEmOutroRecebimentoBloqueia() {
        nf(5L, "4", "40");
        var anterior = new ConferenciaFaturaCompra(); anterior.setId(3L);
        anterior.setNfeId(5L); anterior.setRecebimentoId(99L);
        when(conferencias.findConsumosConflitantes(2L, 4L, 5L)).thenReturn(List.of(anterior));
        var c = conferir(5L, "40");
        assertEquals("DIVERGENTE", c.getStatus());
        assertTrue(c.getDivergencia().contains("ja consumida pela conferencia 3"));
    }

    @Test
    void reavaliacaoDoMesmoParNaoSomaEmDuplicata() {
        nf(5L, "4", "40");
        when(conferencias.findConsumoAcumuladoRecebimento(2L, 4L, 5L)).thenReturn(List.of());
        when(conferencias.findConsumosConflitantes(2L, 4L, 5L)).thenReturn(List.of());
        var c = conferir(5L, "40");
        assertEquals("APROVADA", c.getStatus());
    }
}
