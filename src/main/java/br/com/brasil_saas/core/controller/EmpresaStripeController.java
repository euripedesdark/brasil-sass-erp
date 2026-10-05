package br.com.brasil_saas.core.controller;

import br.com.brasil_saas.core.service.EmpresaStripeService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import br.com.brasil_saas.shared.model.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/core/minha-empresa/stripe")
@RequiredArgsConstructor
public class EmpresaStripeController {
    private final EmpresaStripeService service;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_DIRETORIA','ROLE_GESTOR','ROLE_SUPERUSER')")
    public Object obter(@AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.success(service.obter(user.getEmpresaId()));
    }

    @PutMapping
    @PreAuthorize("hasAnyAuthority('ROLE_DIRETORIA','ROLE_GESTOR','ROLE_SUPERUSER')")
    public Object salvar(
        @AuthenticationPrincipal AuthenticatedUser user,
        @RequestBody EmpresaStripeService.ConfigRequest request) {
        return ApiResponse.success(service.salvar(user.getEmpresaId(), request));
    }

    @PostMapping("/testar")
    @PreAuthorize("hasAnyAuthority('ROLE_DIRETORIA','ROLE_GESTOR','ROLE_SUPERUSER')")
    public Object testar(@AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.success(service.testar(user.getEmpresaId()));
    }
}
