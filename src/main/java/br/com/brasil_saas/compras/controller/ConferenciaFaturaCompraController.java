package br.com.brasil_saas.compras.controller;

import br.com.brasil_saas.compras.model.ConferenciaFaturaCompra;
import br.com.brasil_saas.compras.model.ConferenciaFaturaCompraItem;
import br.com.brasil_saas.compras.model.RecebimentoCompraItem;
import br.com.brasil_saas.compras.model.ItemPedidoCompra;
import br.com.brasil_saas.compras.model.PedidoCompra;
import br.com.brasil_saas.compras.model.RecebimentoCompra;
import br.com.brasil_saas.compras.repository.ConferenciaFaturaCompraRepository;
import br.com.brasil_saas.compras.repository.ConferenciaFaturaCompraItemRepository;
import br.com.brasil_saas.compras.repository.RecebimentoCompraItemRepository;
import br.com.brasil_saas.compras.repository.PedidoCompraRepository;
import br.com.brasil_saas.compras.repository.RecebimentoCompraRepository;
import br.com.brasil_saas.financeiro.repository.TituloRepository;
import br.com.brasil_saas.fiscal.model.Nfe;
import br.com.brasil_saas.fiscal.model.NfeItem;
import br.com.brasil_saas.fiscal.repository.NfeRepository;
import br.com.brasil_saas.fiscal.repository.NfeItemRepository;
import br.com.brasil_saas.shared.exception.BusinessException;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/compras/conferencia-faturas")
@RequiredArgsConstructor
public class ConferenciaFaturaCompraController {
    private final ConferenciaFaturaCompraRepository repository;
    private final PedidoCompraRepository pedidoRepository;
    private final RecebimentoCompraRepository recebimentoRepository;
    private final TituloRepository tituloRepository;
    private final NfeRepository nfeRepository;
    private final NfeItemRepository nfeItemRepository;
    private final RecebimentoCompraItemRepository recebimentoItemRepository;
    private final ConferenciaFaturaCompraItemRepository itemRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('compras:pedido:leitura')")
    public List<ConferenciaFaturaCompra> listar(@AuthenticationPrincipal AuthenticatedUser user) {
        return service.listar(user.getEmpresaId());
    }

