package br.com.brasil_saas.compras.service;

import br.com.brasil_saas.compras.model.*;
import br.com.brasil_saas.compras.repository.*;
import br.com.brasil_saas.estoque.model.*;
import br.com.brasil_saas.estoque.repository.*;
import br.com.brasil_saas.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class DevCompraServiceTest {
    @Mock DevCompraRepository devolucoes;
    @Mock DevCompraItemRepository itens;
    @Mock PedidoCompraRepository pedidos;
    @Mock SaldoEstoqueRepository saldos;
    @Mock MovimentacaoEstoqueRepository movimentacoes;
    @Mock DepositoRepository depositos;
    @Mock ReservaEstoqueRepository reservas;
    @InjectMocks DevCompraService service;

    void pedido() {
        var p = new PedidoCompra(); p.setStatus("RECEBIDO"); p.setEmpresaId(2L);
        var i = new ItemPedidoCompra(); i.setProdutoId(6L); i.setQuantidadeRecebida(new BigDecimal("10"));
        p.setItens(new ArrayList<>(List.of(i)));
        when(pedidos.findByIdForUpdateAndEmpresaId(1L, 2L)).thenReturn(Optional.of(p));
    }
    @Test void duplicadosSaoSomadosAntesDeValidarLimite() {
        pedido();
        assertThrows(BusinessException.class, () -> service.solicitar(2L, 1L, "Defeito", List.of(
            Map.of("produtoId", 6L, "quantidade", "6"), Map.of("produtoId", 6L, "quantidade", "6"))));
        verify(devolucoes, never()).save(any()); verify(itens, never()).save(any());
    }
    @Test void agregaMesmoProdutoEmUmaLinha() {
        pedido(); when(devolucoes.save(any())).thenAnswer(i -> { DevCompra d=i.getArgument(0);d.setId(8L);return d; });
        service.solicitar(2L,1L,"Defeito",List.of(Map.of("produtoId",6L,"quantidade","2"),Map.of("produtoId",6L,"quantidade","3")));
        var captor=ArgumentCaptor.forClass(DevCompraItem.class); verify(itens).save(captor.capture());
        assertEquals(new BigDecimal("5"),captor.getValue().getQuantidade());
    }
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings={"0","-1","0.0001","erro"})
    void rejeitaQuantidadeInvalidaAntesDeGravar(String quantidade) {
        pedido(); assertThrows(BusinessException.class,()->service.solicitar(2L,1L,"Defeito",List.of(Map.of("produtoId",6L,"quantidade",quantidade))));
        verify(devolucoes,never()).save(any());
    }
    void preparada(String reservada) {
        var d=new DevCompra();d.setStatus("SOLICITADA");when(devolucoes.findForUpdate(8L,2L)).thenReturn(Optional.of(d));
        var i=new DevCompraItem();i.setProdutoId(6L);i.setQuantidade(new BigDecimal("4"));
        when(itens.findByEmpresaIdAndDevolucaoIdAndDeletedAtIsNull(2L,8L)).thenReturn(List.of(i));
        var dep=new Deposito();dep.setId(3L);dep.setCodigo("CD");
        when(depositos.findFirstByEmpresaIdAndTipoAndAtivoTrueAndDeletedAtIsNullOrderByIdAsc(2L,"PADRAO")).thenReturn(Optional.of(dep));
        when(depositos.findAtivoForUpdate(3L,2L)).thenReturn(Optional.of(dep));
        var s=new SaldoEstoque();s.setQuantidade(BigDecimal.TEN);when(saldos.findForUpdate(2L,3L,6L)).thenReturn(Optional.of(s));
        when(reservas.sumAtivas(2L,3L,6L)).thenReturn(new BigDecimal(reservada));
    }
    @Test void saidaTemSinalDepositoESaldoCorretos() {
        preparada("2");when(devolucoes.save(any())).thenAnswer(i->i.getArgument(0));
        assertEquals("DEVOLVIDA",service.devolver(2L,8L).getStatus());
        var c=ArgumentCaptor.forClass(MovimentacaoEstoque.class);verify(movimentacoes).save(c.capture());
        assertEquals(3L,c.getValue().getDepositoId());assertEquals(new BigDecimal("-4"),c.getValue().getQuantidade());
        assertEquals(new BigDecimal("6"),c.getValue().getSaldoApos());
        assertThrows(BusinessException.class,()->service.devolver(2L,8L));verify(movimentacoes,times(1)).save(any());
    }
    @Test void preservaEstoqueReservado() {
        preparada("7");assertThrows(BusinessException.class,()->service.devolver(2L,8L));
        verify(saldos,never()).save(any());verifyNoInteractions(movimentacoes);
    }
    @Test void devolverECancelarUsamMesmoBloqueio() {
        var d=new DevCompra();d.setStatus("SOLICITADA");when(devolucoes.findForUpdate(8L,2L)).thenReturn(Optional.of(d));
        when(devolucoes.save(any())).thenAnswer(i->i.getArgument(0));assertEquals("CANCELADA",service.cancelar(2L,8L).getStatus());
        assertThrows(BusinessException.class,()->service.devolver(2L,8L));verify(devolucoes,times(2)).findForUpdate(8L,2L);
    }
}
