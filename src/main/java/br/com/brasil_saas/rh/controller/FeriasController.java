package br.com.brasil_saas.rh.controller;
import br.com.brasil_saas.rh.model.Ferias;
import br.com.brasil_saas.rh.service.FeriasService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
@RestController @RequestMapping("/api/rh/ferias") @RequiredArgsConstructor
public class FeriasController {
    private final FeriasService svc;
    @GetMapping @PreAuthorize("hasAuthority('rh:ferias:leitura')")
    public List<Ferias> listar(@AuthenticationPrincipal AuthenticatedUser u) { return svc.listar(u.getEmpresaId()); }
    @PostMapping @PreAuthorize("hasAuthority('rh:ferias:escrita')")
    public Ferias programar(@AuthenticationPrincipal AuthenticatedUser u, @RequestParam Long funcionarioId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio, @RequestParam(required = false, defaultValue = "30") Integer dias, @RequestParam(required = false) String observacao) { return svc.programar(u.getEmpresaId(), funcionarioId, inicio, dias, observacao); }
    @PostMapping("/{id}/gozar") @PreAuthorize("hasAuthority('rh:ferias:escrita')")
    public Ferias gozar(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id, @RequestParam Long folhaId) { return svc.gozar(u.getEmpresaId(), id, folhaId); }
    @PostMapping("/{id}/cancelar") @PreAuthorize("hasAuthority('rh:ferias:escrita')")
    public Ferias cancelar(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) { return svc.cancelar(u.getEmpresaId(), id); }
}
