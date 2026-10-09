package br.com.brasil_saas.estoque.controller;

import br.com.brasil_saas.estoque.model.*;
import br.com.brasil_saas.estoque.repository.*;
import br.com.brasil_saas.vendas.model.*;
import br.com.brasil_saas.vendas.repository.PedidoVendaRepository;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import br.com.brasil_saas.shared.exception.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaEstoqueControllerTest {
    @Mock ReservaEstoqueRepository reservas;
    @Mock SaldoEstoqueRepository saldos;
    @Mock DepositoRepository depositos;
    @Mock LoteEstoqueRepository lotes;
    @Mock EnderecoEstoqueRepository enderecos;
    @Mock MovimentacaoEstoqueRepository movimentos;
    @Mock PedidoVendaRepository pedidos;
    @InjectMocks ReservaEstoqueController controller;
    final AuthenticatedUser user = new AuthenticatedUser(1L, "usuario", "", true, 2L, List.of());

    ReservaEstoqueController.Request request(Long pedido, Long lote, Long endereco, String quantidade) {
        return new ReservaEstoqueController.Request(3L, 7L, pedido, lote, endereco, new BigDecimal(quantidade), null);
    }
    PedidoVenda pedido(String status, String tipo) {
        PedidoVenda p = new PedidoVenda(); p.setId(10L); p.setEmpresaId(2L); p.setStatus(status); p.setTipo(tipo);
        ItemPedidoVenda i = new ItemPedidoVenda(); i.setProdutoId(7L); i.setQuantidade(new BigDecimal("12"));
        p.setItens(List.of(i)); return p;
    }
    void base(Long pedidoId) {
        Deposito d = new Deposito(); d.setId(3L);
        SaldoEstoque s = new SaldoEstoque(); s.setQuantidade(BigDecimal.TEN);
        when(depositos.findByIdAndEmpresaIdAndAtivoTrue(3L, 2L)).thenReturn(Optional.of(d));
        when(saldos.findForUpdate(2L, 3L, 7L)).thenReturn(Optional.of(s));
        if (pedidoId != null) when(pedidos.findByIdForUpdateAndEmpresaId(10L, 2L)).thenReturn(Optional.of(pedido("ABERTO", "PEDIDO")));
    }
    ReservaEstoque reserva(Long id, Long lote, Long endereco, String status) {
        ReservaEstoque r = new ReservaEstoque(); r.setId(id); r.setEmpresaId(2L); r.setPedidoVendaId(10L);
        r.setProdutoId(7L); r.setDepositoId(3L); r.setLoteId(lote); r.setEnderecoId(endereco);
        r.setQuantidade(new BigDecimal("4")); r.setStatus(status); return r;
    }
    void salvar() { when(reservas.save(any())).thenAnswer(i -> i.getArgument(0)); }

    @Test void substituicaoNaoDescontaDuasVezesAPropriaReserva() {
        base(10L); salvar(); ReservaEstoque r = reserva(20L, null, null, "RESERVADA");
        when(reservas.findByEmpresaIdAndPedidoVendaIdAndDeletedAtIsNull(2L, 10L)).thenReturn(List.of(r));
        when(reservas.findWithLockByIdAndEmpresaIdAndDeletedAtIsNull(20L, 2L)).thenReturn(Optional.of(r));
        when(reservas.sumAtivas(2L, 3L, 7L)).thenReturn(BigDecimal.TEN);
        assertSame(r, controller.reservar(user, request(10L, null, null, "4")));
        assertEquals(new BigDecimal("4"), r.getQuantidade());
        verify(reservas).save(r);
    }
    @Test void aumentoNaoUsaEstoqueReservadoPorOutros() {
        base(10L); ReservaEstoque r = reserva(20L, null, null, "RESERVADA");
        when(reservas.findByEmpresaIdAndPedidoVendaIdAndDeletedAtIsNull(2L, 10L)).thenReturn(List.of(r));
        when(reservas.findWithLockByIdAndEmpresaIdAndDeletedAtIsNull(20L, 2L)).thenReturn(Optional.of(r));
        when(reservas.sumAtivas(2L, 3L, 7L)).thenReturn(BigDecimal.TEN);
        assertThrows(BusinessException.class, () -> controller.reservar(user, request(10L, null, null, "5")));
        assertEquals(new BigDecimal("4"), r.getQuantidade()); verify(reservas, never()).save(any());
    }
    @Test void reservaAnonimaNaoSobrescreveOutraReservaAnonima() {
        base(null); salvar();
        ReservaEstoque r = controller.reservar(user, request(null, null, null, "2"));
        assertNull(r.getId()); assertNull(r.getPedidoVendaId()); assertEquals(new BigDecimal("2"), r.getQuantidade());
        verifyNoInteractions(pedidos);
        verify(reservas, never()).findByEmpresaIdAndPedidoVendaIdAndDepositoIdAndProdutoIdAndDeletedAtIsNull(any(), any(), any(), any());
    }
    @Test void pedidoDeOutraEmpresaRecusadoAntesDeBloquearEstoque() {
        assertThrows(ResourceNotFoundException.class, () -> controller.reservar(user, request(10L, null, null, "2")));
        verify(pedidos).findByIdForUpdateAndEmpresaId(10L, 2L);
        verifyNoInteractions(saldos, depositos, reservas);
    }
    @ParameterizedTest @ValueSource(strings = {"FATURADO", "CANCELADO"})
    void pedidoEncerradoNaoRecebeReserva(String status) {
        when(pedidos.findByIdForUpdateAndEmpresaId(10L, 2L)).thenReturn(Optional.of(pedido(status, "PEDIDO")));
        assertThrows(BusinessException.class, () -> controller.reservar(user, request(10L, null, null, "2")));
        verifyNoInteractions(saldos, depositos, reservas);
    }
    @Test void orcamentoNaoRecebeReservaPeloEndpointDePedido() {
        when(pedidos.findByIdForUpdateAndEmpresaId(10L, 2L)).thenReturn(Optional.of(pedido("ABERTO", "ORCAMENTO")));
        assertThrows(BusinessException.class, () -> controller.reservar(user, request(10L, null, null, "2")));
        verifyNoInteractions(saldos, depositos, reservas);
    }
    @Test void separacaoNaoRetornaAReservadaPorAtualizacaoDeQuantidade() {
        base(10L); ReservaEstoque r = reserva(20L, null, null, "SEPARACAO");
        when(reservas.findByEmpresaIdAndPedidoVendaIdAndDeletedAtIsNull(2L, 10L)).thenReturn(List.of(r));
        when(reservas.findWithLockByIdAndEmpresaIdAndDeletedAtIsNull(20L, 2L)).thenReturn(Optional.of(r));
        assertThrows(BusinessException.class, () -> controller.reservar(user, request(10L, null, null, "2")));
        assertEquals("SEPARACAO", r.getStatus()); verify(reservas, never()).save(any());
    }
    @Test void consumidaNaoPodeSerLiberada() {
        ReservaEstoque r = reserva(20L, null, null, "CONSUMIDA");
        when(reservas.findWithLockByIdAndEmpresaIdAndDeletedAtIsNull(20L, 2L)).thenReturn(Optional.of(r));
        assertThrows(BusinessException.class, () -> controller.liberar(user, 20L));
        assertEquals("CONSUMIDA", r.getStatus()); verify(reservas, never()).save(any());
    }
    @Test void quantidadeTotalReservadaNaoExcedeProdutoNoPedido() {
        base(10L); ReservaEstoque r = reserva(20L, 50L, null, "RESERVADA"); r.setQuantidade(BigDecimal.TEN);
        when(reservas.findByEmpresaIdAndPedidoVendaIdAndDeletedAtIsNull(2L, 10L)).thenReturn(List.of(r));
        assertThrows(BusinessException.class, () -> controller.reservar(user, request(10L, null, null, "3")));
        verify(reservas, never()).save(any());
    }
    @Test void loteDiferenteNoMesmoEnderecoMantemReservasIndependentes() {
        base(10L); salvar(); ReservaEstoque anterior = reserva(20L, 50L, 60L, "RESERVADA");
        when(reservas.findByEmpresaIdAndPedidoVendaIdAndDeletedAtIsNull(2L, 10L)).thenReturn(List.of(anterior));
        EnderecoEstoque e = new EnderecoEstoque(); e.setDepositoId(3L);
        when(enderecos.findByIdAndEmpresaIdAndAtivoTrue(60L, 2L)).thenReturn(Optional.of(e));
        var a = mock(MovimentacaoEstoqueRepository.EnderecoSaldo.class);
        when(a.getEnderecoId()).thenReturn(60L); when(a.getLoteId()).thenReturn(51L); when(a.getQuantidade()).thenReturn(new BigDecimal("6"));
        when(movimentos.saldosPorEndereco(2L, 3L, 7L, null)).thenReturn(List.of(a));
        when(reservas.sumAtivasPorEndereco(2L, 3L, 7L, 60L)).thenReturn(new BigDecimal("4"));
        LoteEstoque l = new LoteEstoque(); l.setProdutoId(7L); l.setDepositoId(3L); l.setQuantidade(new BigDecimal("6"));
        when(lotes.findForUpdate(2L, 51L)).thenReturn(Optional.of(l));
        ReservaEstoque nova = controller.reservar(user, request(10L, 51L, 60L, "2"));
        assertNull(nova.getId()); assertEquals(51L, nova.getLoteId()); assertEquals(50L, anterior.getLoteId());
        assertEquals(new BigDecimal("4"), anterior.getQuantidade());
    }
}
