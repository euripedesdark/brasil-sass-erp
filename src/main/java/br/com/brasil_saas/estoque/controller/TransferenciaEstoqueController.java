package br.com.brasil_saas.estoque.controller;

import br.com.brasil_saas.estoque.model.*;
import br.com.brasil_saas.estoque.repository.*;
import br.com.brasil_saas.shared.exception.BusinessException;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import br.com.brasil_saas.cadastro.repository.ProdutoRepository;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/estoque/transferencias")
@RequiredArgsConstructor
public class TransferenciaEstoqueController {
    private final TransferenciaEstoqueRepository transferenciaRepository;
    private final TransferenciaEstoqueItemRepository itemRepository;
    private final SaldoEstoqueRepository saldoRepository;
    private final DepositoRepository depositoRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;
    private final ReservaEstoqueRepository reservaRepository;
    private final EnderecoEstoqueRepository enderecoRepository;
    private final LoteEstoqueRepository loteRepository;
    private final ProdutoRepository produtoRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('estoque:transferencia:leitura')")
    public List<TransferenciaEstoque> listar(@AuthenticationPrincipal AuthenticatedUser user) {
        return transferenciaRepository.findByEmpresaIdAndDeletedAtIsNullOrderByDataTransferenciaDesc(user.getEmpresaId());
    }

