package br.com.brasil_saas.fiscal.obrigacao;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
@RestController @RequestMapping("/api/fiscal/obrigacoes") @RequiredArgsConstructor
public class ObrigacaoController {
    private final ObrigacaoService svc;
    public record EntregarReq(String competencia, String protocolo) {}
    @GetMapping @PreAuthorize("hasAuthority('fiscal:obrigacao:leitura')")
    public List<FisObrigacao> catalogo(@AuthenticationPrincipal AuthenticatedUser u) { return svc.catalogo(u.getEmpresaId()); }
    @PostMapping @PreAuthorize("hasAuthority('fiscal:obrigacao:escrita')")
    public ResponseEntity<FisObrigacao> salvar(@AuthenticationPrincipal AuthenticatedUser u, @RequestBody FisObrigacao o) { return ResponseEntity.status(HttpStatus.CREATED).body(svc.salvar(u.getEmpresaId(), o)); }
    @PostMapping("/instalar-modelo") @PreAuthorize("hasAuthority('fiscal:obrigacao:escrita')")
    public List<FisObrigacao> instalar(@AuthenticationPrincipal AuthenticatedUser u) { return svc.instalarModelo(u.getEmpresaId()); }
    @GetMapping("/agenda") @PreAuthorize("hasAuthority('fiscal:obrigacao:leitura')")
    public List<Map<String, Object>> agenda(@AuthenticationPrincipal AuthenticatedUser u, @RequestParam(required = false) String competencia) { return svc.agenda(u.getEmpresaId(), competencia); }
    @PostMapping("/{id}/entregar") @PreAuthorize("hasAuthority('fiscal:obrigacao:escrita')")
    public FisObrigacaoEntrega entregar(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id, @RequestBody EntregarReq r) { return svc.entregar(u.getEmpresaId(), id, r.competencia(), r.protocolo()); }
}
