package br.com.brasil_saas.estoque.controller;

import br.com.brasil_saas.estoque.model.ReservaEstoque;
import br.com.brasil_saas.estoque.model.SaldoEstoque;
import br.com.brasil_saas.estoque.model.LoteEstoque;
import br.com.brasil_saas.estoque.model.EnderecoEstoque;
import br.com.brasil_saas.estoque.repository.DepositoRepository;
import br.com.brasil_saas.estoque.repository.LoteEstoqueRepository;
import br.com.brasil_saas.estoque.repository.ReservaEstoqueRepository;
import br.com.brasil_saas.estoque.repository.SaldoEstoqueRepository;
import br.com.brasil_saas.estoque.repository.EnderecoEstoqueRepository;
import br.com.brasil_saas.estoque.repository.MovimentacaoEstoqueRepository;
import br.com.brasil_saas.shared.exception.BusinessException;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/estoque/reservas")
@RequiredArgsConstructor
public class ReservaEstoqueController {
    private final ReservaEstoqueRepository repository;
    private final SaldoEstoqueRepository saldoRepository;
    private final DepositoRepository depositoRepository;
    private final LoteEstoqueRepository loteRepository;
    private final EnderecoEstoqueRepository enderecoRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('estoque:reserva:leitura')")
    public List<ReservaEstoque> listar(@AuthenticationPrincipal AuthenticatedUser user,
                                       @RequestParam(required = false) Long pedidoVendaId) {
        if (pedidoVendaId != null)
            return repository.findByEmpresaIdAndPedidoVendaIdAndDeletedAtIsNull(user.getEmpresaId(), pedidoVendaId);
        return repository.findByEmpresaIdAndDeletedAtIsNullOrderByDataReservaDesc(user.getEmpresaId());
    }

