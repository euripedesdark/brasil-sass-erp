package br.com.brasil_saas.compras.controller;

import br.com.brasil_saas.compras.model.ConferenciaFaturaCompra;
import br.com.brasil_saas.compras.model.ConferenciaFaturaCompraItem;
import br.com.brasil_saas.compras.service.ConferenciaFaturaCompraService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/compras/conferencia-faturas")
@RequiredArgsConstructor
public class ConferenciaFaturaCompraController {

    private final ConferenciaFaturaCompraService service;

    @GetMapping
    @PreAuthorize("hasAuthority('compras:pedido:leitura')")
    public List<ConferenciaFaturaCompra> listar(
            @AuthenticationPrincipal AuthenticatedUser user) {
        return service.listar(user.getEmpresaId());
    }

    @GetMapping("/{id}/itens")
    @PreAuthorize("hasAuthority('compras:pedido:leitura')")
    public List<ConferenciaFaturaCompraItem> itens(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id) {
        return service.listarItens(user.getEmpresaId(), id);
    }

    @PostMapping("/{id}/aprovacao-excepcional")
    @PreAuthorize("hasAuthority('compras:conferencia:aprovar')")
    public ConferenciaFaturaCompra aprovarExcepcional(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id,
            @Valid @RequestBody ConferenciaFaturaCompraService.AprovacaoExcepcionalRequest request) {
        return service.aprovarExcepcional(user.getEmpresaId(), user.getId(), id, request.motivo());
    }

    @PostMapping

    @PreAuthorize("hasAuthority('compras:pedido:escrita')")
    public ConferenciaFaturaCompra conferir(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody ConferenciaFaturaCompraService.Request request) {
        return service.conferir(user.getEmpresaId(), request);
    }
}
