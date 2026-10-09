package br.com.brasil_saas.fiscal.controller;

import br.com.brasil_saas.fiscal.model.Reinf;
import br.com.brasil_saas.fiscal.service.ReinfService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fiscal/reinf")
@RequiredArgsConstructor
public class ReinfController {

    private final ReinfService svc;

    @GetMapping
    @PreAuthorize("hasAuthority('fiscal:sped:leitura') or hasAuthority('fiscal:apuracao:leitura')")
    public List<Reinf> listar(@AuthenticationPrincipal AuthenticatedUser u,
                              @RequestParam(required = false) String competencia) {
        if (competencia != null && !competencia.isBlank()) {
            return svc.listarCompetencia(u.getEmpresaId(), competencia);
        }
        return svc.listar(u.getEmpresaId());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('fiscal:sped:leitura') or hasAuthority('fiscal:apuracao:leitura')")
    public Map<String, Object> detalhe(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) {
        return svc.detalhe(u.getEmpresaId(), id);
    }

    @PostMapping("/gerar")
    @PreAuthorize("hasAuthority('fiscal:sped:gerar') or hasAuthority('fiscal:apuracao:escrita')")
    public List<Reinf> gerar(@AuthenticationPrincipal AuthenticatedUser u,
                             @RequestParam String competencia) {
        return svc.gerarPeriodo(u.getEmpresaId(), competencia);
    }

    @PostMapping("/fechar")
    @PreAuthorize("hasAuthority('fiscal:sped:gerar') or hasAuthority('fiscal:apuracao:escrita')")
    public Reinf fechar(@AuthenticationPrincipal AuthenticatedUser u,
                        @RequestParam String competencia) {
        return svc.fechar(u.getEmpresaId(), competencia);
    }
}
