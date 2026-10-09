package br.com.brasil_saas.vendas.service.impl;

import br.com.brasil_saas.cadastro.repository.ClienteRepository;
import br.com.brasil_saas.cadastro.model.Cliente;
import br.com.brasil_saas.cadastro.model.Pessoa;
import br.com.brasil_saas.financeiro.model.Titulo;
import br.com.brasil_saas.financeiro.repository.TituloRepository;
import br.com.brasil_saas.estoque.model.Deposito;
import br.com.brasil_saas.estoque.model.SaldoEstoque;
import br.com.brasil_saas.estoque.repository.*;
import br.com.brasil_saas.shared.exception.BusinessException;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import br.com.brasil_saas.vendas.model.*;
import br.com.brasil_saas.vendas.repository.PedidoVendaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoVendaServiceImplTest {
    @Mock PedidoVendaRepository pedidos;
    @Mock ClienteRepository clientes;
    @Mock DepositoRepository depositos;
    @Mock SaldoEstoqueRepository saldos;
    @Mock ReservaEstoqueRepository reservas;
    @Mock MovimentacaoEstoqueRepository movimentos;
    @Mock TituloRepository titulos;
    @InjectMocks PedidoVendaServiceImpl service;

    @Test
    void creditoNaoConsultaClienteDeOutraEmpresa() {
        when(clientes.findByIdAndEmpresaIdAndDeletedAtIsNull(7L, 2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.credito(2L, 7L));
        verify(clientes, never()).findById(anyLong());
    }

    @Test
    void creditoSomaApenasRecebiveisEmAbertoDaPessoa() {
        Cliente cliente = new Cliente();
        Pessoa pessoa = new Pessoa();
        pessoa.setId(8L);
        cliente.setPessoa(pessoa);
        cliente.setLimiteCredito(new BigDecimal("100"));
        Titulo receber = titulo("R", "ABERTO", "20");
        Titulo parcial = titulo("R", "PARCIAL", "10");
        Titulo pagar = titulo("P", "ABERTO", "50");
        Titulo liquidado = titulo("R", "LIQUIDADO", "30");
        when(clientes.findByIdAndEmpresaIdAndDeletedAtIsNull(7L, 2L)).thenReturn(Optional.of(cliente));
        when(titulos.findByEmpresaIdAndPessoaIdAndDeletedAtIsNull(2L, 8L))
                .thenReturn(List.of(receber, parcial, pagar, liquidado));
        var credito = service.credito(2L, 7L);
        assertEquals(new BigDecimal("30"), credito.get("emAberto"));
        assertEquals(new BigDecimal("70"), credito.get("disponivel"));
    }

    private static Titulo titulo(String tipo, String status, String valor) {
        Titulo titulo = new Titulo();
        titulo.setTipo(tipo);
        titulo.setStatus(status);
        titulo.setValorSaldo(new BigDecimal(valor));
        return titulo;
    }

    @Test
    void faturamentoNaoConsomeEstoqueReservadoParaOutroPedido() {
        PedidoVenda pedido = new PedidoVenda();
        pedido.setId(10L);
        pedido.setEmpresaId(2L);
        pedido.setStatus("ABERTO");
        pedido.setTipo("PEDIDO");
        ItemPedidoVenda item = new ItemPedidoVenda();
        item.setProdutoId(7L);
        item.setQuantidade(new BigDecimal("4"));
        item.setCriadoEstoque(false);
        pedido.setItens(List.of(item));
        Deposito deposito = new Deposito();
        deposito.setId(3L);
        SaldoEstoque saldo = new SaldoEstoque();
        saldo.setQuantidade(new BigDecimal("10"));
        when(pedidos.findByIdForUpdateAndEmpresaId(10L, 2L)).thenReturn(Optional.of(pedido));
        when(depositos.findFirstByEmpresaIdAndTipoAndAtivoTrueOrderByIdAsc(2L, "PADRAO")).thenReturn(Optional.of(deposito));
        when(saldos.findForUpdate(2L, 3L, 7L)).thenReturn(Optional.of(saldo));
        when(reservas.sumAtivasDeOutrosPedidos(2L, 3L, 7L, 10L)).thenReturn(new BigDecimal("7"));
        assertThrows(BusinessException.class, () -> service.faturar(10L, 2L, true));
        assertEquals(new BigDecimal("10"), saldo.getQuantidade());
        assertFalse(item.getCriadoEstoque());
        verify(saldos, never()).save(any());
        verifyNoInteractions(movimentos);
    }
}
