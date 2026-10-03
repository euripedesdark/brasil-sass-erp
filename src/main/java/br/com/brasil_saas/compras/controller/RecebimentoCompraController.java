package br.com.brasil_saas.compras.controller;

import br.com.brasil_saas.compras.model.RecebimentoCompra;
import br.com.brasil_saas.compras.model.RecebimentoCompraItem;
import br.com.brasil_saas.compras.repository.RecebimentoCompraItemRepository;
import br.com.brasil_saas.compras.repository.RecebimentoCompraRepository;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/compras/recebimentos")
@RequiredArgsConstructor
public class RecebimentoCompraController {

    private final RecebimentoCompraRepository repository;
    private final RecebimentoCompraItemRepository itemRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('compras:pedido:leitura')")
    public List<RecebimentoCompra> listar(@AuthenticationPrincipal AuthenticatedUser user,
                                          @RequestParam(required = false) Long pedidoId) {
        if (pedidoId != null) {
            return repository.findByEmpresaIdAndPedidoIdAndDeletedAtIsNullOrderByDataRecebimentoDesc(
                    user.getEmpresaId(), pedidoId);
        }
        return repository.findByEmpresaIdOrderByDataRecebimentoDescCreatedAtDesc(user.getEmpresaId());
    }

    @GetMapping("/{id}/itens")
    @PreAuthorize("hasAuthority('compras:pedido:leitura')")
    public List<RecebimentoCompraItem> listarItens(@AuthenticationPrincipal AuthenticatedUser user,
                                                   @PathVariable Long id) {
        repository.findByIdAndEmpresaIdAndDeletedAtIsNull(id, user.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Recebimento nao encontrado"));
        return itemRepository.findByEmpresaIdAndRecebimentoIdAndDeletedAtIsNullOrderByIdAsc(
                user.getEmpresaId(), id);
    }
}
