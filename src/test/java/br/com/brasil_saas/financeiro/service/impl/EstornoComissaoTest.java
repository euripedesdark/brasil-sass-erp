package br.com.brasil_saas.financeiro.service.impl;

import br.com.brasil_saas.financeiro.model.Comissao;
import br.com.brasil_saas.financeiro.repository.ComissaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EstornoComissaoTest {

    private ComissaoRepository comissoes;
    private ComissaoServiceImpl svc;

    @BeforeEach
    void setUp() {
        comissoes = mock(ComissaoRepository.class);
        svc = new ComissaoServiceImpl(comissoes,
                mock(br.com.brasil_saas.vendas.repository.RegraComissaoRepository.class),
                mock(br.com.brasil_saas.rh.repository.FuncionarioRepository.class));
        when(comissoes.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    private Comissao comissao(String status, String valor) {
        Comissao c = new Comissao();
        c.setId(5L);
        c.setEmpresaId(1L);
        c.setFuncionarioId(8L);
        c.setPedidoId(11L);
        c.setValorVenda(new BigDecimal("1000"));
        c.setPercentual(new BigDecimal("5"));
        c.setValorComissao(new BigDecimal(valor));
        c.setStatus(status);
        return c;
    }

    @Test
    void pendenteViraEstornada() {
        Comissao c = comissao("PENDENTE", "50");
        when(comissoes.findByEmpresaIdAndPedidoId(1L, 11L)).thenReturn(List.of(c));
        assertEquals(1, svc.estornarPorPedido(1L, 11L, "Cancelamento"));
        assertEquals("ESTORNADA", c.getStatus());
    }

    @Test
    void pagaGeraAjusteNegativo() {
        Comissao c = comissao("PAGO", "50");
        when(comissoes.findByEmpresaIdAndPedidoId(1L, 11L)).thenReturn(List.of(c));
        assertEquals(1, svc.estornarPorPedido(1L, 11L, "Devolucao"));
        assertEquals("ESTORNADA", c.getStatus());
        var cap = org.mockito.ArgumentCaptor.forClass(Comissao.class);
        verify(comissoes, times(2)).save(cap.capture());
        Comissao ajuste = cap.getAllValues().stream().filter(x -> x != c).findFirst().orElseThrow();
        assertEquals(0, new BigDecimal("-50").compareTo(ajuste.getValorComissao()));
        assertEquals("PENDENTE", ajuste.getStatus());
    }

    @Test
    void jaEstornadaNaoConta() {
        Comissao c = comissao("ESTORNADA", "50");
        when(comissoes.findByEmpresaIdAndPedidoId(1L, 11L)).thenReturn(List.of(c));
        assertEquals(0, svc.estornarPorPedido(1L, 11L, "x"));
        verify(comissoes, never()).save(any());
    }
}
