package br.com.brasil_saas.estoque.controller;

import br.com.brasil_saas.estoque.model.LoteEstoque;
import br.com.brasil_saas.estoque.repository.DepositoRepository;
import br.com.brasil_saas.estoque.repository.LoteEstoqueRepository;
import br.com.brasil_saas.shared.exception.BusinessException;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import jakarta.transaction.Transactional;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Cadastro e ciclo de vida operacional do lote: ATIVO, QUARENTENA, BLOQUEADO, VENCIDO, INATIVO.
 * Lotes fora de ATIVO nao podem ser usados em transferencia, reserva ou picking.
 */
@RestController
@RequestMapping("/api/estoque/lotes")
@RequiredArgsConstructor
public class LoteEstoqueController {
    private static final Set<String> STATUS_OPERACIONAIS = Set.of("ATIVO", "QUARENTENA", "BLOQUEADO", "VENCIDO", "INATIVO");

    private final LoteEstoqueRepository repository;
    private final DepositoRepository depositoRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('estoque:lote:leitura')")
    public List<LoteEstoque> listar(@AuthenticationPrincipal AuthenticatedUser user,
                                    @RequestParam(required = false) Long produtoId,
                                    @RequestParam(required = false) Long depositoId,
                                    @RequestParam(required = false) String status) {
        List<LoteEstoque> lotes;
        if (produtoId != null) {
            lotes = repository.findByEmpresaIdAndProdutoIdAndDeletedAtIsNullOrderByDataValidadeAsc(user.getEmpresaId(), produtoId);
            if (depositoId != null) {
                lotes = lotes.stream().filter(x -> depositoId.equals(x.getDepositoId())).toList();
            }
        } else if (depositoId != null && status != null && !status.isBlank()) {
            lotes = repository.findByEmpresaIdAndDepositoIdAndStatusAndDeletedAtIsNullOrderByDataValidadeAsc(
                    user.getEmpresaId(), depositoId, status);
        } else if (depositoId != null) {
            lotes = repository.findByEmpresaIdAndDepositoIdAndStatusAndDeletedAtIsNullOrderByDataValidadeAsc(
                    user.getEmpresaId(), depositoId, "ATIVO");
        } else {
            lotes = repository.findByEmpresaIdAndDeletedAtIsNullOrderByDataValidadeAsc(user.getEmpresaId());
        }
        if (status != null && !status.isBlank() && produtoId != null) {
            String st = status.trim().toUpperCase();
            lotes = lotes.stream().filter(x -> st.equalsIgnoreCase(x.getStatus())).toList();
        }
        return lotes;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('estoque:lote:escrita')")
    public LoteEstoque criar(@AuthenticationPrincipal AuthenticatedUser user,
                             @Valid @RequestBody Request request) {
        if (request.depositoId() != null) {
            depositoRepository.findByIdAndEmpresaIdAndAtivoTrue(request.depositoId(), user.getEmpresaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Deposito nao encontrado"));
        }
        repository.findByEmpresaIdAndProdutoIdAndCodigoAndDeletedAtIsNull(
                        user.getEmpresaId(), request.produtoId(), request.codigo().trim())
                .ifPresent(x -> { throw new BusinessException("Ja existe lote com este codigo para o produto"); });

        String status = normalizarStatus(request.status());
        if (request.dataValidade() != null && request.dataValidade().isBefore(LocalDate.now())
                && "ATIVO".equals(status)) {
            status = "VENCIDO";
        }

        LoteEstoque lote = new LoteEstoque();
        lote.setEmpresaId(user.getEmpresaId());
        lote.setProdutoId(request.produtoId());
        lote.setCodigo(request.codigo().trim());
        lote.setDataFabricacao(request.dataFabricacao());
        lote.setDataValidade(request.dataValidade());
        lote.setQuantidade(request.quantidade() == null ? BigDecimal.ZERO : request.quantidade());
        lote.setDepositoId(request.depositoId());
        lote.setStatus(status);
        return repository.save(lote);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('estoque:lote:escrita')")
    public LoteEstoque atualizar(@AuthenticationPrincipal AuthenticatedUser user,
                                 @PathVariable Long id, @Valid @RequestBody Request request) {
        LoteEstoque lote = repository.findByIdAndEmpresaIdAndDeletedAtIsNull(id, user.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Lote nao encontrado"));
        if (request.depositoId() != null) {
            depositoRepository.findByIdAndEmpresaIdAndAtivoTrue(request.depositoId(), user.getEmpresaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Deposito nao encontrado"));
        }
        repository.findByEmpresaIdAndProdutoIdAndCodigoAndDeletedAtIsNull(
                        user.getEmpresaId(), request.produtoId(), request.codigo().trim())
                .filter(x -> !x.getId().equals(id))
                .ifPresent(x -> { throw new BusinessException("Ja existe lote com este codigo para o produto"); });

        lote.setProdutoId(request.produtoId());
        lote.setCodigo(request.codigo().trim());
        lote.setDataFabricacao(request.dataFabricacao());
        lote.setDataValidade(request.dataValidade());
        if (request.quantidade() != null) {
            lote.setQuantidade(request.quantidade());
        }
        lote.setDepositoId(request.depositoId());
        if (request.status() != null && !request.status().isBlank()) {
            lote.setStatus(normalizarStatus(request.status()));
        }
        if (lote.getDataValidade() != null && lote.getDataValidade().isBefore(LocalDate.now())
                && "ATIVO".equals(lote.getStatus())) {
            lote.setStatus("VENCIDO");
        }
        return repository.save(lote);
    }

    @PostMapping("/{id}/quarentena")
    @Transactional
    @PreAuthorize("hasAuthority('estoque:lote:escrita')")
    public LoteEstoque quarentena(@AuthenticationPrincipal AuthenticatedUser user,
                                  @PathVariable Long id,
                                  @RequestBody(required = false) MotivoRequest body) {
        LoteEstoque lote = carregar(user.getEmpresaId(), id);
        if ("INATIVO".equals(lote.getStatus()) || "VENCIDO".equals(lote.getStatus())) {
            throw new BusinessException("Lote " + lote.getStatus() + " nao pode ir para quarentena");
        }
        if ("QUARENTENA".equals(lote.getStatus())) {
            throw new BusinessException("Lote ja esta em quarentena");
        }
        lote.setStatus("QUARENTENA");
        return repository.save(lote);
    }

    @PostMapping("/{id}/bloquear")
    @Transactional
    @PreAuthorize("hasAuthority('estoque:lote:escrita')")
    public LoteEstoque bloquear(@AuthenticationPrincipal AuthenticatedUser user,
                                @PathVariable Long id,
                                @RequestBody(required = false) MotivoRequest body) {
        LoteEstoque lote = carregar(user.getEmpresaId(), id);
        if ("INATIVO".equals(lote.getStatus())) {
            throw new BusinessException("Lote inativo nao pode ser bloqueado");
        }
        if ("BLOQUEADO".equals(lote.getStatus())) {
            throw new BusinessException("Lote ja esta bloqueado");
        }
        lote.setStatus("BLOQUEADO");
        return repository.save(lote);
    }

    @PostMapping("/{id}/liberar")
    @Transactional
    @PreAuthorize("hasAuthority('estoque:lote:escrita')")
    public LoteEstoque liberar(@AuthenticationPrincipal AuthenticatedUser user,
                               @PathVariable Long id,
                               @RequestBody(required = false) MotivoRequest body) {
        LoteEstoque lote = carregar(user.getEmpresaId(), id);
        if (!"QUARENTENA".equals(lote.getStatus()) && !"BLOQUEADO".equals(lote.getStatus())) {
            throw new BusinessException("Somente lotes em QUARENTENA ou BLOQUEADO podem ser liberados");
        }
        if (lote.getDataValidade() != null && lote.getDataValidade().isBefore(LocalDate.now())) {
            lote.setStatus("VENCIDO");
        } else {
            lote.setStatus("ATIVO");
        }
        return repository.save(lote);
    }

    @PostMapping("/vencer-expirados")
    @Transactional
    @PreAuthorize("hasAuthority('estoque:lote:escrita')")
    public List<LoteEstoque> vencerExpirados(@AuthenticationPrincipal AuthenticatedUser user) {
        LocalDate hoje = LocalDate.now();
        List<LoteEstoque> todos = repository.findByEmpresaIdAndDeletedAtIsNullOrderByDataValidadeAsc(user.getEmpresaId());
        List<LoteEstoque> alterados = new ArrayList<>();
        for (LoteEstoque lote : todos) {
            if ("ATIVO".equals(lote.getStatus())
                    && lote.getDataValidade() != null
                    && lote.getDataValidade().isBefore(hoje)) {
                lote.setStatus("VENCIDO");
                alterados.add(repository.save(lote));
            }
        }
        return alterados;
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('estoque:lote:escrita')")
    public void excluir(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        LoteEstoque lote = carregar(user.getEmpresaId(), id);
        lote.setStatus("INATIVO");
        lote.setDeletedAt(LocalDateTime.now());
        repository.save(lote);
    }

    private LoteEstoque carregar(Long empresaId, Long id) {
        return repository.findByIdAndEmpresaIdAndDeletedAtIsNull(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote nao encontrado"));
    }

    private static String normalizarStatus(String status) {
        if (status == null || status.isBlank()) {
            return "ATIVO";
        }
        String s = status.trim().toUpperCase();
        if (!STATUS_OPERACIONAIS.contains(s)) {
            throw new BusinessException("Status de lote invalido: " + status
                    + ". Use ATIVO, QUARENTENA, BLOQUEADO, VENCIDO ou INATIVO");
        }
        return s;
    }

    public record Request(@NotNull Long produtoId, @NotBlank String codigo, LocalDate dataFabricacao,
                          LocalDate dataValidade, BigDecimal quantidade, Long depositoId, String status) {}

    public record MotivoRequest(String motivo) {}
}
