package br.com.brasil_saas.compras.controller;

import br.com.brasil_saas.compras.model.ConferenciaFaturaCompra;
import br.com.brasil_saas.compras.model.PedidoCompra;
import br.com.brasil_saas.compras.model.RecebimentoCompra;
import br.com.brasil_saas.compras.repository.ConferenciaFaturaCompraRepository;
import br.com.brasil_saas.compras.repository.PedidoCompraRepository;
import br.com.brasil_saas.compras.repository.RecebimentoCompraRepository;
import br.com.brasil_saas.financeiro.repository.TituloRepository;
import br.com.brasil_saas.fiscal.model.Nfe;
import br.com.brasil_saas.fiscal.repository.NfeRepository;
import br.com.brasil_saas.shared.exception.BusinessException;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/compras/conferencia-faturas")
@RequiredArgsConstructor
public class ConferenciaFaturaCompraController {
    private final ConferenciaFaturaCompraRepository repository;
    private final PedidoCompraRepository pedidoRepository;
    private final RecebimentoCompraRepository recebimentoRepository;
    private final TituloRepository tituloRepository;
    private final NfeRepository nfeRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('compras:pedido:leitura')")
    public List<ConferenciaFaturaCompra> listar(@AuthenticationPrincipal AuthenticatedUser user) {
        return repository.findByEmpresaIdOrderByCreatedAtDesc(user.getEmpresaId());
    }

    @PostMapping
    @Transactional
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
        return repository.save(c);
    }

    public record Request(@NotNull Long pedidoId, @NotNull BigDecimal valorFatura,
                          Long recebimentoId, Long tituloId, Long nfeId, BigDecimal tolerancia) {}
}
