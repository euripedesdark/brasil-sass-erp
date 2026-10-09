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
    @Mock br.com.brasil_saas.financeiro.repository.TituloRepository tituloRepository;
    @Mock br.com.brasil_saas.financeiro.repository.BaixaRepository baixaRepository;
    @Mock br.com.brasil_saas.financeiro.service.TituloService tituloService;
    @Mock br.com.brasil_saas.core.service.DocumentoFluxoService documentoFluxoService;
    @Mock br.com.brasil_saas.contabilidade.service.ContabilidadeService contabilidadeService;
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

    // ---- integracao financeira automatica (devolver) ----
    private PedidoCompra pedidoRecebidoComTitulo(Long tituloId) {
        var x = new PedidoCompra();
        x.setId(1L); x.setEmpresaId(2L); x.setFornecedorId(5L);
        x.setStatus("RECEBIDO"); x.setNumero("PC-1"); x.setTituloId(tituloId);
        x.setValorTotal(new BigDecimal("100.00"));
        var i = new ItemPedidoCompra();
        i.setProdutoId(6L); i.setQuantidade(new BigDecimal("10"));
        i.setQuantidadeRecebida(new BigDecimal("10"));
        i.setValorTotal(new BigDecimal("100.00"));
        x.setItens(new ArrayList<>(List.of(i)));
        return x;
    }
    private void devolucaoSolicitada4un() {
        var d = new DevCompra(); d.setId(8L); d.setPedidoId(1L); d.setStatus("SOLICITADA");
        when(devolucoes.findForUpdate(8L, 2L)).thenReturn(Optional.of(d));
        var i = new DevCompraItem(); i.setProdutoId(6L); i.setQuantidade(new BigDecimal("4"));
        when(itens.findByEmpresaIdAndDevolucaoIdAndDeletedAtIsNull(2L, 8L)).thenReturn(List.of(i));
        var dep = new Deposito(); dep.setId(3L); dep.setCodigo("CD");
        when(depositos.findFirstByEmpresaIdAndTipoAndAtivoTrueAndDeletedAtIsNullOrderByIdAsc(2L, "PADRAO")).thenReturn(Optional.of(dep));
        when(depositos.findAtivoForUpdate(3L, 2L)).thenReturn(Optional.of(dep));
        var s = new SaldoEstoque(); s.setQuantidade(BigDecimal.TEN);
        when(saldos.findForUpdate(2L, 3L, 6L)).thenReturn(Optional.of(s));
        when(reservas.sumAtivas(2L, 3L, 6L)).thenReturn(BigDecimal.ZERO);
        when(devolucoes.save(any())).thenAnswer(i2 -> i2.getArgument(0));
    }
    private br.com.brasil_saas.financeiro.model.Titulo tituloPagarAberto() {
        var t = new br.com.brasil_saas.financeiro.model.Titulo();
        t.setId(9L); t.setEmpresaId(2L); t.setTipo("P"); t.setStatus("ABERTO");
        t.setNumeroDocumento("PC-1"); t.setDescricao("Compra - Pedido PC-1");
        t.setPessoaId(55L);
        t.setValorOriginal(new BigDecimal("100.00")); t.setValorSaldo(new BigDecimal("100.00"));
        t.setDataEmissao(java.time.LocalDate.now().minusDays(5));
        t.setDataVencimento(java.time.LocalDate.now().plusDays(25));
        return t;
    }
    @Test void devolverComTituloAbertoReduzAPagar() {
        devolucaoSolicitada4un();
        when(pedidos.findByIdForUpdateAndEmpresaId(1L, 2L)).thenReturn(Optional.of(pedidoRecebidoComTitulo(9L)));
        when(tituloRepository.findForUpdate(9L, 2L)).thenReturn(Optional.of(tituloPagarAberto()));
        when(baixaRepository.findByTituloIdAndDeletedAtIsNull(9L)).thenReturn(List.of());
        when(contabilidadeService.espelharAjusteDevolucao(eq(2L), eq(9L), eq(9L), argThat(p -> p != null && p.compareTo(new BigDecimal("0.4")) == 0), any())).thenReturn(new br.com.brasil_saas.contabilidade.service.ContabilidadeService.EspelhoContabil(List.of(), "SEM_LANCAMENTO_ORIGINAL"));
        assertEquals("DEVOLVIDA", service.devolver(2L, 8L).getStatus());
        var captor = ArgumentCaptor.forClass(br.com.brasil_saas.financeiro.dto.FinanceiroDtos.BaixaRequest.class);
        verify(tituloService).baixar(eq(2L), eq(9L), captor.capture());
        assertEquals(new BigDecimal("40.00"), captor.getValue().valorBaixa());
        assertTrue(captor.getValue().observacao().contains("DEVOLUCAO_COMPRA#8"));
        verify(documentoFluxoService).ligar(eq(2L), eq(null),
                eq("DEVOLUCAO_COMPRA"), eq(8L), any(),
                eq("TITULO"), eq(9L), any(), eq("AJUSTE_DEVOLUCAO"));
    }
    @Test void devolverComTituloBaixadoGeraCreditoReceber() {
        devolucaoSolicitada4un();
        when(pedidos.findByIdForUpdateAndEmpresaId(1L, 2L)).thenReturn(Optional.of(pedidoRecebidoComTitulo(9L)));
        var t = tituloPagarAberto();
        t.setStatus("BAIXADO"); t.setValorSaldo(BigDecimal.ZERO);
        when(tituloRepository.findForUpdate(9L, 2L)).thenReturn(Optional.of(t));
        when(tituloRepository.findByEmpresaIdAndNumeroDocumentoAndDeletedAtIsNull(eq(2L), any())).thenReturn(List.of());
        when(contabilidadeService.espelharAjusteDevolucao(any(), any(), any(), any(), any())).thenReturn(new br.com.brasil_saas.contabilidade.service.ContabilidadeService.EspelhoContabil(List.of(), "SEM_LANCAMENTO_ORIGINAL"));
        when(tituloRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertEquals("DEVOLVIDA", service.devolver(2L, 8L).getStatus());
        verify(tituloService, never()).baixar(any(), any(), any());
        var captor = ArgumentCaptor.forClass(br.com.brasil_saas.financeiro.model.Titulo.class);
        verify(tituloRepository).save(captor.capture());
        assertEquals("R", captor.getValue().getTipo());
        assertEquals(new BigDecimal("40.00"), captor.getValue().getValorSaldo());
        assertEquals(55L, captor.getValue().getPessoaId());
        verify(documentoFluxoService).ligar(eq(2L), eq(null),
                eq("DEVOLUCAO_COMPRA"), eq(8L), any(),
                eq("TITULO"), any(), any(), eq("CREDITO_DEVOLUCAO"));
    }
    @Test void devolverSemTituloSoRegistraFluxo() {
        devolucaoSolicitada4un();
        when(pedidos.findByIdForUpdateAndEmpresaId(1L, 2L)).thenReturn(Optional.of(pedidoRecebidoComTitulo(null)));
        assertEquals("DEVOLVIDA", service.devolver(2L, 8L).getStatus());
        verifyNoInteractions(tituloService, baixaRepository);
        verify(documentoFluxoService).ligar(eq(2L), eq(null),
                eq("DEVOLUCAO_COMPRA"), eq(8L), any(),
                eq("PEDIDO_COMPRA"), eq(1L), any(), eq("DEVOLVIDA_SEM_TITULO"));
    }
    @Test void devolverRegistraAjusteContabilAplicado() {
        devolucaoSolicitada4un();
        when(pedidos.findByIdForUpdateAndEmpresaId(1L, 2L)).thenReturn(Optional.of(pedidoRecebidoComTitulo(9L)));
        when(tituloRepository.findForUpdate(9L, 2L)).thenReturn(Optional.of(tituloPagarAberto()));
        when(baixaRepository.findByTituloIdAndDeletedAtIsNull(9L)).thenReturn(List.of());
        when(contabilidadeService.espelharAjusteDevolucao(eq(2L), eq(9L), eq(9L), argThat(x -> x != null && x.compareTo(new BigDecimal("0.4")) == 0), any()))
                .thenReturn(new br.com.brasil_saas.contabilidade.service.ContabilidadeService.EspelhoContabil(List.of(), "PERIODO_FECHADO"));
        assertEquals("DEVOLVIDA", service.devolver(2L, 8L).getStatus());
        verify(documentoFluxoService).ligar(eq(2L), eq(null),
                eq("DEVOLUCAO_COMPRA"), eq(8L), any(),
                eq("TITULO"), eq(9L), eq("PERIODO_FECHADO"), eq("AJUSTE_CONTABIL_PENDENTE"));
    }
}
