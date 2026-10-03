package br.com.brasil_saas.producao.controller;

import br.com.brasil_saas.producao.model.EstruturaProduto;
import br.com.brasil_saas.producao.repository.EstruturaProdutoRepository;
import br.com.brasil_saas.shared.exception.BusinessException;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/producao/estruturas")
@RequiredArgsConstructor
public class EstruturaProdutoController {
    private final EstruturaProdutoRepository repository;

    @GetMapping("/{produtoPaiId}")
    @PreAuthorize("hasAuthority('producao:estrutura:leitura')")
    public List<EstruturaProduto> listar(@AuthenticationPrincipal AuthenticatedUser user,
                                         @PathVariable Long produtoPaiId) {
        return repository.findByEmpresaIdAndProdutoPaiIdAndDeletedAtIsNullOrderByNivelAscIdAsc(
                user.getEmpresaId(), produtoPaiId);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('producao:estrutura:escrita')")
    public EstruturaProduto criar(@AuthenticationPrincipal AuthenticatedUser user,
                                  @RequestBody Request request) {
        if (request.produtoPaiId() == null || request.produtoFilhoId() == null
                || request.quantidade() == null || request.quantidade().signum() <= 0) {
            throw new BusinessException("Produto pai, componente e quantidade positiva sao obrigatorios");
        }
        if (request.produtoPaiId().equals(request.produtoFilhoId())) {
            throw new BusinessException("O produto nao pode ser componente de si mesmo");
        }
        EstruturaProduto e = repository
                .findByEmpresaIdAndProdutoPaiIdAndProdutoFilhoIdAndDeletedAtIsNull(
                        user.getEmpresaId(), request.produtoPaiId(), request.produtoFilhoId())
                .orElseGet(EstruturaProduto::new);
        e.setEmpresaId(user.getEmpresaId());
        e.setProdutoPaiId(request.produtoPaiId());
        e.setProdutoFilhoId(request.produtoFilhoId());
        e.setQuantidade(request.quantidade());
        e.setPerdaPercentual(request.perdaPercentual() == null ? BigDecimal.ZERO : request.perdaPercentual());
        e.setNivel(request.nivel() == null ? 1 : request.nivel());
        e.setAtivo(request.ativo() == null || request.ativo());
        e.setObservacao(request.observacao());
        return repository.save(e);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('producao:estrutura:escrita')")
    public void excluir(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        EstruturaProduto e = repository.findByIdAndEmpresaIdAndDeletedAtIsNull(id, user.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Componente da estrutura nao encontrado"));
        e.setDeletedAt(LocalDateTime.now());
        e.setAtivo(false);
        repository.save(e);
    }

    public record Request(Long produtoPaiId, Long produtoFilhoId, BigDecimal quantidade,
                          BigDecimal perdaPercentual, Integer nivel, Boolean ativo, String observacao) {}
}
