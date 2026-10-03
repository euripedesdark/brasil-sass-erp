package br.com.brasil_saas.projetos.controller;
import br.com.brasil_saas.projetos.model.*;
import br.com.brasil_saas.projetos.service.ProjetoService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
@RestController @RequestMapping("/api/projetos") @RequiredArgsConstructor
public class ProjetoController {
    private final ProjetoService svc;
    public record PctReq(Integer percentual) {}
    public record DecisaoReq(Boolean aprovar) {}
    @GetMapping @PreAuthorize("hasAuthority('projetos:leitura')")
    public List<PrjProjeto> projetos(@AuthenticationPrincipal AuthenticatedUser u, @RequestParam(required = false) String status) { return svc.projetos(u.getEmpresaId(), status); }
    @PostMapping @PreAuthorize("hasAuthority('projetos:escrita')")
    public ResponseEntity<PrjProjeto> salvar(@AuthenticationPrincipal AuthenticatedUser u, @RequestBody PrjProjeto p) { return ResponseEntity.status(HttpStatus.CREATED).body(svc.salvar(u.getEmpresaId(), p)); }
    @GetMapping("/{id}/resumo") @PreAuthorize("hasAuthority('projetos:leitura')")
    public Map<String, Object> resumo(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) { return svc.resumo(u.getEmpresaId(), id); }
    @GetMapping("/{id}/etapas") @PreAuthorize("hasAuthority('projetos:leitura')")
    public List<PrjEtapa> etapas(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) { return svc.etapas(u.getEmpresaId(), id); }
    @PostMapping("/{id}/etapas") @PreAuthorize("hasAuthority('projetos:escrita')")
    public ResponseEntity<PrjEtapa> salvarEtapa(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id, @RequestBody PrjEtapa e) { return ResponseEntity.status(HttpStatus.CREATED).body(svc.salvarEtapa(u.getEmpresaId(), id, e)); }
    @PostMapping("/{pid}/etapas/{eid}/avancar") @PreAuthorize("hasAuthority('projetos:escrita')")
    public PrjEtapa avancar(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long pid, @PathVariable Long eid, @RequestBody(required = false) PctReq r) { return svc.avancarEtapa(u.getEmpresaId(), pid, eid, r == null ? null : r.percentual()); }
    @GetMapping("/{id}/movimentos") @PreAuthorize("hasAuthority('projetos:leitura')")
    public List<PrjMovimento> movimentos(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id, @RequestParam(required = false) String tipo) { return svc.movimentos(u.getEmpresaId(), id, tipo); }
    @PostMapping("/{id}/movimentos") @PreAuthorize("hasAuthority('projetos:escrita')")
    public ResponseEntity<PrjMovimento> lancar(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id, @RequestBody PrjMovimento m) { return ResponseEntity.status(HttpStatus.CREATED).body(svc.lancarMovimento(u.getEmpresaId(), id, m)); }
    @GetMapping("/{id}/riscos") @PreAuthorize("hasAuthority('projetos:leitura')")
    public List<PrjRisco> riscos(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) { return svc.riscos(u.getEmpresaId(), id); }
    @PostMapping("/{id}/riscos") @PreAuthorize("hasAuthority('projetos:escrita')")
    public ResponseEntity<PrjRisco> salvarRisco(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id, @RequestBody PrjRisco r) { return ResponseEntity.status(HttpStatus.CREATED).body(svc.salvarRisco(u.getEmpresaId(), id, r)); }
    @GetMapping("/{id}/mudancas") @PreAuthorize("hasAuthority('projetos:leitura')")
    public List<PrjMudanca> mudancas(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) { return svc.mudancas(u.getEmpresaId(), id); }
    @PostMapping("/{id}/mudancas") @PreAuthorize("hasAuthority('projetos:escrita')")
    public ResponseEntity<PrjMudanca> solicitar(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id, @RequestBody PrjMudanca m) { return ResponseEntity.status(HttpStatus.CREATED).body(svc.solicitarMudanca(u.getEmpresaId(), id, m)); }
    @PostMapping("/{pid}/mudancas/{mid}/decidir") @PreAuthorize("hasAuthority('projetos:escrita')")
    public PrjMudanca decidir(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long pid, @PathVariable Long mid, @RequestBody DecisaoReq r) { return svc.decidirMudanca(u.getEmpresaId(), u.getId(), pid, mid, Boolean.TRUE.equals(r.aprovar())); }
    @GetMapping("/{id}/faturamentos") @PreAuthorize("hasAuthority('projetos:leitura')")
    public List<PrjFaturamento> faturamentos(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) { return svc.faturamentos(u.getEmpresaId(), id); }
    @PostMapping("/{id}/faturamentos") @PreAuthorize("hasAuthority('projetos:escrita')")
    public ResponseEntity<PrjFaturamento> salvarFat(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id, @RequestBody PrjFaturamento f) { return ResponseEntity.status(HttpStatus.CREATED).body(svc.salvarFaturamento(u.getEmpresaId(), id, f)); }
    @PostMapping("/{pid}/faturamentos/{fid}/faturar") @PreAuthorize("hasAuthority('projetos:escrita')")
    public PrjFaturamento faturar(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long pid, @PathVariable Long fid) { return svc.faturar(u.getEmpresaId(), pid, fid); }
}
