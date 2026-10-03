package br.com.brasil_saas.portais.controller;
import br.com.brasil_saas.portais.model.*;
import br.com.brasil_saas.portais.service.PortalService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
@RestController @RequestMapping("/api/portais") @RequiredArgsConstructor
public class PortalController {
    private final PortalService svc;
    public record GerarReq(String tipo, Long pessoaId, Integer diasValidade) {}
    @GetMapping("/acessos") @PreAuthorize("hasAuthority('portal:leitura')")
    public List<PtlAcesso> acessos(@AuthenticationPrincipal AuthenticatedUser u) { return svc.acessos(u.getEmpresaId()); }
    @PostMapping("/acessos") @PreAuthorize("hasAuthority('portal:escrita')")
    public ResponseEntity<PtlAcesso> gerar(@AuthenticationPrincipal AuthenticatedUser u, @RequestBody GerarReq r) { return ResponseEntity.status(HttpStatus.CREATED).body(svc.gerar(u.getEmpresaId(), u.getId(), r.tipo(), r.pessoaId(), r.diasValidade())); }
    @PostMapping("/acessos/{id}/revogar") @PreAuthorize("hasAuthority('portal:escrita')")
    public ResponseEntity<Void> revogar(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) { svc.revogar(u.getEmpresaId(), id); return ResponseEntity.noContent().build(); }
    @GetMapping("/publico/validar")
    public Map<String, Object> validar(@RequestParam String token) { return svc.validar(token); }
    @GetMapping("/publico/minha-conta")
    public Map<String, Object> minhaConta(@RequestParam String token) { return svc.minhaConta(token); }
}
