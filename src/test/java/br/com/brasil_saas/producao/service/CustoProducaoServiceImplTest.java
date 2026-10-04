package br.com.brasil_saas.producao.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.brasil_saas.cadastro.model.Produto;
import br.com.brasil_saas.cadastro.repository.ProdutoRepository;
import br.com.brasil_saas.producao.model.ApontamentoProducao;
import br.com.brasil_saas.producao.model.ItemProducao;
import br.com.brasil_saas.producao.model.Producao;
import br.com.brasil_saas.producao.repository.ApontamentoProducaoRepository;
import br.com.brasil_saas.producao.repository.ProducaoRepository;
import br.com.brasil_saas.producao.service.impl.CustoProducaoServiceImpl;
import br.com.brasil_saas.rh.model.Funcionario;
import br.com.brasil_saas.rh.repository.FuncionarioRepository;
import br.com.brasil_saas.shared.exception.BusinessException;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CustoProducaoServiceImplTest {
    private static final Long EMP = 1L;
    private ProducaoRepository producoes;
    private ApontamentoProducaoRepository apontamentos;
    private ProdutoRepository produtos;
    private FuncionarioRepository funcionarios;
    private CustoProducaoServiceImpl svc;
    private Producao op;

    @BeforeEach
    void setUp() {
        producoes = mock(ProducaoRepository.class);
        apontamentos = mock(ApontamentoProducaoRepository.class);
        produtos = mock(ProdutoRepository.class);
        funcionarios = mock(FuncionarioRepository.class);
        svc = new CustoProducaoServiceImpl(producoes, apontamentos, produtos, funcionarios);

        op = new Producao();
        op.setId(50L);
        op.setEmpresaId(EMP);
        op.setQuantidadePlanejada(new BigDecimal("100"));
        op.setItens(new ArrayList<>());
        when(producoes.findById(50L)).thenReturn(Optional.of(op));
        when(apontamentos.findByEmpresaIdAndProducaoId(EMP, 50L)).thenReturn(List.of());
    }

    private void item(long produto, String qtd, String custoItem, String precoProduto) {
        ItemProducao it = new ItemProducao();
        it.setProdutoId(produto);
        it.setQuantidade(new BigDecimal(qtd));
        if (custoItem != null) it.setCustoUnitario(new BigDecimal(custoItem));
        op.getItens().add(it);
        Produto p = new Produto();
        p.setPrecoCusto(precoProduto == null ? BigDecimal.ZERO : new BigDecimal(precoProduto));
        when(produtos.findByIdAndEmpresaIdAndDeletedAtIsNull(produto, EMP)).thenReturn(Optional.of(p));
    }

    private void apontamento(Long func, String horas, String produzido, String refugo) {
        ApontamentoProducao a = new ApontamentoProducao();
        a.setFuncionarioId(func);
        a.setHorasTrabalhadas(new BigDecimal(horas));
        a.setQuantidadeProduzida(new BigDecimal(produzido));
        a.setQuantidadeRefugo(new BigDecimal(refugo));
        a.setStatus("FINALIZADO");
        when(apontamentos.findByEmpresaIdAndProducaoId(EMP, 50L))
                .thenReturn(new ArrayList<>(List.of(a)));
    }

    private void funcionario(long id, String valorHora) {
        Funcionario f = new Funcionario();
        f.setEmpresaId(EMP);
        f.setValorHora(new BigDecimal(valorHora));
        when(funcionarios.findById(id)).thenReturn(Optional.of(f));
    }

    @Test
    void materiaisEmaoDeObra() {
        item(1, "10", null, "5");
        item(2, "4", "2.5", "99");
        funcionario(7, "20");
        apontamento(7L, "5", "50", "10");

        var r = svc.calcular(EMP, 50L);
        assertEquals(0, new BigDecimal("60").compareTo(r.custoMateriais()));
        assertEquals(0, new BigDecimal("100").compareTo(r.custoMaoDeObra()));
        assertEquals(0, new BigDecimal("160").compareTo(r.custoTotal()));
        assertEquals(0, new BigDecimal("40").compareTo(r.quantidadeBoa()));
        assertEquals(0, new BigDecimal("4").compareTo(r.custoUnitario()));
    }

    @Test
    void semApontamentoUsaPlanejada() {
        item(1, "10", null, "10");
        var r = svc.calcular(EMP, 50L);
        assertEquals(0, new BigDecimal("100").compareTo(r.quantidadeBoa()));
        assertEquals(0, new BigDecimal("1").compareTo(r.custoUnitario()));
        assertFalse(r.alertas().isEmpty());
    }

    @Test
    void alertasNaoQuebramCalculo() {
        item(1, "10", null, "0");
        funcionario(7, "0");
        apontamento(7L, "2", "10", "0");
        var r = svc.calcular(EMP, 50L);
        assertEquals(0, BigDecimal.ZERO.compareTo(r.custoTotal()));
        assertTrue(r.alertas().stream().anyMatch(a -> a.contains("sem custo")));
        assertTrue(r.alertas().stream().anyMatch(a -> a.contains("valor/hora")));
    }

    @Test
    void canceladoNaoEntra() {
        item(1, "1", null, "1");
        ApontamentoProducao ativo = new ApontamentoProducao();
        ativo.setFuncionarioId(null);
        ativo.setHorasTrabalhadas(BigDecimal.ZERO);
        ativo.setQuantidadeProduzida(new BigDecimal("10"));
        ativo.setQuantidadeRefugo(BigDecimal.ZERO);
        ativo.setStatus("FINALIZADO");
        ApontamentoProducao cancelado = new ApontamentoProducao();
        cancelado.setHorasTrabalhadas(new BigDecimal("8"));
        cancelado.setQuantidadeProduzida(new BigDecimal("999"));
        cancelado.setQuantidadeRefugo(BigDecimal.ZERO);
        cancelado.setStatus("CANCELADO");
        when(apontamentos.findByEmpresaIdAndProducaoId(EMP, 50L))
                .thenReturn(List.of(ativo, cancelado));

        var r = svc.calcular(EMP, 50L);
        assertEquals(0, new BigDecimal("10").compareTo(r.quantidadeApontada()));
    }

    @Test
    void outraEmpresaRejeitada() {
        op.setEmpresaId(2L);
        assertThrows(BusinessException.class, () -> svc.calcular(EMP, 50L));
    }
}
