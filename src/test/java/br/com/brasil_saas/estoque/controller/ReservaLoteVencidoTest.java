package br.com.brasil_saas.estoque.controller;

import br.com.brasil_saas.estoque.model.*;
import br.com.brasil_saas.estoque.repository.*;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import br.com.brasil_saas.shared.exception.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaLoteVencidoTest {
    @Mock ReservaEstoqueRepository reservas;
    @Mock SaldoEstoqueRepository saldos;
    @Mock DepositoRepository depositos;
    @Mock LoteEstoqueRepository lotes;
    @Mock EnderecoEstoqueRepository enderecos;
    @Mock MovimentacaoEstoqueRepository movimentos;
    @Mock br.com.brasil_saas.vendas.repository.PedidoVendaRepository pedidos;
    @InjectMocks ReservaEstoqueController controller;
    final AuthenticatedUser user = new AuthenticatedUser(1L, "usuario", "", true, 2L, List.of());

    private void baseOk() {
        Deposito d = new Deposito(); d.setId(3L);
        SaldoEstoque s = new SaldoEstoque(); s.setQuantidade(BigDecimal.TEN);
        when(depositos.findByIdAndEmpresaIdAndAtivoTrue(3L, 2L)).thenReturn(Optional.of(d));
        when(saldos.findForUpdate(2L, 3L, 7L)).thenReturn(Optional.of(s));
        when(reservas.sumAtivas(2L, 3L, 7L)).thenReturn(BigDecimal.ZERO);
    }

    @Test
    void loteVencidoNaoReserva() {
        baseOk();
        LoteEstoque lote = new LoteEstoque();
        lote.setId(9L); lote.setProdutoId(7L); lote.setDepositoId(3L);
        lote.setStatus("ATIVO");
        lote.setDataValidade(LocalDate.now().minusDays(1));
        lote.setQuantidade(BigDecimal.TEN);
        when(lotes.findForUpdate(2L, 9L)).thenReturn(Optional.of(lote));
        var req = new ReservaEstoqueController.Request(3L, 7L, null, 9L, null, BigDecimal.ONE, null);
        assertThrows(BusinessException.class, () -> controller.reservar(user, req));
        verify(reservas, never()).save(any());
    }

    @Test
    void loteValidoReserva() {
        baseOk();
        LoteEstoque lote = new LoteEstoque();
        lote.setId(9L); lote.setProdutoId(7L); lote.setDepositoId(3L);
        lote.setStatus("ATIVO");
        lote.setDataValidade(LocalDate.now().plusDays(30));
        lote.setQuantidade(BigDecimal.TEN);
        when(lotes.findForUpdate(2L, 9L)).thenReturn(Optional.of(lote));
        when(reservas.sumAtivasPorLote(2L, 3L, 7L, 9L)).thenReturn(BigDecimal.ZERO);
        when(reservas.save(any())).thenAnswer(i -> i.getArgument(0));
        var req = new ReservaEstoqueController.Request(3L, 7L, null, 9L, null, BigDecimal.ONE, null);
        ReservaEstoque out = controller.reservar(user, req);
        assertEquals("RESERVADA", out.getStatus());
    }
}
