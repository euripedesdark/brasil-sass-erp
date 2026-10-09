package br.com.brasil_saas.conhecimento.controller;

import br.com.brasil_saas.conhecimento.model.ArtigoKb;
import br.com.brasil_saas.conhecimento.service.ConhecimentoService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conhecimento")
@RequiredArgsConstructor
public class ConhecimentoController {
    private final ConhecimentoService service;

    @GetMapping("/artigos")
    @PreAuthorize("isAuthenticated()")
    public List<ArtigoKb> listar(@AuthenticationPrincipal AuthenticatedUser u, @RequestParam(required = false) String q) {
        return service.listar(u.getEmpresaId(), q);
    }

    @GetMapping("/artigos/{id}")
    @PreAuthorize("isAuthenticated()")
    public ArtigoKb buscar(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser u) {
        return service.buscar(u.getEmpresaId(), id);
    }

    @PostMapping("/artigos")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ArtigoKb> criar(@RequestBody ArtigoKb a, @AuthenticationPrincipal AuthenticatedUser u) {
        a.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.salvar(u.getEmpresaId(), a));
    }

    @PutMapping("/artigos/{id}")
    @PreAuthorize("isAuthenticated()")
    public ArtigoKb atualizar(@PathVariable Long id, @RequestBody ArtigoKb a, @AuthenticationPrincipal AuthenticatedUser u) {
        a.setId(id);
        return service.salvar(u.getEmpresaId(), a);
    }

    @DeleteMapping("/artigos/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> excluir(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser u) {
        service.excluir(u.getEmpresaId(), id);
        return ResponseEntity.noContent().build();
    }
}
