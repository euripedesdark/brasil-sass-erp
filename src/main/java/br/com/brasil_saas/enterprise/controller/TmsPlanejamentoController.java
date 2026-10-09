package br.com.brasil_saas.enterprise.controller;

import br.com.brasil_saas.enterprise.service.PlanejadorCarga;
import br.com.brasil_saas.enterprise.service.TmsPlanejamentoService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enterprise/tms-planejamento")
@RequiredArgsConstructor
public class TmsPlanejamentoController {
    private final TmsPlanejamentoService service;

    public record Request(String origem, @NotNull BigDecimal capacidadePeso, @NotNull BigDecimal capacidadeVolume,
                          Long transportadoraId, LocalDateTime saida, @NotEmpty @Valid List<Entrega> entregas) {}
    public record Entrega(@NotNull String referenciaTipo, @NotNull Long referenciaId, String destino,
                          @NotNull BigDecimal peso, @NotNull BigDecimal volume) {}

    @PostMapping
    @PreAuthorize("hasAuthority('enterprise:escrita')")
    public List<Map<String, Object>> planejar(@Valid @RequestBody Request r, @AuthenticationPrincipal AuthenticatedUser u) {
        var entregas = r.entregas().stream().map(e -> new PlanejadorCarga.Entrega(e.referenciaTipo(), e.referenciaId(),
                e.destino(), e.peso(), e.volume())).toList();
        return service.planejar(u.getEmpresaId(), r.origem(), r.capacidadePeso(), r.capacidadeVolume(),
                r.transportadoraId(), r.saida(), entregas);
    }
}