    @GetMapping("/{id}/itens")
    @PreAuthorize("hasAuthority('estoque:transferencia:leitura')")
    public List<TransferenciaEstoqueItem> itens(@AuthenticationPrincipal AuthenticatedUser user,
                                                @PathVariable Long id) {
        transferenciaRepository.findByIdAndEmpresaIdAndDeletedAtIsNull(id, user.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Transferência não encontrada"));
        return itemRepository.findByTransferenciaId(id);
    }

    @PostMapping
    @Transactional
    @PreAuthorize("hasAuthority('estoque:transferencia:escrita')")
    public ResponseEntity<TransferenciaEstoque> transferir(@AuthenticationPrincipal AuthenticatedUser user,
                                                            @Valid @RequestBody TransferenciaRequest request) {
        if (request.depositoOrigemId().equals(request.depositoDestinoId()))
            throw new BusinessException("O depósito de origem deve ser diferente do destino");

        depositoRepository.findByIdAndEmpresaIdAndAtivoTrue(request.depositoOrigemId(), user.getEmpresaId())
                .orElseThrow(() -> new BusinessException("Depósito de origem inválido"));
        depositoRepository.findByIdAndEmpresaIdAndAtivoTrue(request.depositoDestinoId(), user.getEmpresaId())
                .orElseThrow(() -> new BusinessException("Depósito de destino inválido"));

        TransferenciaEstoque transferencia = new TransferenciaEstoque();
        transferencia.setEmpresaId(user.getEmpresaId());
        transferencia.setDepositoOrigemId(request.depositoOrigemId());
        transferencia.setDepositoDestinoId(request.depositoDestinoId());
        transferencia.setObservacoes(request.observacoes());
        transferencia = transferenciaRepository.save(transferencia);

        for (ItemRequest item : request.itens()) {
            if (item.quantidade().signum() <= 0) throw new BusinessException("Quantidade deve ser maior que zero");

            produtoRepository.findByIdAndEmpresaIdAndDeletedAtIsNull(item.produtoId(), user.getEmpresaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + item.produtoId()));

            SaldoEstoque origem = saldoRepository.findForUpdate(user.getEmpresaId(), request.depositoOrigemId(), item.produtoId())
                    .orElseThrow(() -> new BusinessException("Não existe saldo do produto " + item.produtoId() + " no depósito de origem"));

            BigDecimal reservado = reservaRepository.sumAtivas(
                    user.getEmpresaId(), request.depositoOrigemId(), item.produtoId());
            BigDecimal disponivel = origem.getQuantidade().subtract(
                    reservado == null ? BigDecimal.ZERO : reservado);

            if (disponivel.compareTo(item.quantidade()) < 0)
                throw new BusinessException("Saldo disponível insuficiente para o produto " + item.produtoId()
                        + ". Disponível: " + disponivel);

            SaldoEstoque destino = saldoRepository.findForUpdate(user.getEmpresaId(), request.depositoDestinoId(), item.produtoId())
                    .orElseGet(() -> {
                        SaldoEstoque s = new SaldoEstoque();
                        s.setEmpresaId(user.getEmpresaId());
                        s.setDepositoId(request.depositoDestinoId());
                        s.setProdutoId(item.produtoId());
                        s.setQuantidade(BigDecimal.ZERO);
                        return s;
                    });

            origem.setQuantidade(origem.getQuantidade().subtract(item.quantidade()));
            origem.setAtualizadoEm(LocalDateTime.now());
            destino.setQuantidade(destino.getQuantidade().add(item.quantidade()));
            destino.setAtualizadoEm(LocalDateTime.now());
            saldoRepository.save(origem);
            saldoRepository.save(destino);

            MovimentacaoEstoque saida = movimento(user.getEmpresaId(), item.produtoId(), request.depositoOrigemId(),
                    item.quantidade().negate(), origem.getQuantidade(), transferencia.getId(), "TRANSFERENCIA_SAIDA");
            MovimentacaoEstoque entrada = movimento(user.getEmpresaId(), item.produtoId(), request.depositoDestinoId(),
                    item.quantidade(), destino.getQuantidade(), transferencia.getId(), "TRANSFERENCIA_ENTRADA");
            movimentacaoRepository.save(saida);
            movimentacaoRepository.save(entrada);

            TransferenciaEstoqueItem ti = new TransferenciaEstoqueItem();
            ti.setTransferenciaId(transferencia.getId());
            ti.setProdutoId(item.produtoId());
            ti.setQuantidade(item.quantidade());
            itemRepository.save(ti);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(transferencia);
    }

    @PostMapping("/interna")
    @Transactional
    @PreAuthorize("hasAuthority('estoque:wms:transferencia')")
    public ResponseEntity<TransferenciaEstoque> transferirInternamente(@AuthenticationPrincipal AuthenticatedUser user,
                                                                        @Valid @RequestBody TransferenciaInternaRequest request) {
        depositoRepository.findByIdAndEmpresaIdAndAtivoTrue(request.depositoId(), user.getEmpresaId())
                .orElseThrow(() -> new BusinessException("Depósito inválido"));

        if (request.enderecoOrigemId().equals(request.enderecoDestinoId()))
            throw new BusinessException("O endereço de origem deve ser diferente do destino");

        EnderecoEstoque origemEndereco = enderecoRepository.findByIdAndEmpresaIdAndAtivoTrue(request.enderecoOrigemId(), user.getEmpresaId())
                .orElseThrow(() -> new BusinessException("Endereço de origem não encontrado"));
        EnderecoEstoque destinoEndereco = enderecoRepository.findByIdAndEmpresaIdAndAtivoTrue(request.enderecoDestinoId(), user.getEmpresaId())
                .orElseThrow(() -> new BusinessException("Endereço de destino não encontrado"));
        if (!request.depositoId().equals(origemEndereco.getDepositoId()) || !request.depositoId().equals(destinoEndereco.getDepositoId()))
            throw new BusinessException("Os endereços precisam pertencer ao depósito informado");

        if (request.quantidade().signum() <= 0) throw new BusinessException("Quantidade deve ser maior que zero");

        if (request.loteId() != null) {
            LoteEstoque lote = loteRepository.findByIdAndEmpresaIdAndDeletedAtIsNull(request.loteId(), user.getEmpresaId())
                    .orElseThrow(() -> new BusinessException("Lote não encontrado"));
            if (!request.produtoId().equals(lote.getProdutoId()) || !request.depositoId().equals(lote.getDepositoId()))
                throw new BusinessException("Lote não pertence ao produto/deposito informado");
            if (!"ATIVO".equals(lote.getStatus()))
                throw new BusinessException("Lote " + lote.getCodigo() + " nao esta ATIVO (status=" + lote.getStatus() + ")");
        }

        BigDecimal saldoEndereco = movimentacaoRepository.saldosPorEndereco(
                user.getEmpresaId(), request.depositoId(), request.produtoId(), request.loteId()).stream()
                .filter(x -> request.enderecoOrigemId().equals(x.getEnderecoId()))
                .map(MovimentacaoEstoqueRepository.EnderecoSaldo::getQuantidade)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal reservado = request.loteId() == null
                ? reservaRepository.sumAtivasPorEndereco(user.getEmpresaId(), request.depositoId(), request.produtoId(), request.enderecoOrigemId())
                : reservaRepository.sumAtivasPorEnderecoELote(user.getEmpresaId(), request.depositoId(), request.produtoId(), request.enderecoOrigemId(), request.loteId());
        BigDecimal disponivel = saldoEndereco.subtract(reservado == null ? BigDecimal.ZERO : reservado);
        if (disponivel.compareTo(request.quantidade()) < 0)
            throw new BusinessException("Saldo disponível insuficiente no endereço de origem. Disponível: " + disponivel);

        TransferenciaEstoque transferencia = new TransferenciaEstoque();
        transferencia.setEmpresaId(user.getEmpresaId());
        transferencia.setDepositoOrigemId(request.depositoId());
        transferencia.setDepositoDestinoId(request.depositoId());
        transferencia.setStatus("CONCLUIDA");
        transferencia.setObservacoes(request.observacoes());
        transferencia = transferenciaRepository.save(transferencia);

        MovimentacaoEstoque saida = movimento(user.getEmpresaId(), request.produtoId(), request.depositoId(),
                request.quantidade().negate(), saldoEndereco.subtract(request.quantidade()), transferencia.getId(), "TRANSFERENCIA_INTERNA_SAIDA");
        saida.setEnderecoId(request.enderecoOrigemId());
        saida.setLoteId(request.loteId());
        saida.setObservacao("Transferência WMS entre endereços");
        MovimentacaoEstoque entrada = movimento(user.getEmpresaId(), request.produtoId(), request.depositoId(),
                request.quantidade(), request.quantidade(), transferencia.getId(), "TRANSFERENCIA_INTERNA_ENTRADA");
        entrada.setEnderecoId(request.enderecoDestinoId());
        entrada.setLoteId(request.loteId());
        entrada.setObservacao("Transferência WMS entre endereços");
        movimentacaoRepository.save(saida);
        movimentacaoRepository.save(entrada);

        TransferenciaEstoqueItem item = new TransferenciaEstoqueItem();
        item.setTransferenciaId(transferencia.getId());
        item.setProdutoId(request.produtoId());
        item.setQuantidade(request.quantidade());
        item.setLoteId(request.loteId());
        item.setEnderecoOrigemId(request.enderecoOrigemId());
        item.setEnderecoDestinoId(request.enderecoDestinoId());
        itemRepository.save(item);

        return ResponseEntity.status(HttpStatus.CREATED).body(transferencia);
    }

    private MovimentacaoEstoque movimento(Long empresaId, Long produtoId, Long depositoId,
                                          BigDecimal quantidade, BigDecimal saldoApos, Long transferenciaId, String tipo) {
        MovimentacaoEstoque m = new MovimentacaoEstoque();
        m.setEmpresaId(empresaId);
        m.setProdutoId(produtoId);
        m.setDepositoId(depositoId);
        m.setTipo(tipo);
        m.setOrigem("TRANSFERENCIA");
        m.setOrigemId(transferenciaId);
        m.setQuantidade(quantidade);
        m.setSaldoApos(saldoApos);
        m.setDataMovimento(LocalDateTime.now());
        m.setObservacao("Transferência entre depósitos");
        return m;
    }

    public record TransferenciaInternaRequest(@NotNull Long depositoId, @NotNull Long produtoId, Long loteId,
                                             @NotNull Long enderecoOrigemId, @NotNull Long enderecoDestinoId,
                                             @NotNull @DecimalMin("0.001") BigDecimal quantidade, String observacoes) {}

    public record TransferenciaRequest(@NotNull Long depositoOrigemId, @NotNull Long depositoDestinoId,
                                       @NotEmpty List<ItemRequest> itens, String observacoes) {}
    public record ItemRequest(@NotNull Long produtoId, @NotNull @DecimalMin("0.001") BigDecimal quantidade) {}
}