    @GetMapping("/picking-sugestoes")
    @PreAuthorize("hasAuthority('estoque:picking:leitura')")
    public List<Map<String,Object>> pickingSugestoes(@AuthenticationPrincipal AuthenticatedUser user,
                                                      @RequestParam Long depositoId,
                                                      @RequestParam Long produtoId,
                                                      @RequestParam(required = false) Long loteId) {
        depositoRepository.findByIdAndEmpresaIdAndAtivoTrue(depositoId, user.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Deposito nao encontrado"));
        return movimentacaoRepository.saldosPorEndereco(user.getEmpresaId(), depositoId, produtoId, loteId)
                .stream().map(x -> {
                    Map<String,Object> item = new java.util.LinkedHashMap<>();
                    item.put("enderecoId", x.getEnderecoId());
                    item.put("loteId", x.getLoteId());
                    item.put("quantidade", x.getQuantidade());
                    return item;
                }).toList();
    }

    @PostMapping
    @Transactional
    @PreAuthorize("hasAuthority('estoque:reserva:escrita')")
    public ReservaEstoque reservar(@AuthenticationPrincipal AuthenticatedUser user,
                                   @Valid @RequestBody Request request) {
        depositoRepository.findByIdAndEmpresaIdAndAtivoTrue(request.depositoId(), user.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Deposito nao encontrado"));

        SaldoEstoque saldo = saldoRepository.findForUpdate(user.getEmpresaId(), request.depositoId(), request.produtoId())
                .orElseThrow(() -> new BusinessException("Nao existe saldo do produto no deposito"));

        BigDecimal reservado = repository.sumAtivas(user.getEmpresaId(), request.depositoId(), request.produtoId());
        BigDecimal disponivel = saldo.getQuantidade().subtract(reservado == null ? BigDecimal.ZERO : reservado);

        EnderecoEstoque endereco = null;
        if (request.enderecoId() != null) {
            endereco = enderecoRepository.findByIdAndEmpresaIdAndAtivoTrue(request.enderecoId(), user.getEmpresaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Endereco nao encontrado"));
            if (!request.depositoId().equals(endereco.getDepositoId()))
                throw new BusinessException("Endereco nao pertence ao deposito informado");

            BigDecimal saldoEndereco = movimentacaoRepository.saldosPorEndereco(
                    user.getEmpresaId(), request.depositoId(), request.produtoId(), request.loteId()).stream()
                    .filter(x -> request.enderecoId().equals(x.getEnderecoId()))
                    .map(MovimentacaoEstoqueRepository.EnderecoSaldo::getQuantidade)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal reservadoEndereco = repository.sumAtivasPorEndereco(
                    user.getEmpresaId(), request.depositoId(), request.produtoId(), request.enderecoId());
            BigDecimal disponivelEndereco = saldoEndereco.subtract(reservadoEndereco == null ? BigDecimal.ZERO : reservadoEndereco);
            if (disponivelEndereco.compareTo(request.quantidade()) < 0)
                throw new BusinessException("Quantidade insuficiente no endereco. Disponivel: " + disponivelEndereco);
        }

        if (request.loteId() != null) {
            LoteEstoque lote = loteRepository.findByIdAndEmpresaIdAndDeletedAtIsNull(request.loteId(), user.getEmpresaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lote nao encontrado"));
            if (!request.produtoId().equals(lote.getProdutoId()) || !request.depositoId().equals(lote.getDepositoId()))
                throw new BusinessException("Lote nao pertence ao produto/deposito informado");
            BigDecimal reservadoLote = repository.sumAtivasPorLote(user.getEmpresaId(), request.depositoId(),
                    request.produtoId(), request.loteId());
            BigDecimal disponivelLote = lote.getQuantidade().subtract(reservadoLote == null ? BigDecimal.ZERO : reservadoLote);
            if (disponivelLote.compareTo(request.quantidade()) < 0)
                throw new BusinessException("Quantidade insuficiente no lote " + lote.getCodigo() + ". Disponivel: " + disponivelLote);
        }
        if (disponivel.compareTo(request.quantidade()) < 0)
            throw new BusinessException("Estoque disponivel insuficiente. Disponivel: " + disponivel);

        if (request.pedidoVendaId() != null && repository.findByEmpresaIdAndPedidoVendaIdAndDeletedAtIsNull(user.getEmpresaId(), request.pedidoVendaId()).stream().anyMatch(r -> !List.of("LIBERADA","CANCELADA","CONSUMIDA").contains(r.getStatus()) && request.produtoId().equals(r.getProdutoId()) && request.depositoId().equals(r.getDepositoId()) && request.enderecoId() != null && request.enderecoId().equals(r.getEnderecoId()))) throw new BusinessException("Ja existe reserva ativa para este pedido/produto/endereco");

        ReservaEstoque reserva;
        if (request.loteId() == null) {
            reserva = repository.findByEmpresaIdAndPedidoVendaIdAndDepositoIdAndProdutoIdAndDeletedAtIsNull(
                    user.getEmpresaId(), request.pedidoVendaId(), request.depositoId(), request.produtoId())
                    .orElseGet(ReservaEstoque::new);
        } else {
            reserva = repository.findByEmpresaIdAndPedidoVendaIdAndDepositoIdAndProdutoIdAndLoteIdAndDeletedAtIsNull(
                    user.getEmpresaId(), request.pedidoVendaId(), request.depositoId(), request.produtoId(), request.loteId())
                    .orElseGet(ReservaEstoque::new);
        }

        reserva.setEmpresaId(user.getEmpresaId());
        reserva.setDepositoId(request.depositoId());
        reserva.setProdutoId(request.produtoId());
        reserva.setPedidoVendaId(request.pedidoVendaId());
        reserva.setLoteId(request.loteId());
        reserva.setEnderecoId(request.enderecoId());
        if (request.dataExpiracao() != null && request.dataExpiracao().isBefore(LocalDateTime.now())) throw new BusinessException("Data de expiracao deve ser futura");
        reserva.setQuantidade(request.quantidade());
        reserva.setStatus("RESERVADA");
        if (reserva.getDataReserva() == null) reserva.setDataReserva(LocalDateTime.now());
        reserva.setDataExpiracao(request.dataExpiracao());
        reserva.setDeletedAt(null);
        return repository.save(reserva);
    }

    @PostMapping("/{id}/liberar")
    @Transactional
    @PreAuthorize("hasAuthority('estoque:reserva:escrita')")
    public ReservaEstoque liberar(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        ReservaEstoque reserva = repository.findWithLockByIdAndEmpresaIdAndDeletedAtIsNull(id, user.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Reserva nao encontrada"));
        if ("LIBERADA".equals(reserva.getStatus()) || "CANCELADA".equals(reserva.getStatus()))
            throw new BusinessException("Reserva ja esta encerrada");
        reserva.setStatus("LIBERADA");
        return repository.save(reserva);
    }

    @PostMapping("/{id}/separar")
    @Transactional
    @PreAuthorize("hasAuthority('estoque:reserva:escrita')")
    public ReservaEstoque separar(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        ReservaEstoque reserva = repository.findWithLockByIdAndEmpresaIdAndDeletedAtIsNull(id, user.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Reserva nao encontrada"));
        if (!"RESERVADA".equals(reserva.getStatus()))
            throw new BusinessException("Somente reservas RESERVADAS podem ir para separacao");
        reserva.setStatus("SEPARACAO");
        return repository.save(reserva);
    }

    public record Request(@NotNull Long depositoId, @NotNull Long produtoId, Long pedidoVendaId, Long loteId,
                          Long enderecoId, @NotNull @DecimalMin("0.001") BigDecimal quantidade, LocalDateTime dataExpiracao) {}
}