package br.com.brasil_saas.rh.controller;

import br.com.brasil_saas.rh.model.EsocialEvento;
import br.com.brasil_saas.rh.service.EsocialFilaService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Fila eSocial. Transmissão real: microservices/esocial (esocial-jt). */
@RestController
@RequestMapping("/api/rh/esocial-fila")
@RequiredArgsConstructor
public class EsocialFilaController {

    private final EsocialFilaService svc;

    public record RegistrarReq(String tipo, Long funcionarioId, String payload) {}

    @GetMapping
    @PreAuthorize("hasAuthority('rh:funcionario:leitura')")
    public List<EsocialEvento> listar(@AuthenticationPrincipal AuthenticatedUser u,
                                      @RequestParam(required = false) String status) {
        return svc.listar(u.getEmpresaId(), status);
    }

    @GetMapping("/tipos")
    @PreAuthorize("hasAuthority('rh:funcionario:leitura')")
    public Map<String, String> tipos() {
        return svc.tiposSuportados();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('rh:funcionario:escrita')")
    public ResponseEntity<EsocialEvento> registrar(@AuthenticationPrincipal AuthenticatedUser u,
                                                   @RequestBody RegistrarReq r) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(svc.registrar(u.getEmpresaId(), r.tipo(), r.funcionarioId(), r.payload()));
    }

    @PostMapping("/{id}/transmitir")
    @PreAuthorize("hasAuthority('rh:funcionario:escrita')")
    public EsocialEvento transmitir(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) {
        return svc.transmitir(u.getEmpresaId(), id);
    }

    @PostMapping("/{id}/consultar")
    @PreAuthorize("hasAuthority('rh:funcionario:leitura')")
    public EsocialEvento consultar(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) {
        return svc.consultar(u.getEmpresaId(), id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('rh:funcionario:escrita')")
    public ResponseEntity<Void> excluir(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) {
        svc.excluir(u.getEmpresaId(), id);
        return ResponseEntity.noContent().build();
    }
}
