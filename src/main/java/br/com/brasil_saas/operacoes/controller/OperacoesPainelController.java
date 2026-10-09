package br.com.brasil_saas.operacoes.controller;

import br.com.brasil_saas.operacoes.service.OperacoesPainelService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/operacoes")
@RequiredArgsConstructor
public class OperacoesPainelController {

    private final OperacoesPainelService service;

    @GetMapping("/painel")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> painel(@AuthenticationPrincipal AuthenticatedUser u) {
        return service.resumo(u.getEmpresaId());
    }
}
