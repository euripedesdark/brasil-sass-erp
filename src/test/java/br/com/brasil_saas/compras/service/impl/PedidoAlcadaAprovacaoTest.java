package br.com.brasil_saas.compras.service.impl;

import br.com.brasil_saas.compras.model.AlcadaAprovacao;
import br.com.brasil_saas.compras.model.ItemPedidoCompra;
import br.com.brasil_saas.compras.model.PedidoCompra;
import br.com.brasil_saas.compras.repository.AlcadaAprovacaoRepository;
import br.com.brasil_saas.compras.repository.PedidoCompraRepository;
import br.com.brasil_saas.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PedidoAlcadaAprovacaoTest {

    private static final Long EMPRESA = 1L;
    private PedidoCompraRepository pedidos;
    private AlcadaAprovacaoRepository alcadas;
    private PedidoCompraServiceImpl svc;

    @BeforeEach
    void setUp() {
        pedidos = mock(PedidoCompraRepository.class);
        alcadas = mock(AlcadaAprovacaoRepository.class);
        svc = new PedidoCompraServiceImpl(pedidos,
                mock(br.com.brasil_saas.cadastro.repository.FornecedorRepository.class),
                mock(br.com.brasil_saas.compras.repository.RecebimentoCompraRepository.class),
                mock(br.com.brasil_saas.compras.repository.RecebimentoCompraItemRepository.class),
                mock(br.com.brasil_saas.estoque.repository.SaldoEstoqueRepository.class),
                mock(br.com.brasil_saas.estoque.repository.DepositoRepository.class),
                mock(br.com.brasil_saas.estoque.repository.MovimentacaoEstoqueRepository.class),
                mock(br.com.brasil_saas.financeiro.repository.TituloRepository.class),
                alcadas,
                mock(br.com.brasil_saas.financeiro.service.TituloService.class),
                mock(br.com.brasil_saas.core.service.DocumentoFluxoService.class));
        when(pedidos.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    private PedidoCompra pedido(BigDecimal total, String aprov) {
        PedidoCompra p = new PedidoCompra();
        p.setId(11L);
        p.setEmpresaId(EMPRESA);
        p.setNumero("PC-1");
        p.setStatus("ABERTO");
        p.setValorTotal(total);
        p.setStatusAprovacao(aprov);
        ItemPedidoCompra it = new ItemPedidoCompra();
        it.setId(1L);
        it.setQuantidade(new BigDecimal("2"));
        it.setQuantidadeRecebida(BigDecimal.ZERO);
        p.setItens(new ArrayList<>(List.of(it)));
        when(pedidos.findByIdAndEmpresaId(11L, EMPRESA)).thenReturn(Optional.of(p));
        when(pedidos.findByIdForUpdateAndEmpresaId(11L, EMPRESA)).thenReturn(Optional.of(p));
        return p;
    }

    private void comAlcada(String limite) {
        AlcadaAprovacao a = new AlcadaAprovacao();
        a.setValorLimite(new BigDecimal(limite));
        when(alcadas.findByEmpresaIdAndAtivoTrueAndDeletedAtIsNullOrderByValorLimiteAsc(EMPRESA))
                .thenReturn(List.of(a));
    }

    @Test
    void acimaDaAlcadaSemAprovacaoBloqueiaRecebimento() {
        pedido(new BigDecimal("5000"), "PENDENTE");
        comAlcada("1000");
        assertThrows(BusinessException.class, () -> svc.receberParcial(11L, EMPRESA, Map.of(1L, BigDecimal.ONE)));
        assertThrows(BusinessException.class, () -> svc.receber(11L, EMPRESA));
    }

    @Test
    void aprovadoLiberaRecebimentoParcial() {
        PedidoCompra p = pedido(new BigDecimal("5000"), "APROVADO");
        comAlcada("1000");
        svc.receberParcial(11L, EMPRESA, Map.of(1L, BigDecimal.ONE));
        assertEquals("PARCIAL", p.getStatus());
    }

    @Test
    void rejeitadoNaoRecebe() {
        pedido(new BigDecimal("5000"), "REJEITADO");
        comAlcada("1000");
        assertThrows(BusinessException.class, () -> svc.receber(11L, EMPRESA));
    }

    @Test
    void aprovarExigePendenteERejeitarExigeMotivo() {
        PedidoCompra p = pedido(new BigDecimal("5000"), "PENDENTE");
        var out = svc.aprovar(11L, EMPRESA, 7L);
        assertEquals("APROVADO", out.statusAprovacao());
        assertEquals(7L, p.getAprovadoPor());
        assertNotNull(p.getAprovadoEm());
        pedido(new BigDecimal("5000"), "PENDENTE");
        assertThrows(BusinessException.class, () -> svc.rejeitar(11L, EMPRESA, 7L, "curto"));
        var out2 = svc.rejeitar(11L, EMPRESA, 7L, "preco muito acima do mercado");
        assertEquals("REJEITADO", out2.statusAprovacao());
        assertThrows(BusinessException.class, () -> svc.aprovar(11L, EMPRESA, 7L));
    }

    @Test
    void semAlcadaNaoExigeAprovacao() {
        PedidoCompra p = pedido(new BigDecimal("999999"), "APROVADO");
        when(alcadas.findByEmpresaIdAndAtivoTrueAndDeletedAtIsNullOrderByValorLimiteAsc(EMPRESA))
                .thenReturn(List.of());
        svc.receberParcial(11L, EMPRESA, Map.of(1L, BigDecimal.ONE));
        assertEquals("PARCIAL", p.getStatus());
    }
}
