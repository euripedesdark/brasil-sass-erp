package br.com.brasil_saas.financeiro.controller;

import br.com.brasil_saas.financeiro.service.CopaService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/financeiro/copa")
@RequiredArgsConstructor
public class CopaController {

    private final CopaService svc;

    @GetMapping("/por-cliente")
    @PreAuthorize("hasAuthority('financeiro:leitura') or hasAuthority('financeiro:titulo:leitura') or isAuthenticated()")
    public Map<String, Object> porCliente(
            @AuthenticationPrincipal AuthenticatedUser u,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        return svc.porCliente(u.getEmpresaId(), de, ate);
    }

    @GetMapping("/por-periodo")
    @PreAuthorize("hasAuthority('financeiro:leitura') or hasAuthority('financeiro:titulo:leitura') or isAuthenticated()")
    public Map<String, Object> porPeriodo(
            @AuthenticationPrincipal AuthenticatedUser u,
            @RequestParam(required = false) Integer ano) {
        int a = ano != null ? ano : LocalDate.now().getYear();
        return svc.porPeriodo(u.getEmpresaId(), a);
    }
}
