package br.com.brasil_saas.database;

import br.com.brasil_saas.cadastro.repository.ClienteRepository;
import br.com.brasil_saas.core.repository.DocumentoFluxoRepository;
import br.com.brasil_saas.core.service.DocumentoFluxoService;
import br.com.brasil_saas.estoque.model.ReservaEstoque;
import br.com.brasil_saas.estoque.repository.*;
import br.com.brasil_saas.financeiro.repository.*;
import br.com.brasil_saas.financeiro.service.impl.TituloServiceImpl;
import br.com.brasil_saas.shared.exception.BusinessException;
import br.com.brasil_saas.vendas.model.*;
import br.com.brasil_saas.vendas.repository.PedidoVendaRepository;
import br.com.brasil_saas.vendas.service.impl.PedidoVendaServiceImpl;
import org.hibernate.Session;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/** Fluxo real, dentro da transacao descartavel do teste de bootstrap PostgreSQL. */
final class VendasReservasPostgresScenario {
    static void validar(Session session) {
        long empresa = 900001L;
        session.doWork(connection -> {
            try (var s = connection.createStatement()) {
                s.executeUpdate("insert into brasil_saas.bc_cad_produto(id,empresa_id,codigo,nome) values(900001,900001,'TESTE','Produto sintetico')");
                s.executeUpdate("insert into brasil_saas.bc_est_deposito(id,empresa_id,codigo,nome) values(900001,900001,'D1','Deposito sintetico 1'),(900002,900001,'D2','Deposito sintetico 2')");
                s.executeUpdate("insert into brasil_saas.bc_est_endereco(id,empresa_id,deposito_id,codigo) values(900001,900001,900001,'E1')");
                s.executeUpdate("insert into brasil_saas.bc_est_lote(id,empresa_id,produto_id,deposito_id,codigo,quantidade) values(900001,900001,900001,900001,'L1',8)");
                s.executeUpdate("insert into brasil_saas.bc_est_saldo(empresa_id,deposito_id,produto_id,quantidade) values(900001,900001,900001,10),(900001,900002,900001,10)");
                s.executeUpdate("insert into brasil_saas.bc_est_movimentacao(empresa_id,produto_id,deposito_id,endereco_id,lote_id,tipo,quantidade,saldo_apos) values(900001,900001,900001,900001,900001,'ENTRADA',8,10)");
            }
        });
        var factory = new JpaRepositoryFactory(session);
        var pedidos = factory.getRepository(PedidoVendaRepository.class);
        var reservas = factory.getRepository(ReservaEstoqueRepository.class);
        var saldos = factory.getRepository(SaldoEstoqueRepository.class);
        var movimentos = factory.getRepository(MovimentacaoEstoqueRepository.class);
        var lotes = factory.getRepository(LoteEstoqueRepository.class);
        var titulos = factory.getRepository(TituloRepository.class);
        var parcelas = factory.getRepository(TituloParcelaRepository.class);
        var documentos = factory.getRepository(DocumentoFluxoRepository.class);
        var tituloService = new TituloServiceImpl(titulos, parcelas, null, null, null, null, null);
        var service = new PedidoVendaServiceImpl(pedidos, factory.getRepository(ClienteRepository.class), null,
                saldos, factory.getRepository(DepositoRepository.class), lotes,
                factory.getRepository(EnderecoEstoqueRepository.class), movimentos, reservas, titulos,
                null, null, null, tituloService, new DocumentoFluxoService(documentos));

        var pedido = new PedidoVenda();
        pedido.setEmpresaId(empresa); pedido.setClienteId(empresa); pedido.setNumero("PV-SINTETICO");
        pedido.setTipo("PEDIDO"); pedido.setStatus("ABERTO"); pedido.setDataEmissao(LocalDate.of(2026, 10, 9));
        pedido.setValorTotal(new BigDecimal("100.00"));
        for (int n = 1; n <= 2; n++) {
            var item = new ItemPedidoVenda(); item.setEmpresaId(empresa); item.setPedido(pedido);
            item.setNumeroItem(n); item.setProdutoId(empresa); item.setQuantidade(new BigDecimal("2"));
            item.setValorUnitario(new BigDecimal("25")); item.setValorTotal(new BigDecimal("50"));
            item.setCriadoEstoque(false); pedido.getItens().add(item);
        }
        pedidos.save(pedido);
        var primeira = reserva(empresa, 900001L, pedido.getId(), "SEPARACAO", "2");
        primeira.setLoteId(empresa); primeira.setEnderecoId(empresa); reservas.save(primeira);
        var segunda = reserva(empresa, 900002L, pedido.getId(), "RESERVADA", "2"); reservas.save(segunda);
        var anonima = reserva(empresa, 900001L, null, "RESERVADA", "3"); reservas.save(anonima);
        var anonimaLote = reserva(empresa, 900001L, null, "RESERVADA", "2");
        anonimaLote.setLoteId(empresa); anonimaLote.setEnderecoId(empresa); reservas.save(anonimaLote);
        var encerrada = reserva(empresa, 900001L, null, "CANCELADA", "100"); reservas.save(encerrada);
        session.flush();
        assertEquals(2, reservas.findByPedidoForUpdate(empresa, pedido.getId()).size());
        assertTrue(reservas.findByPedidoForUpdate(900002L, pedido.getId()).isEmpty());
        igual("5", reservas.sumAtivasDeOutrosPedidos(empresa, empresa, empresa, pedido.getId()));
        igual("2", reservas.sumAtivasDeOutrosPedidosPorLote(empresa, empresa, empresa, empresa, pedido.getId()));
        igual("2", reservas.sumAtivasDeOutrosPedidosPorEndereco(empresa, empresa, empresa, empresa, null, pedido.getId()));
        igual("2", reservas.sumAtivasDeOutrosPedidosPorEndereco(empresa, empresa, empresa, empresa, empresa, pedido.getId()));

        service.faturar(pedido.getId(), empresa, true);
        session.flush(); session.clear();
        var faturado = pedidos.findByIdAndEmpresaId(pedido.getId(), empresa).orElseThrow();
        assertEquals("FATURADO", faturado.getStatus());
        assertTrue(faturado.getItens().stream().allMatch(i -> Boolean.TRUE.equals(i.getCriadoEstoque())));
        igual("8", saldos.findForUpdate(empresa, 900001L, empresa).orElseThrow().getQuantidade());
        igual("8", saldos.findForUpdate(empresa, 900002L, empresa).orElseThrow().getQuantidade());
        igual("6", lotes.findForUpdate(empresa, empresa).orElseThrow().getQuantidade());
        var posicoes = movimentos.saldosPorEndereco(empresa, empresa, empresa, null);
        assertEquals(1, posicoes.size()); igual("6", posicoes.get(0).getQuantidade());
        assertTrue(reservas.findByPedidoForUpdate(empresa, pedido.getId()).stream().allMatch(r -> "CONSUMIDA".equals(r.getStatus())));
        assertEquals("RESERVADA", reservas.findById(anonima.getId()).orElseThrow().getStatus());
        var titulo = titulos.findById(faturado.getTituloId()).orElseThrow();
        assertEquals("R", titulo.getTipo()); igual("100", titulo.getValorOriginal()); igual("100", titulo.getValorSaldo());
        var recebiveis = parcelas.findByTituloIdAndDeletedAtIsNullOrderByNumeroParcela(titulo.getId());
        assertEquals(1, recebiveis.size()); igual("100", recebiveis.get(0).getValorSaldo());
        assertEquals(titulo.getDataVencimento(), recebiveis.get(0).getDataVencimento());
        assertEquals(2, documentos.porDocumento(empresa, "PEDIDO_VENDA", pedido.getId()).size());
        assertThrows(BusinessException.class, () -> service.faturar(pedido.getId(), empresa, true));
        var saidas = movimentos.findByEmpresaIdAndProdutoIdAndDeletedAtIsNullOrderByDataMovimentoDesc(empresa, empresa)
                .stream().filter(m -> "SAIDA".equals(m.getTipo())).toList();
        assertEquals(2, saidas.size()); igual("-4", saidas.stream().map(m -> m.getQuantidade()).reduce(BigDecimal.ZERO, BigDecimal::add));
        FinanceiroContabilidadePostgresScenario.validar(session, titulo.getId());
        DevolucoesCreditoPostgresScenario.validar(session, pedido.getId());
    }

    private static ReservaEstoque reserva(Long empresa, Long deposito, Long pedido, String status, String quantidade) {
        var r = new ReservaEstoque(); r.setEmpresaId(empresa); r.setDepositoId(deposito); r.setProdutoId(empresa);
        r.setPedidoVendaId(pedido); r.setQuantidade(new BigDecimal(quantidade)); r.setStatus(status); return r;
    }
    private static void igual(String esperado, BigDecimal atual) { assertEquals(0, new BigDecimal(esperado).compareTo(atual)); }
}
