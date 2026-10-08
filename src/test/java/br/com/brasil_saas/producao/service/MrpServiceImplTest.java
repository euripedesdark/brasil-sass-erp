package br.com.brasil_saas.producao.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import br.com.brasil_saas.compras.repository.SolicitacaoCompraRepository;
import br.com.brasil_saas.estoque.model.SaldoEstoque;
import br.com.brasil_saas.estoque.repository.ReservaEstoqueRepository;
import br.com.brasil_saas.estoque.repository.SaldoEstoqueRepository;
import br.com.brasil_saas.producao.model.EstruturaProduto;
import br.com.brasil_saas.producao.repository.EstruturaProdutoRepository;
import br.com.brasil_saas.producao.service.impl.MrpServiceImpl;
import br.com.brasil_saas.shared.exception.BusinessException;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Explosao multinivel: abatimento de estoque por nivel, perda, ciclo. Sem Spring, sem banco. */
class MrpServiceImplTest {

    private static final Long EMP = 1L;
    private EstruturaProdutoRepository bom;
    private SaldoEstoqueRepository saldo;
    private ReservaEstoqueRepository reservas;
    private MrpServiceImpl mrp;

    @BeforeEach
    void setUp() {
        bom = mock(EstruturaProdutoRepository.class);
        saldo = mock(SaldoEstoqueRepository.class);
        reservas = mock(ReservaEstoqueRepository.class);
        mrp = new MrpServiceImpl(bom, saldo, mock(ProducaoService.class), mock(SolicitacaoCompraRepository.class), reservas);
        when(bom.findByEmpresaIdAndProdutoPaiIdAndDeletedAtIsNullOrderByNivelAscIdAsc(anyLong(), anyLong()))
                .thenReturn(List.of());
        when(saldo.findByEmpresaIdAndProdutoId(anyLong(), anyLong())).thenReturn(Optional.empty());
    }

    private void filho(long pai, long filho, String qtd, String perda) {
        EstruturaProduto e = new EstruturaProduto();
        e.setProdutoPaiId(pai);
        e.setProdutoFilhoId(filho);
        e.setQuantidade(new BigDecimal(qtd));
        e.setPerdaPercentual(perda == null ? null : new BigDecimal(perda));
        e.setAtivo(true);
        List<EstruturaProduto> atual = new ArrayList<>(
                bom.findByEmpresaIdAndProdutoPaiIdAndDeletedAtIsNullOrderByNivelAscIdAsc(EMP, pai));
        atual.add(e);
        when(bom.findByEmpresaIdAndProdutoPaiIdAndDeletedAtIsNullOrderByNivelAscIdAsc(EMP, pai)).thenReturn(atual);
    }

    private void estoque(long produto, String qtd) {
        SaldoEstoque s = new SaldoEstoque();
        s.setQuantidade(new BigDecimal(qtd));
        when(saldo.findByEmpresaIdAndProdutoId(EMP, produto)).thenReturn(Optional.of(s));
    }

    private Map<String, Object> linha(List<Map<String, Object>> rows, long produto) {
        return rows.stream().filter(r -> r.get("produtoId").equals(produto)).findFirst().orElseThrow();
    }

    private static BigDecimal bd(Object o) { return (BigDecimal) o; }

    @Test
    @DisplayName("Semiacabado em estoque reduz a demanda dos seus componentes")
    void semiacabadoEmEstoque() {
        filho(1, 2, "1", "0");   // PA usa 1 semi
        filho(2, 3, "4", "0");   // semi usa 4 MP
        estoque(2, "6");         // 6 semis em estoque, pedido de 10
        var rows = mrp.simular(EMP, new MrpRequest(1L, BigDecimal.TEN));

        assertEquals(0, new BigDecimal("4").compareTo(bd(linha(rows, 2).get("necessidadeLiquida"))));
        assertEquals("PRODUZIR", linha(rows, 2).get("acao"));
        // 4 semis a produzir x 4 MP = 16 (e nao 40)
        assertEquals(0, new BigDecimal("16").compareTo(bd(linha(rows, 3).get("necessidadeBruta"))));
        assertEquals("COMPRAR", linha(rows, 3).get("acao"));
    }

