package br.com.brasil_saas.agenda.controller;

import br.com.brasil_saas.agenda.model.EventoAgenda;
import br.com.brasil_saas.agenda.service.AgendaService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/agenda")
@RequiredArgsConstructor
public class AgendaController {

    private final AgendaService service;

    @GetMapping("/eventos")
    @PreAuthorize("isAuthenticated()")
    public List<EventoAgenda> listar(@AuthenticationPrincipal AuthenticatedUser u) {
        return service.listar(u.getEmpresaId());
    }

    @GetMapping("/eventos/hoje")
    @PreAuthorize("isAuthenticated()")
    public List<EventoAgenda> hoje(@AuthenticationPrincipal AuthenticatedUser u) {
        return service.doDia(u.getEmpresaId(), LocalDate.now());
    }

    @GetMapping("/eventos/semana")
    @PreAuthorize("isAuthenticated()")
    public List<EventoAgenda> semana(@AuthenticationPrincipal AuthenticatedUser u) {
        return service.daSemana(u.getEmpresaId());
    }

    @GetMapping("/eventos/dia")
    @PreAuthorize("isAuthenticated()")
    public List<EventoAgenda> dia(@AuthenticationPrincipal AuthenticatedUser u,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return service.doDia(u.getEmpresaId(), data);
    }

    @PostMapping("/eventos")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EventoAgenda> criar(@RequestBody EventoAgenda e, @AuthenticationPrincipal AuthenticatedUser u) {
        e.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.salvar(u.getEmpresaId(), e));
    }

    @PutMapping("/eventos/{id}")
    @PreAuthorize("isAuthenticated()")
    public EventoAgenda atualizar(@PathVariable Long id, @RequestBody EventoAgenda e, @AuthenticationPrincipal AuthenticatedUser u) {
        e.setId(id);
        return service.salvar(u.getEmpresaId(), e);
    }

    @PostMapping("/eventos/{id}/concluir")
    @PreAuthorize("isAuthenticated()")
    public EventoAgenda concluir(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser u) {
        return service.concluir(u.getEmpresaId(), id);
    }

    @DeleteMapping("/eventos/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> cancelar(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser u) {
        service.cancelar(u.getEmpresaId(), id);
        return ResponseEntity.noContent().build();
    }
}