    @GetMapping("/{id}/itens")
    @PreAuthorize("hasAuthority('compras:pedido:leitura')")
    public List<ConferenciaFaturaCompraItem> itens(@AuthenticationPrincipal AuthenticatedUser user,
                                                   @PathVariable Long id) {
        repository.findByIdAndEmpresaIdAndDeletedAtIsNull(id, user.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Conferencia 3-way nao encontrada"));
        return itemRepository.findByEmpresaIdAndConferenciaIdAndDeletedAtIsNullOrderByIdAsc(
                user.getEmpresaId(), id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('compras:pedido:escrita')")
    public ConferenciaFaturaCompra conferir(@AuthenticationPrincipal AuthenticatedUser user,
                                            @Valid @RequestBody Request request) {
        PedidoCompra pedido = pedidoRepository.findByIdForUpdate(request.pedidoId())
                .filter(p -> p.getEmpresaId().equals(user.getEmpresaId()))
                .orElseThrow(() -> new ResourceNotFoundException("Pedido de compra nao encontrado"));

        if ("CANCELADO".equals(pedido.getStatus())) {
            throw new BusinessException("Pedido de compra cancelado nao pode entrar em conferencia");
        }

        BigDecimal valorPedido = pedido.getValorTotal() == null ? BigDecimal.ZERO : pedido.getValorTotal();

        if (request.recebimentoId() == null) throw new BusinessException("O 3-way match exige um recebimento");
        if (request.nfeId() == null) throw new BusinessException("O 3-way match exige o documento fiscal de entrada");

        RecebimentoCompra recebimento = null;
        if (request.recebimentoId() != null) {
            recebimento = recebimentoRepository.findByIdAndEmpresaIdAndDeletedAtIsNull(
                            request.recebimentoId(), user.getEmpresaId())
                    .filter(r -> pedido.getId().equals(r.getPedidoId()))
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Recebimento nao encontrado para o pedido informado"));
        } else {
            List<RecebimentoCompra> recebimentos =
                    recebimentoRepository.findByEmpresaIdAndPedidoIdAndDeletedAtIsNullOrderByDataRecebimentoDesc(
                            user.getEmpresaId(), pedido.getId());
            if (!recebimentos.isEmpty()) recebimento = recebimentos.get(0);
        }

        BigDecimal valorRecebido = recebimento != null
                ? (recebimento.getValorTotal() == null ? BigDecimal.ZERO : recebimento.getValorTotal())
                : pedido.getItens().stream()
                    .map(i -> {
                        BigDecimal qtd = i.getQuantidadeRecebida() == null ? BigDecimal.ZERO : i.getQuantidadeRecebida();
                        return qtd.multiply(i.getValorUnitario() == null ? BigDecimal.ZERO : i.getValorUnitario());
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

        Nfe nfe = null;
        if (request.nfeId() != null) {
            nfe = nfeRepository.findByIdAndEmpresaIdAndDeletedAtIsNull(request.nfeId(), user.getEmpresaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Documento fiscal nao encontrado"));
            if (!"E".equalsIgnoreCase(nfe.getTipoOperacao())) throw new BusinessException("Documento fiscal deve ser de entrada");
            if (nfe.getPedidoCompraId() != null && !pedido.getId().equals(nfe.getPedidoCompraId())) throw new BusinessException("Documento fiscal vinculado a outro pedido");
            if (nfe.getStatus() != null && !("AUTORIZADA".equalsIgnoreCase(nfe.getStatus()) || "AUTORIZADO".equalsIgnoreCase(nfe.getStatus()))) throw new BusinessException("Documento fiscal ainda nao esta autorizado");
        }

        if (request.tituloId() != null) {
            tituloRepository.findById(request.tituloId())
                    .filter(t -> t.getEmpresaId().equals(user.getEmpresaId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Titulo informado nao encontrado"));
        }

        BigDecimal valorFatura = request.valorFatura();
        BigDecimal tolerancia = request.tolerancia() == null ? BigDecimal.ZERO : request.tolerancia();
        BigDecimal limite = tolerancia.abs();

        boolean pedidoOk = valorFatura.subtract(valorPedido).abs().compareTo(limite) <= 0;
        boolean recebimentoOk = valorFatura.subtract(valorRecebido).abs().compareTo(limite) <= 0;

        ConferenciaFaturaCompra c = new ConferenciaFaturaCompra();
        c.setEmpresaId(user.getEmpresaId());
        c.setPedidoId(pedido.getId());
        c.setRecebimentoId(recebimento != null ? recebimento.getId() : null);
        c.setTituloId(request.tituloId());
        c.setNfeId(nfe != null ? nfe.getId() : null);
        c.setValorPedido(valorPedido);
        c.setValorRecebido(valorRecebido);
        c.setValorFatura(valorFatura);
        c.setTolerancia(tolerancia);
        c.setStatus(pedidoOk && recebimentoOk ? "APROVADA" : "DIVERGENTE");
        c.setDivergencia(pedidoOk && recebimentoOk ? null :
                "Divergencia 3-way: pedido=" + valorPedido +
                ", recebido=" + valorRecebido + ", fatura=" + valorFatura +
                ", tolerancia=" + tolerancia);
        ConferenciaFaturaCompra salva = repository.save(c);
        persistirItens(user.getEmpresaId(), salva, pedido, recebimento, nfe, tolerancia);
        return salva;
    }

    private void persistirItens(Long empresaId, ConferenciaFaturaCompra conferencia,
                                PedidoCompra pedido, RecebimentoCompra recebimento,
                                Nfe nfe, BigDecimal tolerancia) {
        List<RecebimentoCompraItem> recebidos = recebimento == null ? List.of()
                : recebimentoItemRepository.findByEmpresaIdAndRecebimentoIdAndDeletedAtIsNullOrderByIdAsc(
                        empresaId, recebimento.getId());
        List<NfeItem> faturados = nfe == null ? List.of()
                : nfeItemRepository.findByNfeIdOrderByNumeroItem(nfe.getId());

        Map<Long, RecebimentoCompraItem> recebidoPorProduto = recebidos.stream()
                .filter(i -> i.getProdutoId() != null)
                .collect(Collectors.toMap(RecebimentoCompraItem::getProdutoId, Function.identity(), (a,b) -> a));
        Map<Long, NfeItem> faturadoPorProduto = faturados.stream()
                .filter(i -> i.getProdutoId() != null)
                .collect(Collectors.toMap(NfeItem::getProdutoId, Function.identity(), (a,b) -> a));

        for (ItemPedidoCompra pedidoItem : pedido.getItens()) {
            Long produtoId = pedidoItem.getProdutoId();
            RecebimentoCompraItem recebido = produtoId == null ? null : recebidoPorProduto.get(produtoId);
            NfeItem faturado = produtoId == null ? null : faturadoPorProduto.get(produtoId);

            BigDecimal qtdPedido = nz(pedidoItem.getQuantidade());
            BigDecimal qtdRecebida = recebido == null ? nz(pedidoItem.getQuantidadeRecebida()) : nz(recebido.getQuantidadeRecebida());
            BigDecimal qtdFaturada = faturado == null ? BigDecimal.ZERO : nz(faturado.getQuantidade());
            BigDecimal precoPedido = nz(pedidoItem.getValorUnitario());
            BigDecimal precoRecebido = recebido == null ? precoPedido : nz(recebido.getValorUnitario());
            BigDecimal precoFaturado = faturado == null ? BigDecimal.ZERO : nz(faturado.getValorUnitario());

            boolean quantidadeOk = qtdRecebida.compareTo(qtdPedido) <= 0 && qtdFaturada.compareTo(qtdRecebida) == 0;
            boolean precoOk = faturado != null
                    && precoFaturado.subtract(precoPedido).abs().compareTo(tolerancia.abs()) <= 0
                    && precoFaturado.subtract(precoRecebido).abs().compareTo(tolerancia.abs()) <= 0;

            ConferenciaFaturaCompraItem item = new ConferenciaFaturaCompraItem();
            item.setEmpresaId(empresaId);
            item.setConferenciaId(conferencia.getId());
            item.setPedidoItemId(pedidoItem.getId());
            item.setRecebimentoItemId(recebido == null ? null : recebido.getId());
            item.setNfeItemId(faturado == null ? null : faturado.getId());
            item.setProdutoId(produtoId);
            item.setQuantidadePedida(qtdPedido);
            item.setQuantidadeRecebida(qtdRecebida);
            item.setQuantidadeFaturada(qtdFaturada);
            item.setValorUnitarioPedido(precoPedido);
            item.setValorUnitarioRecebido(precoRecebido);
            item.setValorUnitarioFaturado(precoFaturado);
            item.setTolerancia(tolerancia);
            item.setStatus(quantidadeOk && precoOk ? "APROVADA" : "DIVERGENTE");
            if (!(quantidadeOk && precoOk)) {
                item.setDivergencia("Item=" + pedidoItem.getNumeroItem()
                        + ", produto=" + produtoId
                        + ", qtd pedido=" + qtdPedido
                        + ", recebida=" + qtdRecebida
                        + ", faturada=" + qtdFaturada
                        + ", preco pedido=" + precoPedido
                        + ", recebido=" + precoRecebido
                        + ", faturado=" + precoFaturado);
            }
            itemRepository.save(item);
        }
    }

    private static BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /** Itens conferidos: pedido x recebimento x NF-e, com a divergencia de cada linha. */
    @GetMapping("/{id}/itens")
    @PreAuthorize("hasAuthority('compras:pedido:leitura')")
    public List<ConferenciaFaturaCompraItem> listarItens(@AuthenticationPrincipal AuthenticatedUser user,
                                                         @PathVariable Long id) {
        return service.listarItens(user.getEmpresaId(), id);
    }
}
