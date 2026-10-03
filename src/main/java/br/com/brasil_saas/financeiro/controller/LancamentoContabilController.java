package br.com.brasil_saas.financeiro.controller;
import br.com.brasil_saas.financeiro.dto.FinanceiroDtos.*;
import br.com.brasil_saas.financeiro.service.LancamentoContabilService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus; import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/financeiro/lancamentos") @RequiredArgsConstructor
public class LancamentoContabilController {
    private final LancamentoContabilService service;
    @GetMapping @PreAuthorize("hasAuthority('financeiro:lancamento:leitura')")
    public List<LancamentoResponse> listar(@AuthenticationPrincipal AuthenticatedUser u) {
        return service.listar(u.getEmpresaId());
    }
    @GetMapping("/{id}") @PreAuthorize("hasAuthority('financeiro:lancamento:leitura')")
    public LancamentoResponse buscar(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) {
        return service.buscar(u.getEmpresaId(), id);
    }
    @PostMapping @PreAuthorize("hasAuthority('financeiro:lancamento:escrita')")
    public ResponseEntity<LancamentoResponse> criar(@AuthenticationPrincipal AuthenticatedUser u,
                                                    @Valid @RequestBody LancamentoRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(u.getEmpresaId(), r));
    }
    @GetMapping("/{id}/partidas") @PreAuthorize("hasAuthority('financeiro:lancamento:leitura')")
    public List<PartidaResponse> listarPartidas(@AuthenticationPrincipal AuthenticatedUser u,
                                                @PathVariable Long id) {
        return service.listarPartidas(u.getEmpresaId(), id);
    }
    @PutMapping("/{id}") @PreAuthorize("hasAuthority('financeiro:lancamento:escrita')")
    public LancamentoResponse atualizar(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id,
                                        @Valid @RequestBody LancamentoRequest r) {
        return service.atualizar(u.getEmpresaId(), id, r);
    }
    @DeleteMapping("/{id}") @PreAuthorize("hasAuthority('financeiro:lancamento:escrita')")
    public ResponseEntity<Void> excluir(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) {
        service.excluir(u.getEmpresaId(), id);
        return ResponseEntity.noContent().build();
    }
}
