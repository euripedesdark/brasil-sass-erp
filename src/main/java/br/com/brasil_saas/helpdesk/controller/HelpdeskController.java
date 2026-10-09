package br.com.brasil_saas.helpdesk.controller;

import br.com.brasil_saas.helpdesk.model.Chamado;
import br.com.brasil_saas.helpdesk.model.ChamadoComentario;
import br.com.brasil_saas.helpdesk.service.HelpdeskService;
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
@RequestMapping("/api/helpdesk")
@RequiredArgsConstructor
public class HelpdeskController {

    private final HelpdeskService service;

    @GetMapping("/chamados")
    @PreAuthorize("isAuthenticated()")
    public List<Chamado> listar(@AuthenticationPrincipal AuthenticatedUser u,
                                @RequestParam(required = false) String status) {
        return service.listar(u.getEmpresaId(), status);
    }

    @GetMapping("/chamados/{id}")
    @PreAuthorize("isAuthenticated()")
    public Chamado buscar(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser u) {
        return service.buscar(u.getEmpresaId(), id);
    }

    @PostMapping("/chamados")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Chamado> criar(@RequestBody Chamado c, @AuthenticationPrincipal AuthenticatedUser u) {
        c.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.salvar(u.getEmpresaId(), c));
    }

    @PutMapping("/chamados/{id}")
    @PreAuthorize("isAuthenticated()")
    public Chamado atualizar(@PathVariable Long id, @RequestBody Chamado c, @AuthenticationPrincipal AuthenticatedUser u) {
        c.setId(id);
        return service.salvar(u.getEmpresaId(), c);
    }

    @PostMapping("/chamados/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public Chamado status(@PathVariable Long id, @RequestParam String status, @AuthenticationPrincipal AuthenticatedUser u) {
        return service.mudarStatus(u.getEmpresaId(), id, status);
    }

    @GetMapping("/chamados/{id}/comentarios")
    @PreAuthorize("isAuthenticated()")
    public List<ChamadoComentario> comentarios(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser u) {
        return service.comentarios(u.getEmpresaId(), id);
    }

    @PostMapping("/chamados/{id}/comentarios")
    @PreAuthorize("isAuthenticated()")
    public ChamadoComentario comentar(@PathVariable Long id, @RequestBody Map<String, String> body,
                                      @AuthenticationPrincipal AuthenticatedUser u) {
        return service.comentar(u.getEmpresaId(), id, body.get("autor"), body.get("texto"));
    }

    @GetMapping("/resumo")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> resumo(@AuthenticationPrincipal AuthenticatedUser u) {
        return service.resumo(u.getEmpresaId());
    }
}
