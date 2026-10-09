package br.com.brasil_saas.financeiro.service.impl;

import br.com.brasil_saas.compras.model.ConferenciaFaturaCompra;
import br.com.brasil_saas.compras.model.ConferenciaFaturaCompraItem;
import br.com.brasil_saas.compras.model.PedidoCompra;
import br.com.brasil_saas.compras.model.RecebimentoCompra;
import br.com.brasil_saas.compras.model.RecebimentoCompraItem;
import br.com.brasil_saas.compras.repository.ConferenciaFaturaCompraItemRepository;
import br.com.brasil_saas.compras.repository.ConferenciaFaturaCompraRepository;
import br.com.brasil_saas.compras.repository.PedidoCompraRepository;
import br.com.brasil_saas.compras.repository.RecebimentoCompraItemRepository;
import br.com.brasil_saas.compras.repository.RecebimentoCompraRepository;
import br.com.brasil_saas.financeiro.dto.FinanceiroDtos.BaixaRequest;
import br.com.brasil_saas.financeiro.model.Baixa;
import br.com.brasil_saas.financeiro.model.Titulo;
import br.com.brasil_saas.financeiro.repository.*;
import br.com.brasil_saas.fiscal.model.Nfe;
import br.com.brasil_saas.fiscal.repository.NfeRepository;
import br.com.brasil_saas.shared.exception.BusinessException;
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
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class TituloCoberturaTotalCompraTest {
    @Mock TituloRepository titulos;
    @Mock TituloParcelaRepository parcelas;
    @Mock BaixaRepository baixas;
    @Mock CondicaoPagamentoRepository condicoes;
    @Mock ContaBancariaRepository contas;
    @Mock ExtratoRepository extratos;
    @Mock ConferenciaFaturaCompraRepository conferencias;
    @Mock ConferenciaFaturaCompraItemRepository conferenciaItens;
    @Mock PedidoCompraRepository pedidosCompra;
    @Mock RecebimentoCompraRepository recebimentosCompra;
    @Mock RecebimentoCompraItemRepository recebidosItens;
    @Mock NfeRepository nfes;
    @InjectMocks TituloServiceImpl service;

    private Titulo titulo() {
        var t = new Titulo();
        t.setId(10L); t.setEmpresaId(1L); t.setTipo("P");
        t.setStatus("ABERTO");
        t.setDataEmissao(LocalDate.of(2026, 1, 1));
        t.setDataVencimento(LocalDate.of(2026, 2, 1));
        t.setValorOriginal(new BigDecimal("100"));
        t.setValorSaldo(new BigDecimal("100"));
        return t;
    }

    private BaixaRequest request() {
        return new BaixaRequest(null, null, null, LocalDate.of(2026, 1, 2),
                new BigDecimal("10"), null, null, null, null);
    }

    private PedidoCompra pedido() {
        var p = new PedidoCompra();
        p.setId(20L); p.setEmpresaId(1L); p.setTituloId(10L);
        return p;
    }

    private RecebimentoCompra recebimento() {
        var r = new RecebimentoCompra();
        r.setId(30L); r.setPedidoId(20L);
        return r;
    }

    private RecebimentoCompraItem recebidoItem() {
        var i = new RecebimentoCompraItem();
        i.setProdutoId(6L); i.setQuantidadeRecebida(new BigDecimal("10"));
        return i;
    }

    private ConferenciaFaturaCompra vigenteAprovada() {
        var c = new ConferenciaFaturaCompra();
        c.setId(40L); c.setStatus("APROVADA");
        c.setPedidoId(20L); c.setRecebimentoId(30L); c.setNfeId(50L);
        return c;
    }

    private ConferenciaFaturaCompraItem itemFaturado(String qtd) {
        var i = new ConferenciaFaturaCompraItem();
        i.setProdutoId(6L); i.setQuantidadeFaturada(new BigDecimal(qtd));
        return i;
    }

    private Nfe nfAutorizada() {
        var n = new Nfe();
        n.setId(50L); n.setStatus("AUTORIZADA"); n.setPedidoCompraId(20L);
        return n;
    }

    @Test
    void bloqueiaRecebimentoSemConferencia() {
        when(titulos.findForUpdate(10L, 1L)).thenReturn(Optional.of(titulo()));
        when(conferencias.findVigentesParaTitulo(1L, 10L)).thenReturn(List.of());
        when(pedidosCompra.findByEmpresaIdAndTituloIdAndDeletedAtIsNull(1L, 10L))
                .thenReturn(List.of(pedido()));
        when(recebimentosCompra.findByEmpresaIdAndPedidoIdAndDeletedAtIsNullOrderByDataRecebimentoDesc(1L, 20L))
                .thenReturn(List.of(recebimento()));
        var erro = assertThrows(BusinessException.class, () -> service.baixar(1L, 10L, request()));
        assertTrue(erro.getMessage().contains("sem conferencia aprovada"));
        verify(baixas, never()).save(any());
    }

    @Test
    void bloqueiaRecebimentoParcialmenteConferido() {
        when(titulos.findForUpdate(10L, 1L)).thenReturn(Optional.of(titulo()));
        when(conferencias.findVigentesParaTitulo(1L, 10L)).thenReturn(List.of(vigenteAprovada()));
        when(pedidosCompra.findByEmpresaIdAndTituloIdAndDeletedAtIsNull(1L, 10L))
                .thenReturn(List.of(pedido()));
        when(recebimentosCompra.findByEmpresaIdAndPedidoIdAndDeletedAtIsNullOrderByDataRecebimentoDesc(1L, 20L))
                .thenReturn(List.of(recebimento()));
        when(recebidosItens.findByEmpresaIdAndRecebimentoIdAndDeletedAtIsNullOrderByIdAsc(1L, 30L))
                .thenReturn(List.of(recebidoItem()));
        when(conferenciaItens.findByEmpresaIdAndConferenciaIdAndDeletedAtIsNullOrderByNumeroItemAsc(1L, 40L))
                .thenReturn(List.of(itemFaturado("4")));
        var erro = assertThrows(BusinessException.class, () -> service.baixar(1L, 10L, request()));
        assertTrue(erro.getMessage().contains("parcialmente conferido"));
        verify(baixas, never()).save(any());
    }

    @Test
    void bloqueiaNfAutorizadaSemConferencia() {
        when(titulos.findForUpdate(10L, 1L)).thenReturn(Optional.of(titulo()));
        var vig = vigenteAprovada(); vig.setNfeId(null);
        when(conferencias.findVigentesParaTitulo(1L, 10L)).thenReturn(List.of(vig));
        when(pedidosCompra.findByEmpresaIdAndTituloIdAndDeletedAtIsNull(1L, 10L))
                .thenReturn(List.of(pedido()));
        when(recebimentosCompra.findByEmpresaIdAndPedidoIdAndDeletedAtIsNullOrderByDataRecebimentoDesc(1L, 20L))
                .thenReturn(List.of(recebimento()));
        when(recebidosItens.findByEmpresaIdAndRecebimentoIdAndDeletedAtIsNullOrderByIdAsc(1L, 30L))
                .thenReturn(List.of(recebidoItem()));
        when(conferenciaItens.findByEmpresaIdAndConferenciaIdAndDeletedAtIsNullOrderByNumeroItemAsc(1L, 40L))
                .thenReturn(List.of(itemFaturado("10")));
        when(nfes.findByEmpresaIdAndPedidoCompraIdAndDeletedAtIsNull(1L, 20L))
                .thenReturn(List.of(nfAutorizada()));
        var erro = assertThrows(BusinessException.class, () -> service.baixar(1L, 10L, request()));
        assertTrue(erro.getMessage().contains("sem conferencia aprovada"));
        verify(baixas, never()).save(any());
    }

    @Test
    void permiteBaixaComCoberturaTotal() {
        when(titulos.findForUpdate(10L, 1L)).thenReturn(Optional.of(titulo()));
        when(conferencias.findVigentesParaTitulo(1L, 10L)).thenReturn(List.of(vigenteAprovada()));
        when(pedidosCompra.findByEmpresaIdAndTituloIdAndDeletedAtIsNull(1L, 10L))
                .thenReturn(List.of(pedido()));
        when(recebimentosCompra.findByEmpresaIdAndPedidoIdAndDeletedAtIsNullOrderByDataRecebimentoDesc(1L, 20L))
                .thenReturn(List.of(recebimento()));
        when(recebidosItens.findByEmpresaIdAndRecebimentoIdAndDeletedAtIsNullOrderByIdAsc(1L, 30L))
                .thenReturn(List.of(recebidoItem()));
        when(conferenciaItens.findByEmpresaIdAndConferenciaIdAndDeletedAtIsNullOrderByNumeroItemAsc(1L, 40L))
                .thenReturn(List.of(itemFaturado("10")));
        when(nfes.findByEmpresaIdAndPedidoCompraIdAndDeletedAtIsNull(1L, 20L))
                .thenReturn(List.of(nfAutorizada()));
        when(baixas.save(any(Baixa.class))).thenAnswer(i -> i.getArgument(0));
        var resposta = service.baixar(1L, 10L, request());
        assertEquals(new BigDecimal("90"), resposta.saldoRestante());
        verify(baixas).save(any(Baixa.class));
    }

    @Test
    void permiteBaixaSemContextoCompra() {
        when(titulos.findForUpdate(10L, 1L)).thenReturn(Optional.of(titulo()));
        when(conferencias.findVigentesParaTitulo(1L, 10L)).thenReturn(List.of());
        when(pedidosCompra.findByEmpresaIdAndTituloIdAndDeletedAtIsNull(1L, 10L))
                .thenReturn(List.of());
        when(baixas.save(any(Baixa.class))).thenAnswer(i -> i.getArgument(0));
        var resposta = service.baixar(1L, 10L, request());
        assertEquals(new BigDecimal("90"), resposta.saldoRestante());
    }
}
