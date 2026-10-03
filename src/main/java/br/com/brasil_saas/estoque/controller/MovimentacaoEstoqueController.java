package br.com.brasil_saas.estoque.controller;

import br.com.brasil_saas.estoque.model.MovimentacaoEstoque;
import br.com.brasil_saas.estoque.repository.MovimentacaoEstoqueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import br.com.brasil_saas.shared.security.AuthenticatedUser;

import java.util.List;

@RestController
@RequestMapping("/api/estoque/movimentacoes")
@RequiredArgsConstructor
public class MovimentacaoEstoqueController {

    private final MovimentacaoEstoqueRepository repo;

    /**
     * Com produtoId: historico do produto.
     * Sem produtoId: ultimas movimentacoes da empresa.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('estoque:movimentacao:leitura')")
    public List<MovimentacaoEstoque> listar(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false) Long produtoId) {

        if (produtoId != null) {
            return repo.findByEmpresaIdAndProdutoIdAndDeletedAtIsNullOrderByDataMovimentoDesc(user.getEmpresaId(), produtoId);
        }
        return repo.findByEmpresaIdAndDeletedAtIsNullOrderByDataMovimentoDesc(user.getEmpresaId());
    }
}
