package br.com.brasil_saas.financeiro.controller;
import br.com.brasil_saas.financeiro.model.Emprestimo;
import br.com.brasil_saas.financeiro.service.EmprestimoService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/financeiro/emprestimos") @RequiredArgsConstructor
public class EmprestimoController {
    private final EmprestimoService svc;
    @GetMapping @PreAuthorize("hasAuthority('financeiro:titulo:leitura')")
    public List<Emprestimo> listar(@AuthenticationPrincipal AuthenticatedUser u) { return svc.listar(u.getEmpresaId()); }
    @PostMapping @PreAuthorize("hasAuthority('financeiro:titulo:escrita')")
    public ResponseEntity<Emprestimo> salvar(@AuthenticationPrincipal AuthenticatedUser u, @RequestBody Emprestimo e) { return ResponseEntity.status(HttpStatus.CREATED).body(svc.salvar(u.getEmpresaId(), e)); }
    @PostMapping("/{id}/quitar") @PreAuthorize("hasAuthority('financeiro:titulo:escrita')")
    public Emprestimo quitar(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) { return svc.quitar(u.getEmpresaId(), id); }
    @DeleteMapping("/{id}") @PreAuthorize("hasAuthority('financeiro:titulo:escrita')")
    public ResponseEntity<Void> excluir(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) { svc.excluir(u.getEmpresaId(), id); return ResponseEntity.noContent().build(); }
}
