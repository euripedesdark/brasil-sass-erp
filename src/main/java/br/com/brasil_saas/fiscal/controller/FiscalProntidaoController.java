package br.com.brasil_saas.fiscal.controller;

import br.com.brasil_saas.fiscal.service.FiscalProntidaoService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/fiscal/prontidao")
@RequiredArgsConstructor
public class FiscalProntidaoController {

    private final FiscalProntidaoService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> checklist(@AuthenticationPrincipal AuthenticatedUser u) {
        return service.checklist(u.getEmpresaId());
    }
}
