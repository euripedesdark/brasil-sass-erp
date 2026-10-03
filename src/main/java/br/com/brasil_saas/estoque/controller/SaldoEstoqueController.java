package br.com.brasil_saas.estoque.controller;

import br.com.brasil_saas.estoque.model.SaldoEstoque;
import br.com.brasil_saas.estoque.repository.SaldoEstoqueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import br.com.brasil_saas.shared.security.AuthenticatedUser;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/estoque/saldos")
@RequiredArgsConstructor
public class SaldoEstoqueController {

    private final SaldoEstoqueRepository repository;

    /**
     * Com produtoId: saldo de um produto.
     * Sem produtoId: todos os saldos da empresa.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('estoque:saldo:leitura')")
    public ResponseEntity<?> buscar(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false) Long produtoId) {

        if (produtoId != null) {
            return repository.findByEmpresaIdAndProdutoId(user.getEmpresaId(), produtoId)
                    .map(saldo -> ResponseEntity.ok(SaldoResponse.from(saldo)))
                    .orElseGet(() -> ResponseEntity.ok(new SaldoResponse(user.getEmpresaId(), produtoId, BigDecimal.ZERO)));
        }

        List<SaldoResponse> lista = repository.findByEmpresaIdOrderByProdutoIdAsc(user.getEmpresaId()).stream()
                .map(SaldoResponse::from)
                .toList();
        return ResponseEntity.ok(lista);
    }

    public record SaldoResponse(Long empresaId, Long produtoId, BigDecimal quantidade) {
        static SaldoResponse from(SaldoEstoque saldo) {
            return new SaldoResponse(saldo.getEmpresaId(), saldo.getProdutoId(), saldo.getQuantidade());
        }
    }
}
