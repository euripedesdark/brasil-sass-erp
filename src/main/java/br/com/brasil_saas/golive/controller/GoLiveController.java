package br.com.brasil_saas.golive.controller;

import br.com.brasil_saas.golive.service.GoLiveService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/core/golive")
@RequiredArgsConstructor
public class GoLiveController {
    private final GoLiveService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> checklist(@AuthenticationPrincipal AuthenticatedUser u) {
        return service.checklist(u.getEmpresaId());
    }
}