    @Test
    @DisplayName("Estoque suficiente zera a necessidade e nao desce para os componentes")
    void estoqueCobreTudo() {
        filho(1, 2, "1", "0");
        estoque(1, "100");
        var rows = mrp.simular(EMP, new MrpRequest(1L, BigDecimal.TEN));
        assertEquals("SEM_ACAO", linha(rows, 1).get("acao"));
        assertEquals(1, rows.size());
    }

    @Test
    @DisplayName("Perda percentual entra no fator; perda nula e tolerada")
    void perda() {
        filho(1, 2, "2", "10");   // 2 * 1.10 = 2,2
        filho(1, 3, "1", null);   // perda nula
        var rows = mrp.simular(EMP, new MrpRequest(1L, BigDecimal.TEN));
        assertEquals(0, new BigDecimal("22").compareTo(bd(linha(rows, 2).get("necessidadeBruta"))));
        assertEquals(0, new BigDecimal("10").compareTo(bd(linha(rows, 3).get("necessidadeBruta"))));
    }

    @Test
    @DisplayName("Mesmo componente em dois ramos: estoque consumido uma unica vez")
    void estoqueNaoContadoEmDobro() {
        filho(1, 2, "1", "0");
        filho(1, 3, "1", "0");
        filho(2, 9, "1", "0");
        filho(3, 9, "1", "0");
        estoque(9, "10");        // pedido 10 => 10 + 10 = 20 de demanda, so 10 em estoque
        var rows = mrp.simular(EMP, new MrpRequest(1L, BigDecimal.TEN));
        var l = linha(rows, 9);
        assertEquals(0, new BigDecimal("20").compareTo(bd(l.get("necessidadeBruta"))));
        assertEquals(0, new BigDecimal("10").compareTo(bd(l.get("necessidadeLiquida"))));
    }

    @Test
    @DisplayName("Ciclo na BOM e recusado")
    void ciclo() {
        filho(1, 2, "1", "0");
        filho(2, 1, "1", "0");
        assertThrows(BusinessException.class, () -> mrp.simular(EMP, new MrpRequest(1L, BigDecimal.ONE)));
    }

    @Test
    @DisplayName("Quantidade invalida e recusada")
    void quantidadeInvalida() {
        assertThrows(BusinessException.class, () -> mrp.simular(EMP, new MrpRequest(1L, BigDecimal.ZERO)));
        assertThrows(BusinessException.class, () -> mrp.simular(EMP, new MrpRequest(null, BigDecimal.ONE)));
    }

    @Test
    @DisplayName("Estoque reservado para venda nao conta como disponivel para o MRP")
    void reservaReduzEstoqueLivre() {
        SaldoEstoque s = new SaldoEstoque();
        s.setDepositoId(9L);
        s.setQuantidade(new BigDecimal("10"));
        when(saldo.findByEmpresaIdAndProdutoId(EMP, 5L)).thenReturn(Optional.of(s));
        when(reservas.sumAtivas(EMP, 9L, 5L)).thenReturn(new BigDecimal("7"));
        var rows = mrp.simular(EMP, new MrpRequest(5L, BigDecimal.TEN));
        assertEquals(0, new BigDecimal("3").compareTo(bd(linha(rows, 5).get("estoqueUtilizado"))));
        assertEquals(0, new BigDecimal("7").compareTo(bd(linha(rows, 5).get("necessidadeLiquida"))));
    }

    @Test
    @DisplayName("Reserva maior que o saldo nao gera estoque negativo")
    void reservaMaiorQueSaldoNaoFicaNegativa() {
        SaldoEstoque s = new SaldoEstoque();
        s.setDepositoId(9L);
        s.setQuantidade(new BigDecimal("4"));
        when(saldo.findByEmpresaIdAndProdutoId(EMP, 5L)).thenReturn(Optional.of(s));
        when(reservas.sumAtivas(EMP, 9L, 5L)).thenReturn(new BigDecimal("9"));
        var rows = mrp.simular(EMP, new MrpRequest(5L, BigDecimal.TEN));
        assertEquals(0, BigDecimal.ZERO.compareTo(bd(linha(rows, 5).get("estoqueUtilizado"))));
        assertEquals(0, BigDecimal.TEN.compareTo(bd(linha(rows, 5).get("necessidadeLiquida"))));
    }
}
