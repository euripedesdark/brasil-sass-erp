package br.com.brasil_saas.vendas.devolucao;

import br.com.brasil_saas.estoque.repository.DepositoRepository;
import br.com.brasil_saas.estoque.repository.MovimentacaoEstoqueRepository;
import br.com.brasil_saas.estoque.repository.SaldoEstoqueRepository;
import br.com.brasil_saas.vendas.model.ItemPedidoVenda;
import br.com.brasil_saas.vendas.model.PedidoVenda;
import br.com.brasil_saas.vendas.repository.PedidoVendaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import br.com.brasil_saas.estoque.model.Deposito;
import br.com.brasil_saas.estoque.model.SaldoEstoque;
import br.com.brasil_saas.estoque.model.MovimentacaoEstoque;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
    @Mock br.com.brasil_saas.financeiro.repository.TituloRepository tituloRepository;
    @Mock br.com.brasil_saas.financeiro.repository.BaixaRepository baixaRepository;
    @Mock br.com.brasil_saas.financeiro.service.TituloService tituloService;
    @Mock br.com.brasil_saas.core.service.DocumentoFluxoService documentoFluxoService;
    @Mock br.com.brasil_saas.contabilidade.service.ContabilidadeService contabilidadeService;
    @InjectMocks DevolucaoService service;

    void pedidoFaturadoComDezUnidades() {
        var pedido = new PedidoVenda();
        pedido.setId(1L); pedido.setEmpresaId(2L); pedido.setClienteId(7L); pedido.setStatus("FATURADO");
        var item = new ItemPedidoVenda();
        item.setProdutoId(6L); item.setQuantidade(new BigDecimal("10"));
        pedido.setItens(new java.util.ArrayList<>(List.of(item)));
        when(pedidos.findByIdForUpdateAndEmpresaId(1L, 2L)).thenReturn(Optional.of(pedido));
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
        pedidoFaturadoComDezUnidades();
        jaDevolvido("7");
        var ex = assertThrows(ResponseStatusException.class,
                () -> service.solicitar(2L, 1L, "Defeito", Map.of(6L, new BigDecimal("4"))));
        assertEquals(422, ex.getStatusCode().value());
        verify(itens, never()).save(any());
    }

    @Test
    void segundaDevolucaoAteOSaldoRestanteEAceita() {
        pedidoFaturadoComDezUnidades();
        jaDevolvido("7");
        when(devolucoes.save(any())).thenAnswer(i -> { VenDevolucao d = i.getArgument(0); d.setId(51L); return d; });
        var d = service.solicitar(2L, 1L, "Defeito", Map.of(6L, new BigDecimal("3")));
        assertEquals("SOLICITADA", d.getStatus());
        assertEquals(7L, d.getClienteId()); assertTrue(d.getNumero().startsWith("DV-")); assertTrue(d.getNumero().length() <= 30);
        verify(itens).save(any());
    }

    @Test
    void motivoMaiorQueOLimiteDaColunaERejeitado() {
        pedidoFaturadoComDezUnidades();
        assertThrows(ResponseStatusException.class,
                () -> service.solicitar(2L, 1L, "x".repeat(101), Map.of(6L, BigDecimal.ONE)));
        verify(devolucoes, never()).save(any());
    }

    @Test
    void pedidoDeOutraEmpresaNaoEEncontrado() {
        assertThrows(ResponseStatusException.class,
                () -> service.solicitar(99L, 1L, "Defeito", Map.of(6L, BigDecimal.ONE)));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void recebeNoDepositoSelecionadoMesmoComCodigoDiferenteDePadrao(boolean saldoExistente) {
        VenDevolucao devolucao = new VenDevolucao();
        devolucao.setId(70L);
        devolucao.setPedidoId(1L);
        devolucao.setStatus("APROVADA");
        var item = new VenDevolucaoItem();
        item.setProdutoId(6L);
        item.setQuantidade(new BigDecimal("4"));
        var deposito = new Deposito();
        deposito.setId(3L);
        deposito.setCodigo("CD-PRINCIPAL");
        deposito.setTipo("PADRAO");
        var saldo = new SaldoEstoque();
        saldo.setEmpresaId(2L);
        saldo.setDepositoId(3L);
        saldo.setProdutoId(6L);
        saldo.setQuantidade(new BigDecimal("10"));
        when(devolucoes.findByIdForUpdate(70L, 2L)).thenReturn(Optional.of(devolucao));
        when(depositos.findFirstByEmpresaIdAndTipoAndAtivoTrueAndDeletedAtIsNullOrderByIdAsc(2L, "PADRAO"))
                .thenReturn(Optional.of(deposito));
        when(depositos.findAtivoForUpdate(3L, 2L)).thenReturn(Optional.of(deposito));
        when(itens.findByDevolucaoIdAndEmpresaIdAndDeletedAtIsNull(70L, 2L)).thenReturn(List.of(item));
        when(saldos.findForUpdate(2L, 3L, 6L)).thenReturn(saldoExistente ? Optional.of(saldo) : Optional.empty());
        when(devolucoes.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        assertEquals("RECEBIDA", service.receber(2L, 70L).getStatus());
        assertEquals(new BigDecimal("4"), item.getQtdRecebida());
        var saldoCaptor = ArgumentCaptor.forClass(SaldoEstoque.class);
        verify(saldos).save(saldoCaptor.capture());
        assertEquals(3L, saldoCaptor.getValue().getDepositoId());
        assertEquals(6L, saldoCaptor.getValue().getProdutoId());
        assertEquals(new BigDecimal(saldoExistente ? "14" : "4"), saldoCaptor.getValue().getQuantidade());
        var movimentoCaptor = ArgumentCaptor.forClass(MovimentacaoEstoque.class);
        verify(movimentacoes).save(movimentoCaptor.capture());
        assertEquals(3L, movimentoCaptor.getValue().getDepositoId());
        assertEquals("DEVOLUCAO", movimentoCaptor.getValue().getOrigem());
        assertEquals(70L, movimentoCaptor.getValue().getOrigemId());
        verify(saldos, never()).findByEmpresaIdAndProdutoIdForUpdate(anyLong(), anyLong());
        assertThrows(ResponseStatusException.class, () -> service.receber(2L, 70L));
        verify(movimentacoes, times(1)).save(any());
    }

    @Test
    void recebimentoSemDepositoAtivoNaoAlteraEstoque() {
        VenDevolucao devolucao = new VenDevolucao();
        devolucao.setStatus("APROVADA");
        when(devolucoes.findByIdForUpdate(70L, 2L)).thenReturn(Optional.of(devolucao));
        var erro = assertThrows(ResponseStatusException.class, () -> service.receber(2L, 70L));
        assertEquals(422, erro.getStatusCode().value());
        verifyNoInteractions(saldos, movimentacoes, itens);
    }

    @Test
    void decisaoBloqueiaDevolucaoENaoPodeSerRepetida() {
        VenDevolucao devolucao = new VenDevolucao();
        devolucao.setStatus("SOLICITADA");
        when(devolucoes.findByIdForUpdate(70L, 2L)).thenReturn(Optional.of(devolucao));
        when(devolucoes.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        assertEquals("APROVADA", service.decidir(2L, 5L, 70L, true).getStatus());
        assertEquals(5L, devolucao.getDecididaPor());
        assertNotNull(devolucao.getDecididaEm());
        assertThrows(ResponseStatusException.class, () -> service.decidir(2L, 5L, 70L, false));
        verify(devolucoes, times(1)).save(any());
    }
    @ParameterizedTest @ValueSource(strings={"0", "-1", "0.0001", "11"})
    void quantidadeInvalidaNaoGravaSolicitacao(String quantidade) {
        pedidoFaturadoComDezUnidades();
        assertThrows(ResponseStatusException.class,()->service.solicitar(2L,1L,"Defeito",Map.of(6L,new BigDecimal(quantidade))));
        verify(devolucoes,never()).save(any()); verify(itens,never()).save(any());
    }


    // ---- integracao financeira automatica (receber) ----
    private PedidoVenda pedidoFaturadoComTitulo(Long tituloId) {
        var pedido = new PedidoVenda();
        pedido.setId(1L); pedido.setEmpresaId(2L); pedido.setClienteId(7L);
        pedido.setStatus("FATURADO"); pedido.setNumero("PV-1"); pedido.setTituloId(tituloId);
        pedido.setValorTotal(new BigDecimal("100.00"));
        var item = new ItemPedidoVenda();
        item.setProdutoId(6L); item.setQuantidade(new BigDecimal("10"));
        item.setValorTotal(new BigDecimal("100.00"));
        pedido.setItens(new java.util.ArrayList<>(List.of(item)));
        return pedido;
    }
    private VenDevolucao devolucaoAprovada4un() {
        var d = new VenDevolucao();
        d.setId(70L); d.setPedidoId(1L); d.setStatus("APROVADA");
        d.setNumero("DV-TESTE-70"); d.setClienteId(7L);
        return d;
    }
    private void estoqueParaReceber4un() {
        var item = new VenDevolucaoItem();
        item.setProdutoId(6L); item.setQuantidade(new BigDecimal("4"));
        var deposito = new Deposito();
        deposito.setId(3L); deposito.setCodigo("CD-PRINCIPAL"); deposito.setTipo("PADRAO");
        var saldo = new SaldoEstoque();
        saldo.setEmpresaId(2L); saldo.setDepositoId(3L); saldo.setProdutoId(6L);
        saldo.setQuantidade(new BigDecimal("10"));
        when(depositos.findFirstByEmpresaIdAndTipoAndAtivoTrueAndDeletedAtIsNullOrderByIdAsc(2L, "PADRAO"))
                .thenReturn(Optional.of(deposito));
        when(depositos.findAtivoForUpdate(3L, 2L)).thenReturn(Optional.of(deposito));
        when(itens.findByDevolucaoIdAndEmpresaIdAndDeletedAtIsNull(70L, 2L)).thenReturn(List.of(item));
        when(saldos.findForUpdate(2L, 3L, 6L)).thenReturn(Optional.of(saldo));
        when(devolucoes.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }
    private br.com.brasil_saas.financeiro.model.Titulo tituloAbertoCom100() {
        var t = new br.com.brasil_saas.financeiro.model.Titulo();
        t.setId(9L); t.setEmpresaId(2L); t.setTipo("R"); t.setStatus("ABERTO");
        t.setNumeroDocumento("PV-1"); t.setDescricao("Venda - Pedido PV-1");
        t.setPessoaId(77L);
        t.setValorOriginal(new BigDecimal("100.00")); t.setValorSaldo(new BigDecimal("100.00"));
        t.setDataEmissao(java.time.LocalDate.now().minusDays(5));
        t.setDataVencimento(java.time.LocalDate.now().plusDays(25));
        return t;
    }
    @Test
    void receberComTituloAbertoGeraBaixaProporcional() {
        when(devolucoes.findByIdForUpdate(70L, 2L)).thenReturn(Optional.of(devolucaoAprovada4un()));
        estoqueParaReceber4un();
        when(pedidos.findByIdForUpdateAndEmpresaId(1L, 2L)).thenReturn(Optional.of(pedidoFaturadoComTitulo(9L)));
        var titulo = tituloAbertoCom100();
        when(tituloRepository.findForUpdate(9L, 2L)).thenReturn(Optional.of(titulo));
        when(baixaRepository.findByTituloIdAndDeletedAtIsNull(9L)).thenReturn(List.of());
        when(contabilidadeService.espelharAjusteDevolucao(eq(2L), eq(9L), eq(9L), argThat(p -> p != null && p.compareTo(new BigDecimal("0.4")) == 0), any())).thenReturn(new br.com.brasil_saas.contabilidade.service.ContabilidadeService.EspelhoContabil(List.of(), "SEM_LANCAMENTO_ORIGINAL"));
        assertEquals("RECEBIDA", service.receber(2L, 70L).getStatus());
        var captor = ArgumentCaptor.forClass(br.com.brasil_saas.financeiro.dto.FinanceiroDtos.BaixaRequest.class);
        verify(tituloService).baixar(eq(2L), eq(9L), captor.capture());
        assertEquals(new BigDecimal("40.00"), captor.getValue().valorBaixa());
        assertTrue(captor.getValue().observacao().contains("DEVOLUCAO_VENDA#70"));
        verify(documentoFluxoService).ligar(eq(2L), eq(null),
                eq("DEVOLUCAO_VENDA"), eq(70L), any(),
                eq("TITULO"), eq(9L), any(), eq("AJUSTE_DEVOLUCAO"));
        verify(tituloRepository, never()).save(argThat(t ->
                t instanceof br.com.brasil_saas.financeiro.model.Titulo
                        && "P".equals(((br.com.brasil_saas.financeiro.model.Titulo) t).getTipo())));
    }
    @Test
    void receberComTituloBaixadoGeraTituloRestituicao() {
        when(devolucoes.findByIdForUpdate(70L, 2L)).thenReturn(Optional.of(devolucaoAprovada4un()));
        estoqueParaReceber4un();
        when(pedidos.findByIdForUpdateAndEmpresaId(1L, 2L)).thenReturn(Optional.of(pedidoFaturadoComTitulo(9L)));
        var titulo = tituloAbertoCom100();
        titulo.setStatus("BAIXADO"); titulo.setValorSaldo(BigDecimal.ZERO);
        when(tituloRepository.findForUpdate(9L, 2L)).thenReturn(Optional.of(titulo));
        when(tituloRepository.findByEmpresaIdAndNumeroDocumentoAndDeletedAtIsNull(eq(2L), any())).thenReturn(List.of());
        when(contabilidadeService.espelharAjusteDevolucao(any(), any(), any(), any(), any())).thenReturn(new br.com.brasil_saas.contabilidade.service.ContabilidadeService.EspelhoContabil(List.of(), "SEM_LANCAMENTO_ORIGINAL"));
        when(tituloRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertEquals("RECEBIDA", service.receber(2L, 70L).getStatus());
        verify(tituloService, never()).baixar(any(), any(), any());
        var captor = ArgumentCaptor.forClass(br.com.brasil_saas.financeiro.model.Titulo.class);
        verify(tituloRepository).save(captor.capture());
        assertEquals("P", captor.getValue().getTipo());
        assertEquals(new BigDecimal("40.00"), captor.getValue().getValorSaldo());
        assertEquals(77L, captor.getValue().getPessoaId());
        verify(documentoFluxoService).ligar(eq(2L), eq(null),
                eq("DEVOLUCAO_VENDA"), eq(70L), any(),
                eq("TITULO"), any(), any(), eq("RESTITUICAO_DEVOLUCAO"));
    }
    @Test
    void receberSemTituloSoRegistraFluxo() {
        when(devolucoes.findByIdForUpdate(70L, 2L)).thenReturn(Optional.of(devolucaoAprovada4un()));
        estoqueParaReceber4un();
        when(pedidos.findByIdForUpdateAndEmpresaId(1L, 2L)).thenReturn(Optional.of(pedidoFaturadoComTitulo(null)));
        assertEquals("RECEBIDA", service.receber(2L, 70L).getStatus());
        verifyNoInteractions(tituloService, baixaRepository);
        verify(documentoFluxoService).ligar(eq(2L), eq(null),
                eq("DEVOLUCAO_VENDA"), eq(70L), any(),
                eq("PEDIDO_VENDA"), eq(1L), any(), eq("RECEBIDA_SEM_TITULO"));
    }
    @Test
    void receberNaoDuplicaBaixaJaExistente() {
        when(devolucoes.findByIdForUpdate(70L, 2L)).thenReturn(Optional.of(devolucaoAprovada4un()));
        estoqueParaReceber4un();
        when(pedidos.findByIdForUpdateAndEmpresaId(1L, 2L)).thenReturn(Optional.of(pedidoFaturadoComTitulo(9L)));
        var titulo = tituloAbertoCom100();
        titulo.setStatus("PARCIAL"); titulo.setValorSaldo(new BigDecimal("60.00"));
        when(tituloRepository.findForUpdate(9L, 2L)).thenReturn(Optional.of(titulo));
        when(contabilidadeService.espelharAjusteDevolucao(any(), any(), any(), any(), any())).thenReturn(new br.com.brasil_saas.contabilidade.service.ContabilidadeService.EspelhoContabil(List.of(), "SEM_LANCAMENTO_ORIGINAL"));
        var existente = new br.com.brasil_saas.financeiro.model.Baixa();
        existente.setObservacao("DEVOLUCAO_VENDA#70 DV-TESTE-70");
        when(baixaRepository.findByTituloIdAndDeletedAtIsNull(9L)).thenReturn(List.of(existente));
        assertEquals("RECEBIDA", service.receber(2L, 70L).getStatus());
        verify(tituloService, never()).baixar(any(), any(), any());
        verify(documentoFluxoService).ligar(eq(2L), eq(null),
                eq("DEVOLUCAO_VENDA"), eq(70L), any(),
                eq("TITULO"), eq(9L), any(), eq("AJUSTE_DEVOLUCAO"));
    }
    @Test
    void receberRegistraAjusteContabilAplicado() {
        when(devolucoes.findByIdForUpdate(70L, 2L)).thenReturn(Optional.of(devolucaoAprovada4un()));
        estoqueParaReceber4un();
        when(pedidos.findByIdForUpdateAndEmpresaId(1L, 2L)).thenReturn(Optional.of(pedidoFaturadoComTitulo(9L)));
        when(tituloRepository.findForUpdate(9L, 2L)).thenReturn(Optional.of(tituloAbertoCom100()));
        when(baixaRepository.findByTituloIdAndDeletedAtIsNull(9L)).thenReturn(List.of());
        var lancado = new br.com.brasil_saas.contabilidade.model.CtbLancamento();
        lancado.setId(55L); lancado.setPeriodo("2026-10");
        when(contabilidadeService.espelharAjusteDevolucao(eq(2L), eq(9L), eq(9L), argThat(p -> p != null && p.compareTo(new BigDecimal("0.4")) == 0), any()))
                .thenReturn(new br.com.brasil_saas.contabilidade.service.ContabilidadeService.EspelhoContabil(List.of(lancado), "APLICADO"));
        assertEquals("RECEBIDA", service.receber(2L, 70L).getStatus());
        verify(documentoFluxoService).ligar(eq(2L), eq(null),
                eq("DEVOLUCAO_VENDA"), eq(70L), any(),
                eq("TITULO"), eq(9L), any(), eq("AJUSTE_CONTABIL"));
    }
}
