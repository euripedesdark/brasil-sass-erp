package br.com.brasil_saas.contratosvenda.controller;

import br.com.brasil_saas.contratosvenda.model.ContratoVenda;
import br.com.brasil_saas.contratosvenda.service.ContratoVendaService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vendas/contratos")
@RequiredArgsConstructor
public class ContratoVendaController {
    private final ContratoVendaService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<ContratoVenda> listar(@AuthenticationPrincipal AuthenticatedUser u, @RequestParam(required = false) String status) {
        return service.listar(u.getEmpresaId(), status);
    }

    @GetMapping("/resumo")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> resumo(@AuthenticationPrincipal AuthenticatedUser u) {
        return service.resumo(u.getEmpresaId());
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ContratoVenda> criar(@RequestBody ContratoVenda c, @AuthenticationPrincipal AuthenticatedUser u) {
        c.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.salvar(u.getEmpresaId(), c));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ContratoVenda atualizar(@PathVariable Long id, @RequestBody ContratoVenda c, @AuthenticationPrincipal AuthenticatedUser u) {
        c.setId(id);
        return service.salvar(u.getEmpresaId(), c);
    }

    @PostMapping("/{id}/ativar")
    @PreAuthorize("isAuthenticated()")
    public ContratoVenda ativar(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser u) {
        return service.ativar(u.getEmpresaId(), id);
    }

    @PostMapping("/{id}/encerrar")
    @PreAuthorize("isAuthenticated()")
    public ContratoVenda encerrar(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser u) {
        return service.encerrar(u.getEmpresaId(), id);
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("isAuthenticated()")
    public ContratoVenda cancelar(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser u) {
        return service.cancelar(u.getEmpresaId(), id);
    }
}
