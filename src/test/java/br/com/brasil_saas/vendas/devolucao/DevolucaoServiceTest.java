package br.com.brasil_saas.vendas.devolucao;

import br.com.brasil_saas.estoque.repository.DepositoRepository;
import br.com.brasil_saas.estoque.repository.MovimentacaoEstoqueRepository;
import br.com.brasil_saas.estoque.repository.SaldoEstoqueRepository;
import br.com.brasil_saas.vendas.model.ItemPedidoVenda;
import br.com.brasil_saas.vendas.model.PedidoVenda;
import br.com.brasil_saas.vendas.repository.PedidoVendaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DevolucaoServiceTest {
    @Mock VenDevolucaoRepository devolucoes;
    @Mock VenDevolucaoItemRepository itens;
    @Mock PedidoVendaRepository pedidos;
    @Mock SaldoEstoqueRepository saldos;
    @Mock MovimentacaoEstoqueRepository movimentacoes;
    @Mock DepositoRepository depositos;
    @InjectMocks DevolucaoService service;

    @BeforeEach
    void pedidoFaturadoComDezUnidades() {
        var pedido = new PedidoVenda();
        pedido.setId(1L); pedido.setEmpresaId(2L); pedido.setStatus("FATURADO");
        var item = new ItemPedidoVenda();
        item.setProdutoId(6L); item.setQuantidade(new BigDecimal("10"));
        pedido.setItens(new java.util.ArrayList<>(List.of(item)));
        when(pedidos.findByIdForUpdate(1L)).thenReturn(Optional.of(pedido));
    }

    private void jaDevolvido(String qtd) {
        var anterior = new VenDevolucao(); anterior.setId(50L);
        when(devolucoes.findByPedidoIdAndEmpresaIdAndStatusInAndDeletedAtIsNull(eq(1L), eq(2L), any()))
                .thenReturn(List.of(anterior));
        var x = new VenDevolucaoItem(); x.setProdutoId(6L); x.setQuantidade(new BigDecimal(qtd));
        when(itens.findByDevolucaoIdAndEmpresaIdAndDeletedAtIsNull(50L, 2L)).thenReturn(List.of(x));
    }

    @Test
    void segundaDevolucaoNaoPodeSomarMaisQueOVendido() {
        jaDevolvido("7");
        var ex = assertThrows(ResponseStatusException.class,
                () -> service.solicitar(2L, 1L, "Defeito", Map.of(6L, new BigDecimal("4"))));
        assertEquals(422, ex.getStatusCode().value());
        verify(itens, never()).save(any());
    }

    @Test
    void segundaDevolucaoAteOSaldoRestanteEAceita() {
        jaDevolvido("7");
        when(devolucoes.save(any())).thenAnswer(i -> { VenDevolucao d = i.getArgument(0); d.setId(51L); return d; });
        var d = service.solicitar(2L, 1L, "Defeito", Map.of(6L, new BigDecimal("3")));
        assertEquals("SOLICITADA", d.getStatus());
        verify(itens).save(any());
    }

    @Test
    void motivoMaiorQueOLimiteDaColunaERejeitado() {
        assertThrows(ResponseStatusException.class,
                () -> service.solicitar(2L, 1L, "x".repeat(101), Map.of(6L, BigDecimal.ONE)));
        verify(devolucoes, never()).save(any());
    }

    @Test
    void pedidoDeOutraEmpresaNaoEEncontrado() {
        assertThrows(ResponseStatusException.class,
                () -> service.solicitar(99L, 1L, "Defeito", Map.of(6L, BigDecimal.ONE)));
    }
}
