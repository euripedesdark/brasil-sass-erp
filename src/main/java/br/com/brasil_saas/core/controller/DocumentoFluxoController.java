package br.com.brasil_saas.core.controller;

import br.com.brasil_saas.core.model.DocumentoFluxo;
import br.com.brasil_saas.core.service.DocumentoFluxoService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/core/documento-fluxo")
@RequiredArgsConstructor
public class DocumentoFluxoController {

    private final DocumentoFluxoService svc;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<DocumentoFluxo> ligacoes(
            @AuthenticationPrincipal AuthenticatedUser u,
            @RequestParam String tipo,
            @RequestParam Long id) {
        return svc.ligacoes(u.getEmpresaId(), tipo, id);
    }

    @GetMapping("/arvore")
    @PreAuthorize("isAuthenticated()")
    public List<Map<String, Object>> arvore(
            @AuthenticationPrincipal AuthenticatedUser u,
            @RequestParam String tipo,
            @RequestParam Long id) {
        return svc.fluxo(u.getEmpresaId(), tipo, id);
    }
}
