package br.com.brasil_saas.fiscal.controller;

import br.com.brasil_saas.fiscal.model.RegraTributaria;
import br.com.brasil_saas.fiscal.service.RegraTributariaService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fiscal/regras-tributarias")
@RequiredArgsConstructor
public class RegraTributariaController {

    private final RegraTributariaService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<RegraTributaria> listar(@AuthenticationPrincipal AuthenticatedUser u) {
        return service.listar(u.getEmpresaId());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public RegraTributaria buscar(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser u) {
        return service.buscar(u.getEmpresaId(), id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('fiscal:escrita') or isAuthenticated()")
    public ResponseEntity<RegraTributaria> criar(@RequestBody RegraTributaria r,
                                                @AuthenticationPrincipal AuthenticatedUser u) {
        r.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.salvar(u.getEmpresaId(), r));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('fiscal:escrita') or isAuthenticated()")
    public RegraTributaria atualizar(@PathVariable Long id, @RequestBody RegraTributaria r,
                                     @AuthenticationPrincipal AuthenticatedUser u) {
        r.setId(id);
        return service.salvar(u.getEmpresaId(), r);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('fiscal:escrita') or isAuthenticated()")
    public ResponseEntity<Void> excluir(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser u) {
        service.excluir(u.getEmpresaId(), id);
        return ResponseEntity.noContent().build();
    }
}
