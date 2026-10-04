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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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

    @Override
    @Transactional
    public PedidoVendaResponse criar(PedidoVendaRequest request) {
        PedidoVenda pedido = new PedidoVenda();
        pedido.setEmpresaId(request.empresaId());
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
        // A conta mora em CalculoDesconto. Aqui so' se monta a lista e se
        // guarda o resultado — a formula nao e' reescrita aqui, e' o que
        // mantem os tres pontos de chamada dando o mesmo resultado.
        List<CalculoDesconto.Item> paraCalculo = new ArrayList<>();

        if (request.itens() != null) {
            int num = 1;
            for (var itemReq : request.itens()) {
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

                // liquido() ja traz o desconto do item deduzido UMA vez.
                item.setValorTotal(itemReq.quantidade().multiply(itemReq.valorUnitario())
                        .subtract(item.getValorDesconto()));
                item.setCriadoEstoque(false);

                itens.add(item);
                paraCalculo.add(new CalculoDesconto.Item(itemReq.quantidade(), itemReq.valorUnitario(),
                        item.getValorDesconto()));
            }
        }

        CalculoDesconto calculo = CalculoDesconto.de(paraCalculo,
                request.percentualDesconto(), request.valorDesconto(), request.valorFrete());

        pedido.setItens(itens);
        pedido.setValorProdutos(calculo.getValorProdutos());
        pedido.setValorServicos(calculo.getValorServicos());
        // valorDesconto no pedido e' o TOTAL do desconto (itens + pedido), e
        // nao so o do pedido. A coluna e' uma so, e o PDV precisa que a soma
        // feche: antes ela guardava so o do pedido e o item ja vinha
        // descontado, entao o total do banco nao batia com o total da tela.
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

        pedido.setTipo("PEDIDO");
        pedido.setStatus("ABERTO");
        pedidoRepository.save(pedido);

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

                // A reserva vira consumida no mesmo evento transacional da baixa física.
                // Isso impede que a expedição tente consumir a mesma quantidade novamente.
                reservaEstoqueRepository
                        .findByEmpresaIdAndPedidoVendaIdAndDepositoIdAndProdutoIdAndDeletedAtIsNull(
                                pedido.getEmpresaId(),
                                pedido.getId(),
                                depositoRepository.findByEmpresaIdAndCodigoAndAtivoTrue(
                                                pedido.getEmpresaId(), "PADRAO")
                                        .orElseThrow(() -> new BusinessException(
                                                "Deposito PADRAO nao encontrado para a empresa "
                                                        + pedido.getEmpresaId()))
                                        .getId(),
                                item.getProdutoId())
                        .ifPresent(reserva -> {
                            reserva.setQuantidade(item.getQuantidade());
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
        // pessoaId, e NAO pedido.getClienteId(). Sao ids de tabelas diferentes:
        // pedido.cliente_id referencia bc_cad_cliente, e titulo.pessoa_id
        // referencia bc_cad_pessoa (medido: "viola restricao de chave
        // estrangeira bc_fin_titulo_pessoa_id_fkey / Chave (pessoa_id)=(9340)
        // nao esta presente na tabela bc_cad_pessoa"). Toda venda faturada
        // estourava 409 DATA_INTEGRITY e o estoque NAO baixava, porque a
        // transacao inteira rollback. O cliente guarda o pessoa_id.
        Long pessoaId = clienteRepository.findById(pedido.getClienteId())
                .map(c -> c.getPessoa() == null ? null : c.getPessoa().getId())
                .orElseThrow(() -> new BusinessException(
                        "Cliente " + pedido.getClienteId() + " do pedido "
                                + pedido.getNumero() + " nao tem pessoa vinculada"));
        titulo.setPessoaId(pessoaId);
        titulo.setValorOriginal(pedido.getValorTotal());
        titulo.setValorSaldo(pedido.getValorTotal());
        titulo.setDataEmissao(LocalDate.now());
        titulo.setDataVencimento(LocalDate.now().plusDays(30));
        titulo.setStatus("ABERTO");

        Titulo tituloSalvo = tituloRepository.save(titulo);
        pedido.setTituloId(tituloSalvo.getId());
        tituloService.gerarParcelas(pedido.getEmpresaId(), tituloSalvo.getId(), pedido.getCondicaoPagamentoId());

        if (pedido.getVendedorId() != null) {
            funcionarioRepository.findById(pedido.getVendedorId()).ifPresent(func -> {
                if ("VENDEDOR".equals(func.getTipoColaborador())) {
                    BigDecimal percentual = calcularPercentualComissao(
                            pedido.getEmpresaId(), func.getId(), pedido.getValorTotal());
                    BigDecimal valorComissao =
                            pedido.getValorTotal().multiply(percentual).divide(new BigDecimal("100"));

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

        pedido.setStatus("CANCELADO");
        pedidoRepository.save(pedido);
    }

    /**
     * Carrega o pedido com trava ( pessimista ) ja filtrando pela empresa.
     *
     * Filtrar depois de buscar seria tarde: o `orElseThrow` do findById puro
     * devolvia o pedido de outra empresa e so depois seDiscoveria o erro.
     */

    private void validarCredito(br.com.brasil_saas.vendas.model.PedidoVenda pedido) {
        if (pedido.getClienteId() == null) return;
        var cli = clienteRepository.findById(pedido.getClienteId()).orElse(null);
        if (cli == null || cli.getPessoa() == null || cli.getPessoa().getId() == null) return;
        java.math.BigDecimal limite = cli.getLimiteCredito() == null ? java.math.BigDecimal.ZERO : cli.getLimiteCredito();
        java.math.BigDecimal emAberto = java.math.BigDecimal.ZERO;
        for (var x : tituloRepository.findByEmpresaIdAndPessoaIdAndDeletedAtIsNull(pedido.getEmpresaId(), cli.getPessoa().getId())) {
            if ("ABERTO".equals(x.getStatus()) == false && "PARCIAL".equals(x.getStatus()) == false) continue;
            emAberto = emAberto.add(x.getValorSaldo() == null ? java.math.BigDecimal.ZERO : x.getValorSaldo());
        }
        java.math.BigDecimal pedido_valor = pedido.getValorTotal() == null ? java.math.BigDecimal.ZERO : pedido.getValorTotal();
        if (emAberto.add(pedido_valor).compareTo(limite) > 0) throw new BusinessException("Limite de crédito estourado: disponível " + limite.subtract(emAberto));
    }
    private PedidoVenda pedidoBloqueado(Long id, Long empresaId) {
        return pedidoRepository.findByIdForUpdateAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido de venda nao encontrado"));
    }

    private BigDecimal calcularPercentualComissao(Long empresaId, Long vendedorId, BigDecimal valorVenda) {
        LocalDate hoje = LocalDate.now();
        LocalDateTime inicioMes = hoje.withDayOfMonth(1).atStartOfDay();
        LocalDateTime fimMes = hoje.plusMonths(1).withDayOfMonth(1).atStartOfDay();

        BigDecimal acumulado = comissaoRepository.sumVendasPeriodo(
                empresaId, vendedorId, inicioMes, fimMes);
        acumulado = acumulado == null ? BigDecimal.ZERO : acumulado;
        BigDecimal acumuladoComVenda = acumulado.add(valorVenda);

        List<RegraComissao> regras = regraComissaoRepository
                .findByEmpresaIdAndVendedorIdAndAtivoTrueOrderByFaixaValorMinAsc(empresaId, vendedorId);

        if (regras.isEmpty()) {
            regras = regraComissaoRepository
                    .findByEmpresaIdAndAtivoTrueOrderByFaixaValorMinAsc(empresaId);
        }

        return regras.stream()
                .filter(r -> (r.getVigenciaInicio() == null || !hoje.isBefore(r.getVigenciaInicio()))
                        && (r.getVigenciaFim() == null || !hoje.isAfter(r.getVigenciaFim())))
                .filter(r -> r.getFaixaValorMin() == null
                        || acumuladoComVenda.compareTo(r.getFaixaValorMin()) >= 0)
                .filter(r -> r.getFaixaValorMax() == null
                        || acumuladoComVenda.compareTo(r.getFaixaValorMax()) <= 0)
                .filter(r -> r.getMetaValor() == null
                        || acumuladoComVenda.compareTo(r.getMetaValor()) >= 0)
                .map(RegraComissao::getPercentual)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    private void reservarEstoqueDoPedido(PedidoVenda pedido) {
        Long depositoId = depositoRepository.findByEmpresaIdAndCodigoAndAtivoTrue(pedido.getEmpresaId(), "PADRAO")
                .orElseThrow(() -> new BusinessException("Deposito PADRAO nao encontrado para a empresa " + pedido.getEmpresaId()))
                .getId();

        for (ItemPedidoVenda item : pedido.getItens()) {
            if (item.getProdutoId() == null) continue;

            SaldoEstoque saldo = saldoEstoqueRepository
                    .findForUpdate(pedido.getEmpresaId(), depositoId, item.getProdutoId())
                    .orElseThrow(() -> new BusinessException("Nao existe saldo para o produto " + item.getProdutoId()));

            BigDecimal reservado = reservaEstoqueRepository
                    .sumAtivas(pedido.getEmpresaId(), depositoId, item.getProdutoId());
            BigDecimal disponivel = saldo.getQuantidade().subtract(reservado == null ? BigDecimal.ZERO : reservado);

            ReservaEstoque reserva = reservaEstoqueRepository
                    .findByEmpresaIdAndPedidoVendaIdAndDepositoIdAndProdutoIdAndDeletedAtIsNull(
                            pedido.getEmpresaId(), pedido.getId(), depositoId, item.getProdutoId())
                    .orElseGet(ReservaEstoque::new);
            BigDecimal jaReservado = reserva.getId() == null ? BigDecimal.ZERO : reserva.getQuantidade();
            BigDecimal necessario = item.getQuantidade().subtract(jaReservado);

            if (necessario.signum() > 0 && disponivel.compareTo(necessario) < 0) {
                throw new BusinessException("Estoque disponivel insuficiente para reservar o produto "
                        + item.getProdutoId() + ". Disponivel: " + disponivel + ", solicitado: " + necessario);
            }

            reserva.setEmpresaId(pedido.getEmpresaId());
            reserva.setDepositoId(depositoId);
            reserva.setProdutoId(item.getProdutoId());
            reserva.setPedidoVendaId(pedido.getId());
            reserva.setQuantidade(item.getQuantidade());
            reserva.setStatus("RESERVADA");
            if (reserva.getDataReserva() == null) reserva.setDataReserva(java.time.LocalDateTime.now());
            reservaEstoqueRepository.save(reserva);
        }
    }

    private void baixarEstoque(Long empresaId, Long produtoId, BigDecimal quantidade, Long origemId) {
        SaldoEstoque saldo = saldoEstoqueRepository.findByEmpresaIdAndProdutoIdForUpdate(empresaId, produtoId)
                .orElseGet(() -> {
                    SaldoEstoque novo = new SaldoEstoque();
                    novo.setEmpresaId(empresaId);
                    novo.setDepositoId(depositoRepository.findByEmpresaIdAndCodigoAndAtivoTrue(empresaId, "PADRAO")
                            .orElseThrow(() -> new BusinessException("Deposito PADRAO nao encontrado para a empresa " + empresaId))
                            .getId());
                    novo.setProdutoId(produtoId);
                    novo.setQuantidade(BigDecimal.ZERO);
                    return novo;
                });

        BigDecimal saldoAtual = saldo.getQuantidade() != null ? saldo.getQuantidade() : BigDecimal.ZERO;
        if (saldoAtual.compareTo(quantidade) < 0) {
            throw new BusinessException(
                    "Estoque insuficiente para o produto " + produtoId
                            + ". Disponivel: " + saldoAtual
                            + ", solicitado: " + quantidade);
        }

        saldo.setQuantidade(saldoAtual.subtract(quantidade));
        saldoEstoqueRepository.save(saldo);

        MovimentacaoEstoque mov = new MovimentacaoEstoque();
        mov.setEmpresaId(empresaId);
        mov.setProdutoId(produtoId);
        mov.setTipo("SAIDA");
        mov.setOrigem("VENDA");
        mov.setOrigemId(origemId);
        mov.setQuantidade(quantidade.negate());
        mov.setSaldoApos(saldo.getQuantidade());
        mov.setObservacao("Baixa por venda");
        movimentacaoRepository.save(mov);
    }
}
