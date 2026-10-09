package br.com.brasil_saas.vendas.service.impl;

import br.com.brasil_saas.cadastro.repository.ClienteRepository;
import br.com.brasil_saas.cadastro.model.Cliente;
import br.com.brasil_saas.cadastro.model.Pessoa;
import br.com.brasil_saas.financeiro.model.Titulo;
import br.com.brasil_saas.financeiro.repository.TituloRepository;
import br.com.brasil_saas.estoque.model.Deposito;
import br.com.brasil_saas.estoque.model.SaldoEstoque;
import br.com.brasil_saas.estoque.model.ReservaEstoque;
import br.com.brasil_saas.core.service.DocumentoFluxoService;
import br.com.brasil_saas.financeiro.service.TituloService;
import br.com.brasil_saas.estoque.repository.*;
import br.com.brasil_saas.shared.exception.BusinessException;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import br.com.brasil_saas.vendas.model.*;
import br.com.brasil_saas.vendas.repository.PedidoVendaRepository;
import br.com.brasil_saas.vendas.dto.PedidoVendaRequest;
import br.com.brasil_saas.vendas.dto.ItemPedidoVendaRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
    @Mock LoteEstoqueRepository lotes;
    @Mock EnderecoEstoqueRepository enderecos;
    @Mock TituloRepository titulos;
    @Mock TituloService tituloService;
    @Mock DocumentoFluxoService fluxo;
    @InjectMocks PedidoVendaServiceImpl service;

    private static PedidoVendaRequest novoPedido(String tipo, String status) {
        return new PedidoVendaRequest(2L, 7L, null, tipo, status, null, null, null, null,
                null, null, null, null, null, null,
                List.of(new ItemPedidoVendaRequest(1, 7L, null, "Produto", BigDecimal.ONE, "UN", BigDecimal.TEN, null)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"FATURADO", "CANCELADO", "DESCONHECIDO"})
    void criacaoNaoPodeSimularEtapaDoFluxo(String status) {
        assertThrows(BusinessException.class, () -> service.criar(novoPedido("PEDIDO", status)));
        verifyNoInteractions(pedidos, clientes, saldos, movimentos, titulos);
    }

    @Test
    void tipoDesconhecidoRecusadoAntesDePersistir() {
        assertThrows(BusinessException.class, () -> service.criar(novoPedido("QUALQUER", "ABERTO")));
        verifyNoInteractions(pedidos, clientes);
    }

    @Test
    void criacaoNormalizaTipoEStatusSemPularFaturamento() {
        when(clientes.findByIdAndEmpresaIdAndDeletedAtIsNull(7L, 2L)).thenReturn(Optional.of(new Cliente()));
        when(pedidos.save(any(PedidoVenda.class))).thenAnswer(invocation -> {
            PedidoVenda pedido = invocation.getArgument(0);
            pedido.setId(10L);
            return pedido;
        });
        var resposta = service.criar(novoPedido(" orcamento ", " aberto "));
        assertEquals("ORCAMENTO", resposta.tipo());
        assertEquals("ABERTO", resposta.status());
        assertEquals(BigDecimal.TEN, resposta.valorTotal());
        verifyNoInteractions(saldos, movimentos, titulos);
    }

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
        when(depositos.findFirstByEmpresaIdAndTipoAndAtivoTrueAndDeletedAtIsNullOrderByIdAsc(2L, "PADRAO")).thenReturn(Optional.of(deposito));
        when(depositos.findByIdAndEmpresaIdAndAtivoTrue(3L, 2L)).thenReturn(Optional.of(deposito));
        when(saldos.findForUpdate(2L, 3L, 7L)).thenReturn(Optional.of(saldo));
        when(reservas.sumAtivasDeOutrosPedidos(2L, 3L, 7L, 10L)).thenReturn(new BigDecimal("7"));
        assertThrows(BusinessException.class, () -> service.faturar(10L, 2L, true));
        assertEquals(new BigDecimal("10"), saldo.getQuantidade());
        assertFalse(item.getCriadoEstoque());
        verify(saldos, never()).save(any());
        verifyNoInteractions(movimentos);
    }

    @ParameterizedTest
    @ValueSource(strings = {"RESERVADA", "SEPARACAO"})
    void faturamentoBaixaEstoqueConsomeReservaEGeraRecebivel(String statusReserva) {
        PedidoVenda pedido = new PedidoVenda();
        pedido.setId(10L);
        pedido.setEmpresaId(2L);
        pedido.setClienteId(7L);
        pedido.setNumero("PV-10");
        pedido.setStatus("ABERTO");
        pedido.setTipo("PEDIDO");
        pedido.setValorTotal(new BigDecimal("100.00"));
        ItemPedidoVenda item = new ItemPedidoVenda();
        item.setProdutoId(7L);
        item.setQuantidade(new BigDecimal("4"));
        item.setCriadoEstoque(false);
        pedido.setItens(List.of(item));
        Deposito deposito = new Deposito();
        deposito.setId(3L);
        SaldoEstoque saldo = new SaldoEstoque();
        saldo.setQuantidade(new BigDecimal("10"));
        ReservaEstoque reserva = new ReservaEstoque();
        reserva.setProdutoId(7L);
        reserva.setDepositoId(3L);
        reserva.setQuantidade(new BigDecimal("2"));
        reserva.setStatus(statusReserva);
        ReservaEstoque segundaReserva = new ReservaEstoque();
        segundaReserva.setProdutoId(7L);
        segundaReserva.setDepositoId(3L);
        segundaReserva.setQuantidade(new BigDecimal("2"));
        segundaReserva.setStatus(statusReserva);
        Cliente cliente = new Cliente();
        Pessoa pessoa = new Pessoa();
        pessoa.setId(8L);
        cliente.setPessoa(pessoa);
        when(pedidos.findByIdForUpdateAndEmpresaId(10L, 2L)).thenReturn(Optional.of(pedido));
        when(depositos.findByIdAndEmpresaIdAndAtivoTrue(3L, 2L)).thenReturn(Optional.of(deposito));
        when(saldos.findForUpdate(2L, 3L, 7L)).thenReturn(Optional.of(saldo));
        when(reservas.findByPedidoForUpdate(2L, 10L)).thenReturn(List.of(reserva, segundaReserva));
        when(clientes.findByIdAndEmpresaIdAndDeletedAtIsNull(7L, 2L)).thenReturn(Optional.of(cliente));
        when(titulos.save(any(Titulo.class))).thenAnswer(invocation -> {
            Titulo titulo = invocation.getArgument(0);
            titulo.setId(88L);
            return titulo;
        });
        service.faturar(10L, 2L, true);
        assertEquals(new BigDecimal("6"), saldo.getQuantidade());
        assertTrue(item.getCriadoEstoque());
        assertEquals("CONSUMIDA", reserva.getStatus());
        assertEquals("CONSUMIDA", segundaReserva.getStatus());
        assertEquals("FATURADO", pedido.getStatus());
        assertEquals(88L, pedido.getTituloId());
        var captor = ArgumentCaptor.forClass(Titulo.class);
        verify(titulos).save(captor.capture());
        assertEquals("R", captor.getValue().getTipo());
        assertEquals(8L, captor.getValue().getPessoaId());
        assertEquals(new BigDecimal("100.00"), captor.getValue().getValorSaldo());
        verify(tituloService).gerarParcelas(2L, 88L, null);
        verify(reservas).save(reserva);
        verify(reservas).save(segundaReserva);
        verify(movimentos, times(2)).save(any());
        verify(pedidos).save(pedido);
    }

    private PedidoVenda prepararFaturamento(String... quantidades) {
        PedidoVenda p = new PedidoVenda(); p.setId(10L); p.setEmpresaId(2L); p.setClienteId(7L);
        p.setNumero("PV-10"); p.setStatus("ABERTO"); p.setTipo("PEDIDO"); p.setValorTotal(new BigDecimal("100.00"));
        var itens = new java.util.ArrayList<ItemPedidoVenda>();
        for (String q : quantidades) {
            ItemPedidoVenda i = new ItemPedidoVenda(); i.setProdutoId(7L); i.setQuantidade(new BigDecimal(q)); i.setCriadoEstoque(false); itens.add(i);
        }
        p.setItens(itens);
        when(pedidos.findByIdForUpdateAndEmpresaId(10L, 2L)).thenReturn(Optional.of(p));
        return p;
    }
    private void prepararFinanceiro() {
        Cliente c = new Cliente(); Pessoa pessoa = new Pessoa(); pessoa.setId(8L); c.setPessoa(pessoa);
        when(clientes.findByIdAndEmpresaIdAndDeletedAtIsNull(7L, 2L)).thenReturn(Optional.of(c));
        when(titulos.save(any(Titulo.class))).thenAnswer(i -> { Titulo t = i.getArgument(0); t.setId(88L); return t; });
    }
    private SaldoEstoque saldo(Long depositoId, String quantidade) {
        Deposito d = new Deposito(); d.setId(depositoId);
        SaldoEstoque s = new SaldoEstoque(); s.setQuantidade(new BigDecimal(quantidade));
        when(depositos.findByIdAndEmpresaIdAndAtivoTrue(depositoId, 2L)).thenReturn(Optional.of(d));
        when(saldos.findForUpdate(2L, depositoId, 7L)).thenReturn(Optional.of(s)); return s;
    }
    private ReservaEstoque reserva(Long depositoId, String quantidade, String status) {
        ReservaEstoque r = new ReservaEstoque(); r.setEmpresaId(2L); r.setPedidoVendaId(10L);
        r.setProdutoId(7L); r.setDepositoId(depositoId); r.setQuantidade(new BigDecimal(quantidade)); r.setStatus(status); return r;
    }
    @Test void faturamentoUsaCadaDepositoReservadoEAgregaItensDoMesmoProduto() {
        PedidoVenda p = prepararFaturamento("1", "3"); prepararFinanceiro();
        SaldoEstoque a = saldo(3L, "10"), b = saldo(4L, "8");
        ReservaEstoque r1 = reserva(3L, "2", "RESERVADA"), r2 = reserva(4L, "2", "SEPARACAO");
        when(reservas.findByPedidoForUpdate(2L, 10L)).thenReturn(List.of(r2, r1));
        service.faturar(10L, 2L, true);
        assertEquals(new BigDecimal("8"), a.getQuantidade()); assertEquals(new BigDecimal("6"), b.getQuantidade());
        assertTrue(p.getItens().stream().allMatch(i -> Boolean.TRUE.equals(i.getCriadoEstoque())));
        var captor = ArgumentCaptor.forClass(br.com.brasil_saas.estoque.model.MovimentacaoEstoque.class);
        verify(movimentos, times(2)).save(captor.capture());
        assertEquals(List.of(3L, 4L), captor.getAllValues().stream().map(m -> m.getDepositoId()).toList());
        assertEquals(new BigDecimal("-4"), captor.getAllValues().stream().map(m -> m.getQuantidade()).reduce(BigDecimal.ZERO, BigDecimal::add));
        assertEquals("CONSUMIDA", r1.getStatus()); assertEquals("CONSUMIDA", r2.getStatus());
        verify(titulos).save(any()); verify(tituloService).gerarParcelas(2L, 88L, null);
    }
    @Test void excessoDeReservaRecusadoAntesDeQualquerBaixa() {
        PedidoVenda p = prepararFaturamento("4");
        when(reservas.findByPedidoForUpdate(2L, 10L)).thenReturn(List.of(reserva(3L, "5", "RESERVADA")));
        assertThrows(BusinessException.class, () -> service.faturar(10L, 2L, true));
        assertFalse(p.getItens().get(0).getCriadoEstoque()); verifyNoInteractions(saldos, movimentos, titulos);
    }
    @Test void expedicaoConcluidaAntesDaFaturaAindaBaixaOEstoqueUmaVez() {
        prepararFaturamento("4"); prepararFinanceiro(); SaldoEstoque s = saldo(3L, "10");
        ReservaEstoque r = reserva(3L, "4", "CONSUMIDA");
        when(reservas.findByPedidoForUpdate(2L, 10L)).thenReturn(List.of(r));
        service.faturar(10L, 2L, true);
        assertEquals(new BigDecimal("6"), s.getQuantidade()); verify(reservas, never()).save(any());
        assertThrows(BusinessException.class, () -> service.faturar(10L, 2L, true));
        verify(movimentos).save(any()); verify(titulos).save(any());
    }
    @Test void reservaParcialCompletaSomenteODiferencialNoDepositoPadrao() {
        prepararFaturamento("4"); prepararFinanceiro(); SaldoEstoque s = saldo(3L, "10");
        Deposito d = new Deposito(); d.setId(3L);
        when(depositos.findFirstByEmpresaIdAndTipoAndAtivoTrueAndDeletedAtIsNullOrderByIdAsc(2L, "PADRAO")).thenReturn(Optional.of(d));
        when(reservas.findByPedidoForUpdate(2L, 10L)).thenReturn(List.of(reserva(3L, "1", "RESERVADA")));
        service.faturar(10L, 2L, true); assertEquals(new BigDecimal("6"), s.getQuantidade());
        var captor = ArgumentCaptor.forClass(br.com.brasil_saas.estoque.model.MovimentacaoEstoque.class);
        verify(movimentos, times(2)).save(captor.capture());
        assertEquals(List.of(new BigDecimal("-1"), new BigDecimal("-3")), captor.getAllValues().stream().map(m -> m.getQuantidade()).toList());
    }
    @Test void faturamentoPreservaLoteEnderecoESaldoFisicoDaReserva() {
        prepararFaturamento("4"); prepararFinanceiro(); SaldoEstoque s = saldo(3L, "10");
        ReservaEstoque r = reserva(3L, "4", "SEPARACAO"); r.setLoteId(50L); r.setEnderecoId(60L);
        when(reservas.findByPedidoForUpdate(2L, 10L)).thenReturn(List.of(r));
        var e = new br.com.brasil_saas.estoque.model.EnderecoEstoque(); e.setDepositoId(3L);
        when(enderecos.findByIdAndEmpresaIdAndAtivoTrue(60L, 2L)).thenReturn(Optional.of(e));
        var pos = mock(MovimentacaoEstoqueRepository.EnderecoSaldo.class);
        when(pos.getEnderecoId()).thenReturn(60L); when(pos.getLoteId()).thenReturn(50L); when(pos.getQuantidade()).thenReturn(new BigDecimal("8"));
        when(movimentos.saldosPorEndereco(2L, 3L, 7L, null)).thenReturn(List.of(pos));
        var lote = new br.com.brasil_saas.estoque.model.LoteEstoque(); lote.setProdutoId(7L); lote.setDepositoId(3L); lote.setQuantidade(new BigDecimal("8"));
        when(lotes.findForUpdate(2L, 50L)).thenReturn(Optional.of(lote));
        service.faturar(10L, 2L, true);
        assertEquals(new BigDecimal("4"), lote.getQuantidade()); assertEquals(new BigDecimal("6"), s.getQuantidade());
        var captor = ArgumentCaptor.forClass(br.com.brasil_saas.estoque.model.MovimentacaoEstoque.class);
        verify(movimentos).save(captor.capture()); assertEquals(50L, captor.getValue().getLoteId()); assertEquals(60L, captor.getValue().getEnderecoId());
        verify(lotes).save(lote);
    }
    @Test void quantidadeFisicaDeLoteNaoPodeCobrirReservaDeOutroPedido() {
        prepararFaturamento("4"); saldo(3L, "10"); ReservaEstoque r = reserva(3L, "4", "RESERVADA"); r.setLoteId(50L);
        when(reservas.findByPedidoForUpdate(2L, 10L)).thenReturn(List.of(r));
        var lote = new br.com.brasil_saas.estoque.model.LoteEstoque(); lote.setProdutoId(7L); lote.setDepositoId(3L); lote.setQuantidade(new BigDecimal("8"));
        when(lotes.findForUpdate(2L, 50L)).thenReturn(Optional.of(lote));
        when(reservas.sumAtivasDeOutrosPedidosPorLote(2L, 3L, 7L, 50L, 10L)).thenReturn(new BigDecimal("5"));
        assertThrows(BusinessException.class, () -> service.faturar(10L, 2L, true));
        assertEquals(new BigDecimal("8"), lote.getQuantidade()); verify(lotes, never()).save(any()); verifyNoInteractions(titulos);
    }

    @Test
    void cancelamentoBloqueiaPedidoELiberaReservas() {
        PedidoVenda pedido = new PedidoVenda();
        pedido.setId(10L);
        pedido.setStatus("ABERTO");
        ReservaEstoque reserva = new ReservaEstoque();
        reserva.setStatus("SEPARACAO");
        when(pedidos.findByIdForUpdateAndEmpresaId(10L, 2L)).thenReturn(Optional.of(pedido));
        when(reservas.findByEmpresaIdAndPedidoVendaIdAndDeletedAtIsNull(2L, 10L)).thenReturn(List.of(reserva));
        service.cancelar(10L, 2L);
        assertEquals("LIBERADA", reserva.getStatus());
        assertEquals("CANCELADO", pedido.getStatus());
        verify(pedidos, never()).findByIdAndEmpresaId(anyLong(), anyLong());
        verify(reservas).save(reserva);
        verify(pedidos).save(pedido);
    }
}
