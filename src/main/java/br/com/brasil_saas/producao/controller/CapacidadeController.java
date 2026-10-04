package br.com.brasil_saas.producao.controller;

import br.com.brasil_saas.producao.service.CapacidadeService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/producao/capacidade")
@RequiredArgsConstructor
public class CapacidadeController {
    private final CapacidadeService service;

    @PostMapping("/simular")
    @PreAuthorize("hasAuthority('producao:capacidade:leitura')")
    public Map<String, Object> simular(@AuthenticationPrincipal AuthenticatedUser u,
                                       @RequestBody CapacidadeService.Request request) {
        return service.simular(u.getEmpresaId(), request);
    }

    @PostMapping("/carga")
    @PreAuthorize("hasAuthority('producao:capacidade:leitura')")
    public List<Map<String, Object>> carga(@AuthenticationPrincipal AuthenticatedUser u,
                                           @RequestBody List<CapacidadeService.Request> pedidos) {
        return service.cargaPorCentro(u.getEmpresaId(), pedidos);
    }

    /** Reserva avulsa ou vinculada a OP (grava no calendario de carga). */
    @PostMapping("/agendar")
    @PreAuthorize("hasAuthority('producao:capacidade:escrita')")
    public Map<String, Object> agendar(@AuthenticationPrincipal AuthenticatedUser u,
                                       @RequestBody CapacidadeService.AgendarRequest request) {
        return service.agendar(u.getEmpresaId(), request);
    }

    /** Agenda uma OP existente. Corpo opcional: {"dataInicio":"2026-10-05"}. */
    @PostMapping("/ordens/{id}/agendar")
    @PreAuthorize("hasAuthority('producao:capacidade:escrita')")
    public Map<String, Object> agendarOrdem(@AuthenticationPrincipal AuthenticatedUser u,
                                            @PathVariable Long id,
                                            @RequestBody(required = false) Map<String, String> body) {
        LocalDate inicio = body == null || body.get("dataInicio") == null || body.get("dataInicio").isBlank()
                ? null : LocalDate.parse(body.get("dataInicio"));
        return service.agendarOrdem(u.getEmpresaId(), id, inicio);
    }

    @DeleteMapping("/ordens/{id}/agendamento")
    @PreAuthorize("hasAuthority('producao:capacidade:escrita')")
    public Map<String, Object> liberar(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) {
        return Map.of("ordemProducaoId", id, "linhasLiberadas", service.liberarOrdem(u.getEmpresaId(), id));
    }

    @GetMapping("/calendario")
    @PreAuthorize("hasAuthority('producao:capacidade:leitura')")
    public List<Map<String, Object>> calendario(
            @AuthenticationPrincipal AuthenticatedUser u,
            @RequestParam Long centroId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        return service.calendario(u.getEmpresaId(), centroId, de, ate);
    }
}
