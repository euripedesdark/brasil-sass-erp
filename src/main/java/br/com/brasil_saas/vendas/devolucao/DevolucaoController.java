package br.com.brasil_saas.vendas.devolucao;
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
@RestController @RequestMapping("/api/vendas/devolucoes") @RequiredArgsConstructor
public class DevolucaoController {
    private final DevolucaoService svc;
    public record SolicitarReq(Long pedidoId, String motivo, Map<Long, BigDecimal> itens) {}
    public record DecidirReq(Boolean aprovar) {}
    @GetMapping @PreAuthorize("hasAuthority('vendas:pedido:leitura')")
    public List<VenDevolucao> listar(@AuthenticationPrincipal AuthenticatedUser u) { return svc.listar(u.getEmpresaId()); }
    @GetMapping("/{id}/itens") @PreAuthorize("hasAuthority('vendas:pedido:leitura')")
    public List<VenDevolucaoItem> itens(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) { return svc.itens(u.getEmpresaId(), id); }
    @PostMapping @PreAuthorize("hasAuthority('vendas:pedido:escrita')")
    public ResponseEntity<VenDevolucao> solicitar(@AuthenticationPrincipal AuthenticatedUser u, @RequestBody SolicitarReq r) { return ResponseEntity.status(HttpStatus.CREATED).body(svc.solicitar(u.getEmpresaId(), r.pedidoId(), r.motivo(), r.itens() == null ? Map.of() : r.itens())); }
    @PostMapping("/{id}/decidir") @PreAuthorize("hasAuthority('vendas:pedido:escrita')")
    public VenDevolucao decidir(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id, @RequestBody DecidirReq r) { return svc.decidir(u.getEmpresaId(), u.getId(), id, Boolean.TRUE.equals(r.aprovar())); }
    @PostMapping("/{id}/receber") @PreAuthorize("hasAuthority('vendas:pedido:escrita')")
    public VenDevolucao receber(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) { return svc.receber(u.getEmpresaId(), id); }
}
