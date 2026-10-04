package br.com.brasil_saas.financeiro.controller;
import br.com.brasil_saas.financeiro.model.Orcamento;
import br.com.brasil_saas.financeiro.model.OrcamentoRealizado;
import br.com.brasil_saas.financeiro.service.OrcamentoService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
@RestController @RequestMapping("/api/financeiro/orcamentos") @RequiredArgsConstructor
public class OrcamentoController {
    private final OrcamentoService svc;
    public record RealizadoReq(Integer mes, BigDecimal valor) {}
    @GetMapping @PreAuthorize("hasAuthority('financeiro:titulo:leitura')")
    public List<Orcamento> listar(@AuthenticationPrincipal AuthenticatedUser u, @RequestParam(required = false) Integer ano) { return svc.listar(u.getEmpresaId(), ano); }
    @PostMapping @PreAuthorize("hasAuthority('financeiro:titulo:escrita')")
    public ResponseEntity<Orcamento> salvar(@AuthenticationPrincipal AuthenticatedUser u, @RequestBody Orcamento o) { return ResponseEntity.status(HttpStatus.CREATED).body(svc.salvar(u.getEmpresaId(), o)); }
    @DeleteMapping("/{id}") @PreAuthorize("hasAuthority('financeiro:titulo:escrita')")
    public ResponseEntity<Void> excluir(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) { svc.excluir(u.getEmpresaId(), id); return ResponseEntity.noContent().build(); }
    @PostMapping("/{id}/realizado") @PreAuthorize("hasAuthority('financeiro:titulo:escrita')")
    public ResponseEntity<OrcamentoRealizado> lancar(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id, @RequestBody RealizadoReq r) { return ResponseEntity.status(HttpStatus.CREATED).body(svc.lancarRealizado(u.getEmpresaId(), id, r.mes(), r.valor())); }
    @GetMapping("/acompanhamento") @PreAuthorize("hasAuthority('financeiro:titulo:leitura')")
    public List<Map<String, Object>> acompanhamento(@AuthenticationPrincipal AuthenticatedUser u, @RequestParam(required = false) Integer ano) { return svc.acompanhamento(u.getEmpresaId(), ano); }
}
