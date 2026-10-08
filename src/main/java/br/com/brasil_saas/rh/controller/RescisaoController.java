package br.com.brasil_saas.rh.controller;

import br.com.brasil_saas.rh.model.Rescisao;
import br.com.brasil_saas.rh.service.RescisaoService;
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
import java.util.Map;

@RestController
@RequestMapping("/api/rh/rescisao")
@RequiredArgsConstructor
public class RescisaoController {

    private final RescisaoService svc;

    public record EfetivarReq(
            Long funcionarioId,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desligamento,
            String motivo) {}

    @GetMapping("/calcular")
    @PreAuthorize("hasAuthority('rh:funcionario:leitura') or hasAuthority('rh:folha:leitura')")
    public Map<String, Object> calcular(
            @AuthenticationPrincipal AuthenticatedUser u,
            @RequestParam Long funcionarioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desligamento,
            @RequestParam(required = false) String motivo) {
        return svc.calcular(u.getEmpresaId(), funcionarioId, desligamento, motivo);
    }

    @PostMapping("/efetivar")
    @PreAuthorize("hasAuthority('rh:funcionario:escrita') or hasAuthority('rh:folha:escrita')")
    public ResponseEntity<Rescisao> efetivar(
            @AuthenticationPrincipal AuthenticatedUser u,
            @RequestBody EfetivarReq body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(svc.efetivar(u.getEmpresaId(), body.funcionarioId(), body.desligamento(), body.motivo()));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('rh:funcionario:leitura') or hasAuthority('rh:folha:leitura')")
    public List<Rescisao> listar(@AuthenticationPrincipal AuthenticatedUser u) {
        return svc.listar(u.getEmpresaId());
    }
}
