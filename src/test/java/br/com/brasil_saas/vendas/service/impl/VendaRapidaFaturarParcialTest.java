package br.com.brasil_saas.vendas.service.impl;

import br.com.brasil_saas.cadastro.model.Cliente;
import br.com.brasil_saas.cadastro.model.Pessoa;
import br.com.brasil_saas.cadastro.repository.ClienteRepository;
import br.com.brasil_saas.financeiro.dto.FinanceiroDtos;
import br.com.brasil_saas.financeiro.model.Titulo;
import br.com.brasil_saas.vendas.dto.PedidoVendaRequest;
import br.com.brasil_saas.vendas.model.ItemPedidoVenda;
import br.com.brasil_saas.vendas.model.PedidoVenda;
import br.com.brasil_saas.vendas.repository.PedidoVendaRepository;
import br.com.brasil_saas.vendas.service.PedidoVendaService;
import br.com.brasil_saas.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class VendaRapidaFaturarParcialTest {

    private static final Long EMPRESA = 1L;
    private PedidoVendaRepository pedidos;
    private ClienteRepository clientes;
    private br.com.brasil_saas.financeiro.repository.TituloRepository titulos;
    private br.com.brasil_saas.financeiro.service.TituloService tituloService;
    private PedidoVendaServiceImpl svc;
    private long seq = 100L;
    private final Map<Long, PedidoVenda> guardados = new HashMap<>();

    @BeforeEach
    void setUp() {
        pedidos = mock(PedidoVendaRepository.class);
        clientes = mock(ClienteRepository.class);
        titulos = mock(br.com.brasil_saas.financeiro.repository.TituloRepository.class);
        tituloService = mock(br.com.brasil_saas.financeiro.service.TituloService.class);
        svc = new PedidoVendaServiceImpl(pedidos, clientes,
                mock(br.com.brasil_saas.servicos.service.OrdemServicoService.class),
                mock(br.com.brasil_saas.estoque.repository.SaldoEstoqueRepository.class),
                mock(br.com.brasil_saas.estoque.repository.DepositoRepository.class),
                mock(br.com.brasil_saas.estoque.repository.LoteEstoqueRepository.class),
                mock(br.com.brasil_saas.estoque.repository.EnderecoEstoqueRepository.class),
                mock(br.com.brasil_saas.estoque.repository.MovimentacaoEstoqueRepository.class),
                mock(br.com.brasil_saas.estoque.repository.ReservaEstoqueRepository.class),
                titulos,
                mock(br.com.brasil_saas.financeiro.repository.ComissaoRepository.class),
                mock(br.com.brasil_saas.vendas.repository.RegraComissaoRepository.class),
                mock(br.com.brasil_saas.rh.repository.FuncionarioRepository.class),
                tituloService,
                mock(br.com.brasil_saas.core.service.DocumentoFluxoService.class),
                mock(br.com.brasil_saas.financeiro.service.ComissaoService.class));
        when(pedidos.save(any())).thenAnswer(i -> {
            PedidoVenda p = i.getArgument(0);
            if (p.getId() == null) p.setId(seq++);
            guardados.put(p.getId(), p);
            return p;
        });
        when(pedidos.findByIdForUpdateAndEmpresaId(any(), eq(EMPRESA))).thenAnswer(i -> Optional.ofNullable(guardados.get(i.getArgument(0))));
        when(titulos.save(any())).thenAnswer(i -> {
            Titulo t = i.getArgument(0);
            if (t.getId() == null) t.setId(500L);
            return t;
        });
        Pessoa pessoa = new Pessoa();
        pessoa.setId(9L);
        Cliente cliente = new Cliente();
        cliente.setId(3L);
        cliente.setPessoa(pessoa);
        when(clientes.findByIdAndEmpresaIdAndDeletedAtIsNull(3L, EMPRESA)).thenReturn(Optional.of(cliente));
    }

    private PedidoVenda pedidoAberto() {
        PedidoVenda p = new PedidoVenda();
        p.setId(11L);
        p.setEmpresaId(EMPRESA);
        p.setNumero("PV-1");
        p.setTipo("PEDIDO");
        p.setStatus("ABERTO");
        p.setClienteId(3L);
        p.setValorTotal(new BigDecimal("200"));
        ItemPedidoVenda it = new ItemPedidoVenda();
        it.setId(21L);
        it.setQuantidade(new BigDecimal("2"));
        it.setQuantidadeFaturada(BigDecimal.ZERO);
        it.setValorUnitario(new BigDecimal("100"));
        it.setValorDesconto(BigDecimal.ZERO);
        p.setItens(new ArrayList<>(List.of(it)));
        when(pedidos.findByIdForUpdateAndEmpresaId(11L, EMPRESA)).thenReturn(Optional.of(p));
        return p;
    }

    @Test
    void faturarParcialAcumulaEAposSegundaVaiAFaturado() {
        pedidoAberto();
        var r1 = svc.faturarParcial(11L, EMPRESA, Map.of(21L, BigDecimal.ONE));
        assertEquals("PARCIAL", r1.status());
        var r2 = svc.faturarParcial(11L, EMPRESA, Map.of(21L, BigDecimal.ONE));
        assertEquals("FATURADO", r2.status());
        assertThrows(BusinessException.class, () -> svc.faturarParcial(11L, EMPRESA, Map.of(21L, BigDecimal.ONE)));
    }

    @Test
    void faturarParcialAcimaDoRestanteFalha() {
        pedidoAberto();
        assertThrows(BusinessException.class, () -> svc.faturarParcial(11L, EMPRESA, Map.of(21L, new BigDecimal("5"))));
    }

    @Test
    void vendaRapidaFaturaEBaixaComTroco() {
        var item = new PedidoVendaService.VendaRapidaItem(null, "Servico avulso", BigDecimal.ONE, "UN", new BigDecimal("150"));
        var req = new PedidoVendaService.VendaRapidaRequest(3L, null, null, null, null,
                new BigDecimal("200"), List.of(item));
        when(tituloService.baixar(eq(EMPRESA), eq(500L), any()))
                .thenReturn(new FinanceiroDtos.BaixaResponse(1L, 500L, null, new BigDecimal("150"),
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, LocalDate.now(), BigDecimal.ZERO, "BAIXADO"));
        Map<String, Object> out = svc.vendaRapida(EMPRESA, req);
        assertEquals(0, new BigDecimal("50").compareTo((BigDecimal) out.get("troco")));
        assertEquals("BAIXADO", out.get("statusTitulo"));
    }

    @Test
    void vendaRapidaComRecebidoInsuficienteFalha() {
        var item = new PedidoVendaService.VendaRapidaItem(null, "Servico avulso", BigDecimal.ONE, "UN", new BigDecimal("150"));
        var req = new PedidoVendaService.VendaRapidaRequest(3L, null, null, null, null,
                new BigDecimal("100"), List.of(item));
        assertThrows(BusinessException.class, () -> svc.vendaRapida(EMPRESA, req));
    }
}
