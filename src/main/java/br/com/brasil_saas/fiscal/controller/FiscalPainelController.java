package br.com.brasil_saas.fiscal.controller;

import br.com.brasil_saas.fiscal.service.FiscalPainelService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/fiscal/painel")
@RequiredArgsConstructor
public class FiscalPainelController {

    private final FiscalPainelService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> resumo(@AuthenticationPrincipal AuthenticatedUser u,
                                      @RequestParam(required = false) String competencia) {
        return service.resumo(u.getEmpresaId(), competencia);
    }
}
