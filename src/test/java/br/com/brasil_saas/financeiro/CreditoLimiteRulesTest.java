package br.com.brasil_saas.financeiro;

import br.com.brasil_saas.cadastro.model.*;
import br.com.brasil_saas.cadastro.repository.ClienteRepository;
import br.com.brasil_saas.financeiro.model.Titulo;
import br.com.brasil_saas.financeiro.repository.TituloRepository;
import br.com.brasil_saas.financeiro.service.CreditoService;
import br.com.brasil_saas.vendas.model.PedidoVenda;
import br.com.brasil_saas.vendas.repository.PedidoVendaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditoLimiteRulesTest {
    @Mock ClienteRepository clientes;
    @Mock TituloRepository titulos;
    @Mock PedidoVendaRepository pedidos;
    @InjectMocks CreditoService service;
    Cliente cliente() {
        var c=new Cliente();c.setLimiteCredito(new BigDecimal("1000"));
        var pessoa=new Pessoa();pessoa.setId(5L);c.setPessoa(pessoa);
        when(clientes.findByIdAndEmpresaIdAndDeletedAtIsNull(1L,2L)).thenReturn(Optional.of(c));return c;
    }
    Titulo titulo(String tipo,String status,String saldo) {
        var t=new Titulo();t.setTipo(tipo);t.setStatus(status);t.setValorSaldo(new BigDecimal(saldo));t.setDataVencimento(LocalDate.now().minusDays(1));return t;
    }
    PedidoVenda pedido(String tipo,boolean deleted) {
        var p=new PedidoVenda();p.setTipo(tipo);p.setStatus("ABERTO");p.setValorTotal(new BigDecimal("100"));if(deleted)p.setDeletedAt(LocalDateTime.now());return p;
    }
    @Test void consideraSomenteRecebiveisEPedidosAtivos() {
        cliente();when(titulos.findByEmpresaIdAndPessoaIdAndDeletedAtIsNull(2L,5L)).thenReturn(List.of(
            titulo("R","ABERTO","200"),titulo("R","PARCIAL","50"),titulo("P","ABERTO","900"),titulo("R","PAGO","300")));
        when(pedidos.findByEmpresaIdAndClienteId(2L,1L)).thenReturn(List.of(pedido("PEDIDO",false),pedido("ORCAMENTO",false),pedido("PEDIDO",true)));
        var r=service.analisar(2L,1L);assertEquals(new BigDecimal("250"),r.get("emAberto"));
        assertEquals(new BigDecimal("250"),r.get("vencido"));assertEquals(new BigDecimal("100"),r.get("emPedidos"));
        assertEquals(new BigDecimal("650"),r.get("disponivel"));assertEquals("INADIMPLENTE",r.get("situacao"));
    }
    @Test void contaAPagarNaoTornaClienteInadimplente() {
        cliente();when(titulos.findByEmpresaIdAndPessoaIdAndDeletedAtIsNull(2L,5L)).thenReturn(List.of(titulo("P","ABERTO","500")));
        assertEquals("OK",service.analisar(2L,1L).get("situacao"));
    }
    @org.junit.jupiter.params.ParameterizedTest @org.junit.jupiter.params.provider.ValueSource(strings={"0","1000"})
    void alteraLimiteComBloqueio(String valor) {
        var c=cliente();when(clientes.findForUpdate(1L,2L)).thenReturn(Optional.of(c));
        service.definirLimite(2L,1L,new BigDecimal(valor));verify(clientes).findForUpdate(1L,2L);assertEquals(new BigDecimal(valor),c.getLimiteCredito());
    }
    @Test void limiteNegativoNaoConsultaNemGrava() {
        assertThrows(ResponseStatusException.class,()->service.definirLimite(2L,1L,new BigDecimal("-1")));verifyNoInteractions(clientes,titulos,pedidos);
    }
}
