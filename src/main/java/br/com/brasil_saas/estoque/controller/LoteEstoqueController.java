package br.com.brasil_saas.estoque.controller;

import br.com.brasil_saas.estoque.model.LoteEstoque;
import br.com.brasil_saas.estoque.repository.DepositoRepository;
import br.com.brasil_saas.estoque.repository.LoteEstoqueRepository;
import br.com.brasil_saas.shared.exception.BusinessException;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Comparator;

@RestController
@RequestMapping("/api/estoque/lotes")
@RequiredArgsConstructor
public class LoteEstoqueController {
    private final LoteEstoqueRepository repository;
    private final DepositoRepository depositoRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('estoque:lote:leitura')")
    public List<LoteEstoque> listar(@AuthenticationPrincipal AuthenticatedUser user,
                                    @RequestParam(required = false) Long produtoId,
                                    @RequestParam(required = false) Long depositoId) {
        if (produtoId != null) {
            List<LoteEstoque> lotes = repository.findByEmpresaIdAndProdutoIdAndDeletedAtIsNullOrderByDataValidadeAsc(user.getEmpresaId(), produtoId);
            if (depositoId != null) lotes = lotes.stream().filter(x -> depositoId.equals(x.getDepositoId())).toList();
            return lotes;
        }
        if (depositoId != null)
            return repository.findByEmpresaIdAndDepositoIdAndStatusAndDeletedAtIsNullOrderByDataValidadeAsc(user.getEmpresaId(), depositoId, "ATIVO");
        return repository.findByEmpresaIdAndDeletedAtIsNullOrderByDataValidadeAsc(user.getEmpresaId());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('estoque:lote:escrita')")
    public LoteEstoque criar(@AuthenticationPrincipal AuthenticatedUser user,
                             @Valid @RequestBody Request request) {
        if (request.depositoId() != null)
            depositoRepository.findByIdAndEmpresaIdAndAtivoTrue(request.depositoId(), user.getEmpresaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Deposito nao encontrado"));

        repository.findByEmpresaIdAndProdutoIdAndCodigoAndDeletedAtIsNull(
                user.getEmpresaId(), request.produtoId(), request.codigo()).ifPresent(x -> {
                    throw new BusinessException("Ja existe lote com este codigo para o produto");
                });

        LoteEstoque lote = new LoteEstoque();
        lote.setEmpresaId(user.getEmpresaId());
        lote.setProdutoId(request.produtoId());
        lote.setCodigo(request.codigo().trim());
        lote.setDataFabricacao(request.dataFabricacao());
        lote.setDataValidade(request.dataValidade());
        lote.setQuantidade(request.quantidade() == null ? BigDecimal.ZERO : request.quantidade());
        lote.setDepositoId(request.depositoId());
        lote.setStatus(request.status() == null || request.status().isBlank() ? "ATIVO" : request.status());
        return repository.save(lote);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('estoque:lote:escrita')")
    public LoteEstoque atualizar(@AuthenticationPrincipal AuthenticatedUser user,
                                 @PathVariable Long id, @Valid @RequestBody Request request) {
        LoteEstoque lote = repository.findByIdAndEmpresaIdAndDeletedAtIsNull(id, user.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Lote nao encontrado"));
        if (request.depositoId() != null)
            depositoRepository.findByIdAndEmpresaIdAndAtivoTrue(request.depositoId(), user.getEmpresaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Deposito nao encontrado"));
        repository.findByEmpresaIdAndProdutoIdAndCodigoAndDeletedAtIsNull(user.getEmpresaId(), request.produtoId(), request.codigo())
                .filter(x -> !x.getId().equals(id))
                .ifPresent(x -> { throw new BusinessException("Ja existe lote com este codigo para o produto"); });

        lote.setProdutoId(request.produtoId());
        lote.setCodigo(request.codigo().trim());
        lote.setDataFabricacao(request.dataFabricacao());
        lote.setDataValidade(request.dataValidade());
        if (request.quantidade() != null) lote.setQuantidade(request.quantidade());
        lote.setDepositoId(request.depositoId());
        if (request.status() != null && !request.status().isBlank()) lote.setStatus(request.status());
        return repository.save(lote);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('estoque:lote:escrita')")
    public void excluir(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        LoteEstoque lote = repository.findByIdAndEmpresaIdAndDeletedAtIsNull(id, user.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Lote nao encontrado"));
        lote.setStatus("INATIVO");
        lote.setDeletedAt(LocalDateTime.now());
        repository.save(lote);
    }

    public record Request(@NotNull Long produtoId, @NotBlank String codigo, LocalDate dataFabricacao,
                          LocalDate dataValidade, BigDecimal quantidade, Long depositoId, String status) {}
}