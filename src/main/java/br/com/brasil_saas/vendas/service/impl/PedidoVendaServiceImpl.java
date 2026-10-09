package br.com.brasil_saas.vendas.service.impl;

import br.com.brasil_saas.estoque.model.MovimentacaoEstoque;
import br.com.brasil_saas.estoque.model.SaldoEstoque;
import br.com.brasil_saas.estoque.repository.MovimentacaoEstoqueRepository;
import br.com.brasil_saas.estoque.repository.SaldoEstoqueRepository;
import br.com.brasil_saas.estoque.repository.DepositoRepository;
import br.com.brasil_saas.estoque.model.ReservaEstoque;
import br.com.brasil_saas.estoque.repository.ReservaEstoqueRepository;
import br.com.brasil_saas.financeiro.model.Titulo;
import br.com.brasil_saas.financeiro.model.Comissao;
import br.com.brasil_saas.financeiro.repository.ComissaoRepository;
import br.com.brasil_saas.financeiro.repository.TituloRepository;
import br.com.brasil_saas.rh.repository.FuncionarioRepository;
import br.com.brasil_saas.cadastro.repository.ClienteRepository;
import br.com.brasil_saas.servicos.service.OrdemServicoService;
import br.com.brasil_saas.shared.exception.BusinessException;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import br.com.brasil_saas.vendas.dto.PedidoVendaRequest;
import br.com.brasil_saas.vendas.dto.PedidoVendaResponse;
import br.com.brasil_saas.vendas.model.ItemPedidoVenda;
import br.com.brasil_saas.vendas.model.PedidoVenda;
import br.com.brasil_saas.vendas.model.RegraComissao;
import br.com.brasil_saas.vendas.repository.RegraComissaoRepository;
import br.com.brasil_saas.vendas.repository.PedidoVendaRepository;
import br.com.brasil_saas.vendas.service.CalculoDesconto;
import br.com.brasil_saas.vendas.service.PedidoVendaService;
import br.com.brasil_saas.core.service.DocumentoFluxoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PedidoVendaServiceImpl implements PedidoVendaService {

    private final PedidoVendaRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final OrdemServicoService osService;
    private final SaldoEstoqueRepository saldoEstoqueRepository;
    private final DepositoRepository depositoRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;
    private final ReservaEstoqueRepository reservaEstoqueRepository;
    private final TituloRepository tituloRepository;
    private final ComissaoRepository comissaoRepository;
    private final RegraComissaoRepository regraComissaoRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final br.com.brasil_saas.financeiro.service.TituloService tituloService;
    private final DocumentoFluxoService documentoFluxoService;

    @Override
    @Transactional
    public PedidoVendaResponse criar(PedidoVendaRequest request) {
        PedidoVenda pedido = new PedidoVenda();
        if (request.empresaId() == null) throw new BusinessException("Empresa obrigatoria");
        if (request.itens() == null || request.itens().isEmpty()) throw new BusinessException("Pedido precisa possuir ao menos um item");
        pedido.setEmpresaId(request.empresaId());
        if (request.clienteId() == null || clienteRepository.findByIdAndEmpresaIdAndDeletedAtIsNull(request.clienteId(), request.empresaId()).isEmpty()) throw new ResourceNotFoundException("Cliente nao encontrado");
        pedido.setClienteId(request.clienteId());
        pedido.setVendedorId(request.vendedorId());
        pedido.setTipo(request.tipo() != null ? request.tipo() : "PEDIDO");
        pedido.setStatus(request.status() != null ? request.status() : "ABERTO");
        pedido.setDataEmissao(request.dataEmissao() != null ? request.dataEmissao() : LocalDate.now());
        pedido.setDataEntrega(request.dataEntrega());
        pedido.setCondicaoPagamentoId(request.condicaoPagamentoId());
        pedido.setTabelaPrecoId(request.tabelaPrecoId());
        pedido.setCanalVenda(request.canalVenda());
        pedido.setOrigem(request.origem());
        pedido.setObservacao(request.observacao());

        List<ItemPedidoVenda> itens = new ArrayList<>();
        List<CalculoDesconto.Item> paraCalculo = new ArrayList<>();

        if (request.itens() != null) {
            int num = 1;
            for (var itemReq : request.itens()) {
                if (itemReq.quantidade() == null || itemReq.quantidade().signum() <= 0) throw new BusinessException("Quantidade deve ser maior que zero");
                if (itemReq.valorUnitario() == null || itemReq.valorUnitario().signum() < 0) throw new BusinessException("Valor unitario invalido");
                ItemPedidoVenda item = new ItemPedidoVenda();
                item.setPedido(pedido);
                item.setEmpresaId(pedido.getEmpresaId());
                item.setNumeroItem(itemReq.numeroItem() != null ? itemReq.numeroItem() : num++);
                item.setProdutoId(itemReq.produtoId());
                item.setServicoId(itemReq.servicoId());
                item.setDescricao(itemReq.descricao());
                item.setQuantidade(itemReq.quantidade());
                item.setUnidade(itemReq.unidade());
                item.setValorUnitario(itemReq.valorUnitario());
                item.setValorDesconto(itemReq.valorDesconto() != null ? itemReq.valorDesconto() : BigDecimal.ZERO);
                item.setValorTotal(itemReq.quantidade().multiply(itemReq.valorUnitario()).subtract(item.getValorDesconto()));
                item.setCriadoEstoque(false);
                itens.add(item);
                paraCalculo.add(new CalculoDesconto.Item(itemReq.quantidade(), itemReq.valorUnitario(), item.getValorDesconto()));
            }
        }

        CalculoDesconto calculo = CalculoDesconto.de(paraCalculo, request.percentualDesconto(), request.valorDesconto(), request.valorFrete());
        pedido.setItens(itens);
        pedido.setValorProdutos(calculo.getValorProdutos());
        pedido.setValorServicos(calculo.getValorServicos());
        pedido.setValorDesconto(calculo.getValorDescontoTotal());
        pedido.setPercentualDesconto(calculo.getPercentualDesconto());
        pedido.setValorFrete(calculo.getValorFrete());
        pedido.setValorTotal(calculo.getValorTotal());
        if (pedido.getNumero() == null || pedido.getNumero().isBlank()) {
            pedido.setNumero("PV-" + System.currentTimeMillis());
        }
        PedidoVenda salvo = pedidoRepository.save(pedido);
        return PedidoVendaResponse.from(salvo);
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoVendaResponse buscarPorId(Long id, Long empresaId) {
        PedidoVenda pedido = pedidoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido de venda nao encontrado"));
        return PedidoVendaResponse.from(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoVendaResponse> listarPorEmpresa(Long empresaId) {
        return pedidoRepository.findByEmpresaIdOrderByDataEmissaoDesc(empresaId).stream()
                .map(PedidoVendaResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public void confirmar(Long id, Long empresaId) {
        PedidoVenda pedido = pedidoBloqueado(id, empresaId);
        if (!"ORCAMENTO".equalsIgnoreCase(pedido.getTipo())) {
            throw new BusinessException("Somente orcamentos podem ser confirmados");
        }
        if (!"ABERTO".equals(pedido.getStatus())) {
            throw new BusinessException("Somente orcamentos ABERTOS podem ser confirmados");
        }
        if (pedido.getItens() == null || pedido.getItens().isEmpty()) {
            throw new BusinessException("O orcamento precisa possuir ao menos um item");
        }
        String numAntes = pedido.getNumero();
        pedido.setTipo("PEDIDO");
        pedido.setStatus("ABERTO");
        pedidoRepository.save(pedido);
        documentoFluxoService.ligar(empresaId, null,
                "ORCAMENTO", pedido.getId(), numAntes,
                "PEDIDO_VENDA", pedido.getId(), pedido.getNumero(),
                "CONVERTE");
        reservarEstoqueDoPedido(pedido);
    }

    @Override
    @Transactional
    public void faturar(Long id, Long empresaId) {
        faturar(id, empresaId, false);
    }

    @Override
    @Transactional
    public void faturar(Long id, Long empresaId, boolean forcar) {
        PedidoVenda pedido = pedidoBloqueado(id, empresaId);
        if (!"ABERTO".equals(pedido.getStatus()) || !"PEDIDO".equalsIgnoreCase(pedido.getTipo())) {
            throw new BusinessException("Somente pedidos ABERTOS podem ser faturados");
        }
        if (forcar == false) validarCredito(pedido);

        for (ItemPedidoVenda item : pedido.getItens()) {
            if (item.getProdutoId() != null && !Boolean.TRUE.equals(item.getCriadoEstoque())) {
                baixarEstoque(pedido.getEmpresaId(), item.getProdutoId(), item.getQuantidade(), pedido.getId());
                item.setCriadoEstoque(true);
                reservaEstoqueRepository
                        .findByEmpresaIdAndPedidoVendaIdAndDeletedAtIsNull(pedido.getEmpresaId(), pedido.getId())
                        .stream()
                        .filter(reserva -> item.getProdutoId().equals(reserva.getProdutoId()) && "RESERVADA".equals(reserva.getStatus()))
                        .findFirst()
                        .ifPresent(reserva -> {
                            reserva.setStatus("CONSUMIDA");
                            reservaEstoqueRepository.save(reserva);
                        });
            }
        }

        Titulo titulo = new Titulo();
        titulo.setEmpresaId(pedido.getEmpresaId());
        titulo.setTipo("R");
        titulo.setNumeroDocumento(pedido.getNumero());
        titulo.setDescricao("Venda - Pedido " + pedido.getNumero());
        Long pessoaId = clienteRepository.findByIdAndEmpresaIdAndDeletedAtIsNull(pedido.getClienteId(), pedido.getEmpresaId())
                .map(c -> c.getPessoa() == null ? null : c.getPessoa().getId())
                .orElseThrow(() -> new BusinessException("Cliente " + pedido.getClienteId() + " do pedido " + pedido.getNumero() + " nao tem pessoa vinculada"));
        titulo.setPessoaId(pessoaId);
        titulo.setValorOriginal(pedido.getValorTotal());
        titulo.setValorSaldo(pedido.getValorTotal());
        titulo.setDataEmissao(LocalDate.now());
        titulo.setDataVencimento(LocalDate.now().plusDays(30));
        titulo.setStatus("ABERTO");

        Titulo tituloSalvo = tituloRepository.save(titulo);
        pedido.setTituloId(tituloSalvo.getId());
        documentoFluxoService.ligar(empresaId, null,
                "PEDIDO_VENDA", pedido.getId(), pedido.getNumero(),
                "TITULO", tituloSalvo.getId(), tituloSalvo.getNumeroDocumento(),
                "FATURA");
        documentoFluxoService.ligar(empresaId, null,
                "PEDIDO_VENDA", pedido.getId(), pedido.getNumero(),
                "FATURA_VENDA", pedido.getId(), pedido.getNumero(),
                "FATURA");
        tituloService.gerarParcelas(pedido.getEmpresaId(), tituloSalvo.getId(), pedido.getCondicaoPagamentoId());

        if (pedido.getVendedorId() != null) {
            funcionarioRepository.findById(pedido.getVendedorId()).ifPresent(func -> {
                if ("VENDEDOR".equals(func.getTipoColaborador())) {
                    BigDecimal percentual = calcularPercentualComissao(pedido.getEmpresaId(), func.getId(), pedido.getValorTotal());
                    BigDecimal valorComissao = pedido.getValorTotal().multiply(percentual).divide(new BigDecimal("100"));
                    Comissao comissao = new Comissao();
                    comissao.setEmpresaId(pedido.getEmpresaId());
                    comissao.setFuncionarioId(func.getId());
                    comissao.setPedidoId(pedido.getId());
                    comissao.setValorVenda(pedido.getValorTotal());
                    comissao.setPercentual(percentual);
                    comissao.setValorComissao(valorComissao);
                    comissao.setStatus("PENDENTE");
                    comissaoRepository.save(comissao);
                }
            });
        }

        pedido.setStatus("FATURADO");
        pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public Map<String, Object> abrirPosVenda(Long id, Long empresaId, Long usuarioId, String motivo, String equipamento) {
        PedidoVenda pedido = pedidoBloqueado(id, empresaId);
        if ("FATURADO".equals(pedido.getStatus()) == false) throw new BusinessException("Somente pedido faturado gera pos-venda");
        if (motivo == null || motivo.isBlank()) throw new BusinessException("Motivo obrigatorio");
        var os = osService.abrir(empresaId, usuarioId, new OrdemServicoService.AberturaReq(pedido.getClienteId(), equipamento == null || equipamento.isBlank() ? "Pos-venda pedido " + pedido.getNumero() : equipamento, "Pos-venda do pedido " + pedido.getNumero() + ": " + motivo, null));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("osId", os.id());
        m.put("osNumero", os.numero());
        return m;
    }

    public void cancelar(Long id, Long empresaId) {
        PedidoVenda pedido = pedidoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido de venda nao encontrado"));
        if ("FATURADO".equals(pedido.getStatus())) {
            throw new BusinessException("Pedidos FATURADOS nao podem ser cancelados diretamente");
        }
        reservaEstoqueRepository
                .findByEmpresaIdAndPedidoVendaIdAndDeletedAtIsNull(empresaId, pedido.getId())
                .stream()
                .filter(r -> "RESERVADA".equals(r.getStatus()) || "SEPARACAO".equals(r.getStatus()))
                .forEach(r -> {
                    r.setStatus("LIBERADA");
                    reservaEstoqueRepository.save(r);
                });
        pedido.setStatus("CANCELADO");
        pedidoRepository.save(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> credito(Long empresaId, Long clienteId) {
        java.math.BigDecimal[] v = somarCredito(empresaId, clienteId);
        java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("limite", v[0]); m.put("emAberto", v[1]); m.put("disponivel", v[0].subtract(v[1]));
        return m;
    }

    private java.math.BigDecimal[] somarCredito(Long empresaId, Long clienteId) {
        var cli = clienteRepository.findById(clienteId).orElse(null);
        java.math.BigDecimal limite = java.math.BigDecimal.ZERO;
        java.math.BigDecimal emAberto = java.math.BigDecimal.ZERO;
        if (cli != null && cli.getPessoa() != null && cli.getPessoa().getId() != null) {
            limite = cli.getLimiteCredito() == null ? java.math.BigDecimal.ZERO : cli.getLimiteCredito();
            for (var x : tituloRepository.findByEmpresaIdAndPessoaIdAndDeletedAtIsNull(empresaId, cli.getPessoa().getId())) {
                if ("ABERTO".equals(x.getStatus()) == false && "PARCIAL".equals(x.getStatus()) == false) continue;
                emAberto = emAberto.add(x.getValorSaldo() == null ? java.math.BigDecimal.ZERO : x.getValorSaldo());
            }
        }
        return new java.math.BigDecimal[]{limite, emAberto};
    }

    private void validarCredito(PedidoVenda pedido) {
        if (pedido.getClienteId() == null) return;
        java.math.BigDecimal[] v = somarCredito(pedido.getEmpresaId(), pedido.getClienteId());
        java.math.BigDecimal pedidoValor = pedido.getValorTotal() == null ? java.math.BigDecimal.ZERO : pedido.getValorTotal();
        if (v[1].add(pedidoValor).compareTo(v[0]) > 0) throw new BusinessException("Limite de credito estourado: disponivel " + v[0].subtract(v[1]));
    }

    private PedidoVenda pedidoBloqueado(Long id, Long empresaId) {
        return pedidoRepository.findByIdForUpdateAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido de venda nao encontrado"));
    }

    private BigDecimal calcularPercentualComissao(Long empresaId, Long vendedorId, BigDecimal valorVenda) {
        LocalDate hoje = LocalDate.now();
        LocalDateTime inicioMes = hoje.withDayOfMonth(1).atStartOfDay();
        LocalDateTime fimMes = hoje.plusMonths(1).withDayOfMonth(1).atStartOfDay();
        BigDecimal acumulado = comissaoRepository.sumVendasPeriodo(empresaId, vendedorId, inicioMes, fimMes);
        acumulado = acumulado == null ? BigDecimal.ZERO : acumulado;
        BigDecimal acumuladoComVenda = acumulado.add(valorVenda);
        List<RegraComissao> regras = regraComissaoRepository.findByEmpresaIdAndVendedorIdAndAtivoTrueOrderByFaixaValorMinAsc(empresaId, vendedorId);
        if (regras.isEmpty()) {
            regras = regraComissaoRepository.findByEmpresaIdAndAtivoTrueOrderByFaixaValorMinAsc(empresaId);
        }
        return regras.stream()
                .filter(r -> (r.getVigenciaInicio() == null || !hoje.isBefore(r.getVigenciaInicio())) && (r.getVigenciaFim() == null || !hoje.isAfter(r.getVigenciaFim())))
                .filter(r -> r.getFaixaValorMin() == null || acumuladoComVenda.compareTo(r.getFaixaValorMin()) >= 0)
                .filter(r -> r.getFaixaValorMax() == null || acumuladoComVenda.compareTo(r.getFaixaValorMax()) <= 0)
                .filter(r -> r.getMetaValor() == null || acumuladoComVenda.compareTo(r.getMetaValor()) >= 0)
                .map(RegraComissao::getPercentual)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElseGet(() -> funcionarioRepository.findById(vendedorId).map(f -> f.getPercentualComissao() == null ? BigDecimal.ZERO : f.getPercentualComissao()).orElse(BigDecimal.ZERO));
    }

    private void reservarEstoqueDoPedido(PedidoVenda pedido) {
        Long depositoPadrao = depositoRepository.findFirstByEmpresaIdAndTipoAndAtivoTrue(pedido.getEmpresaId(), "PADRAO")
                .map(d -> d.getId())
                .orElseGet(() -> depositoRepository.findFirstByEmpresaIdAndAtivoTrueOrderByIdAsc(pedido.getEmpresaId()).map(d -> d.getId()).orElse(null));
        if (depositoPadrao == null) return;
        for (ItemPedidoVenda item : pedido.getItens()) {
            if (item.getProdutoId() == null) continue;
            SaldoEstoque saldo = saldoEstoqueRepository.findForUpdate(pedido.getEmpresaId(), depositoPadrao, item.getProdutoId()).orElse(null);
            if (saldo == null) continue;
            BigDecimal reservado = reservaEstoqueRepository.sumAtivas(pedido.getEmpresaId(), depositoPadrao, item.getProdutoId());
            reservado = reservado == null ? BigDecimal.ZERO : reservado;
            BigDecimal disponivel = saldo.getQuantidade().subtract(reservado);
            if (disponivel.compareTo(item.getQuantidade()) < 0) {
                throw new BusinessException("Estoque insuficiente para reservar produto " + item.getProdutoId() + " (disponivel " + disponivel + ")");
            }
            ReservaEstoque reserva = new ReservaEstoque();
            reserva.setEmpresaId(pedido.getEmpresaId());
            reserva.setDepositoId(depositoPadrao);
            reserva.setProdutoId(item.getProdutoId());
            reserva.setQuantidade(item.getQuantidade());
            reserva.setPedidoVendaId(pedido.getId());
            reserva.setStatus("RESERVADA");
            reserva.setDataReserva(LocalDateTime.now());
            reservaEstoqueRepository.save(reserva);
        }
    }

    private void baixarEstoque(Long empresaId, Long produtoId, BigDecimal quantidade, Long pedidoId) {
        Long depositoPadrao = depositoRepository.findFirstByEmpresaIdAndTipoAndAtivoTrue(empresaId, "PADRAO")
                .map(d -> d.getId())
                .orElseGet(() -> depositoRepository.findFirstByEmpresaIdAndAtivoTrueOrderByIdAsc(empresaId).map(d -> d.getId()).orElse(null));
        if (depositoPadrao == null) throw new BusinessException("Nenhum deposito ativo para baixa de estoque");
        SaldoEstoque saldo = saldoEstoqueRepository.findForUpdate(empresaId, depositoPadrao, produtoId)
                .orElseThrow(() -> new BusinessException("Sem saldo do produto " + produtoId));
        if (saldo.getQuantidade().compareTo(quantidade) < 0) {
            throw new BusinessException("Saldo insuficiente do produto " + produtoId);
        }
        saldo.setQuantidade(saldo.getQuantidade().subtract(quantidade));
        saldoEstoqueRepository.save(saldo);
        MovimentacaoEstoque mov = new MovimentacaoEstoque();
        mov.setEmpresaId(empresaId);
        mov.setDepositoId(depositoPadrao);
        mov.setProdutoId(produtoId);
        mov.setTipo("SAIDA");
        // A tabela bc_est_movimentacao nao tem documento_tipo/documento_id;
        // o vinculo com o documento de origem usa origem/origem_id, mesmo
        // padrao de PedidoCompraServiceImpl e InventarioEstoqueController.
        mov.setOrigem("PEDIDO_VENDA");
        mov.setOrigemId(pedidoId);
        mov.setQuantidade(quantidade.negate());
        mov.setSaldoApos(saldo.getQuantidade());
        mov.setObservacao("Baixa por venda");
        movimentacaoRepository.save(mov);
    }
}
