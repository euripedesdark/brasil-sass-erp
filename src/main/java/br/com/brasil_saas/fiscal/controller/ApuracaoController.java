package br.com.brasil_saas.fiscal.controller;
import br.com.brasil_saas.fiscal.model.Apuracao;
import br.com.brasil_saas.fiscal.service.ApuracaoService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/fiscal/apuracoes") @RequiredArgsConstructor
public class ApuracaoController {
    private final ApuracaoService svc;
    @GetMapping @PreAuthorize("hasAuthority('fiscal:apuracao:leitura')")
    public List<Apuracao> listar(@AuthenticationPrincipal AuthenticatedUser u) { return svc.listar(u.getEmpresaId()); }
    @PostMapping("/calcular") @PreAuthorize("hasAuthority('fiscal:apuracao:escrita')")
    public Apuracao calcular(@AuthenticationPrincipal AuthenticatedUser u, @RequestParam Long impostoId, @RequestParam String competencia) { return svc.calcular(u.getEmpresaId(), impostoId, competencia); }
    @PostMapping("/{id}/encerrar") @PreAuthorize("hasAuthority('fiscal:apuracao:escrita')")
    public Apuracao encerrar(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) { return svc.encerrar(u.getEmpresaId(), id); }
    @PostMapping("/{id}/reabrir") @PreAuthorize("hasAuthority('fiscal:apuracao:escrita')")
    public Apuracao reabrir(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) { return svc.reabrir(u.getEmpresaId(), id); }
    @PostMapping("/{id}/transmitir") @PreAuthorize("hasAuthority('fiscal:apuracao:escrita')")
    public Apuracao transmitir(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) { return svc.transmitir(u.getEmpresaId(), id); }
}
