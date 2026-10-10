package br.com.brasil_saas.compras.service.impl;

import br.com.brasil_saas.compras.model.PedidoCompra;
import br.com.brasil_saas.compras.model.ToleranciaConferencia;
import br.com.brasil_saas.compras.repository.ToleranciaConferenciaRepository;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ToleranciaCadastradaTest {

    private ConferenciaFaturaCompraServiceImpl svc(ToleranciaConferenciaRepository repo) throws Exception {
        var c = ConferenciaFaturaCompraServiceImpl.class.getDeclaredConstructor(
                br.com.brasil_saas.compras.repository.ConferenciaFaturaCompraRepository.class,
                br.com.brasil_saas.compras.repository.ConferenciaFaturaCompraItemRepository.class,
                br.com.brasil_saas.compras.repository.PedidoCompraRepository.class,
                br.com.brasil_saas.compras.repository.RecebimentoCompraRepository.class,
                br.com.brasil_saas.compras.repository.RecebimentoCompraItemRepository.class,
                br.com.brasil_saas.financeiro.repository.TituloRepository.class,
                br.com.brasil_saas.fiscal.repository.NfeRepository.class,
                br.com.brasil_saas.fiscal.repository.NfeItemRepository.class,
                br.com.brasil_saas.core.service.DocumentoFluxoService.class,
                ToleranciaConferenciaRepository.class);
        c.setAccessible(true);
        return c.newInstance(
                mock(br.com.brasil_saas.compras.repository.ConferenciaFaturaCompraRepository.class),
                mock(br.com.brasil_saas.compras.repository.ConferenciaFaturaCompraItemRepository.class),
                mock(br.com.brasil_saas.compras.repository.PedidoCompraRepository.class),
                mock(br.com.brasil_saas.compras.repository.RecebimentoCompraRepository.class),
                mock(br.com.brasil_saas.compras.repository.RecebimentoCompraItemRepository.class),
                mock(br.com.brasil_saas.financeiro.repository.TituloRepository.class),
                mock(br.com.brasil_saas.fiscal.repository.NfeRepository.class),
                mock(br.com.brasil_saas.fiscal.repository.NfeItemRepository.class),
                mock(br.com.brasil_saas.core.service.DocumentoFluxoService.class),
                repo);
    }

    private PedidoCompra pedidoSemItens() {
        PedidoCompra p = new PedidoCompra();
        p.setEmpresaId(1L);
        p.setItens(new java.util.ArrayList<>());
        return p;
    }

    @Test
    void semRegraZeraTolerancia() throws Exception {
        var repo = mock(ToleranciaConferenciaRepository.class);
        when(repo.findByEmpresaIdAndAtivoTrueAndDeletedAtIsNullOrderById(1L)).thenReturn(List.of());
        assertEquals(0, BigDecimal.ZERO.compareTo(svc(repo).toleranciaCadastrada(1L, pedidoSemItens(), new BigDecimal("100"))));
    }

    @Test
    void regraGlobalValorFixa() throws Exception {
        var repo = mock(ToleranciaConferenciaRepository.class);
        ToleranciaConferencia t = new ToleranciaConferencia();
        t.setTipo("VALOR");
        t.setLimite(new BigDecimal("7.50"));
        when(repo.findByEmpresaIdAndAtivoTrueAndDeletedAtIsNullOrderById(1L)).thenReturn(List.of(t));
        assertEquals(0, new BigDecimal("7.50").compareTo(svc(repo).toleranciaCadastrada(1L, pedidoSemItens(), new BigDecimal("100"))));
    }

    @Test
    void regraPercentualCalculaSobrePedido() throws Exception {
        var repo = mock(ToleranciaConferenciaRepository.class);
        ToleranciaConferencia t = new ToleranciaConferencia();
        t.setTipo("PERCENTUAL");
        t.setLimite(new BigDecimal("2"));
        when(repo.findByEmpresaIdAndAtivoTrueAndDeletedAtIsNullOrderById(1L)).thenReturn(List.of(t));
        assertEquals(0, new BigDecimal("20.00").compareTo(svc(repo).toleranciaCadastrada(1L, pedidoSemItens(), new BigDecimal("1000"))));
    }
}
