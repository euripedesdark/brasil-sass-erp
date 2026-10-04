package br.com.brasil_saas.rh.controller;

import br.com.brasil_saas.rh.dto.FolhaPagamentoRequest;
import br.com.brasil_saas.rh.dto.FolhaPagamentoResponse;
import br.com.brasil_saas.rh.service.FolhaPagamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rh/folhas")
@RequiredArgsConstructor
public class FolhaPagamentoController {

    private final FolhaPagamentoService service;
    private final br.com.brasil_saas.rh.service.DecimoTerceiroService decimo;

    @PostMapping
    @PreAuthorize("hasAuthority('rh:folha:escrita')")
    public ResponseEntity<FolhaPagamentoResponse> criar(@Valid @RequestBody FolhaPagamentoRequest request,
                                                         @AuthenticationPrincipal AuthenticatedUser u) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(u.getEmpresaId(), request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('rh:folha:leitura')")
    public ResponseEntity<FolhaPagamentoResponse> buscarPorId(@PathVariable Long id,
                                                              @AuthenticationPrincipal AuthenticatedUser u) {
        return ResponseEntity.ok(service.buscarPorId(u.getEmpresaId(), id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('rh:folha:leitura')")
    public ResponseEntity<List<FolhaPagamentoResponse>> listarPorEmpresa(@AuthenticationPrincipal AuthenticatedUser u) {
        return ResponseEntity.ok(service.listarPorEmpresa(u.getEmpresaId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('rh:folha:escrita')")
    public ResponseEntity<FolhaPagamentoResponse> atualizar(@PathVariable Long id,
                                                            @Valid @RequestBody FolhaPagamentoRequest request,
                                                            @AuthenticationPrincipal AuthenticatedUser u) {
        return ResponseEntity.ok(service.atualizar(u.getEmpresaId(), id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('rh:folha:escrita')")
    public ResponseEntity<Void> excluir(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser u) {
        service.excluir(u.getEmpresaId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/processar")
    @PreAuthorize("hasAuthority('rh:folha:escrita')")
    public ResponseEntity<Void> processar(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser u) {
        service.processar(u.getEmpresaId(), id);
        return ResponseEntity.ok().build();
    }

    public record ImportarPontoReq(Long funcionarioId, Integer ano, Integer mes) {}
    @PostMapping("/gerar-decimo")
    @PreAuthorize("hasAuthority('rh:folha:escrita')")
    public ResponseEntity<?> gerarDecimo(@AuthenticationPrincipal AuthenticatedUser u, @RequestParam int ano, @RequestParam int parcela) { return ResponseEntity.ok(decimo.gerar(u.getEmpresaId(), ano, parcela)); }

    @PostMapping("/{id}/importar-ponto")
    @PreAuthorize("hasAuthority('rh:folha:escrita')")
    public ResponseEntity<?> importarPonto(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id, @RequestBody ImportarPontoReq r) {
        return ResponseEntity.ok(service.importarPonto(u.getEmpresaId(), id, r.funcionarioId(), r.ano(), r.mes()));
    }
    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAuthority('rh:folha:escrita')")
    public ResponseEntity<Void> cancelar(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser u) {
        service.cancelar(u.getEmpresaId(), id);
        return ResponseEntity.ok().build();
    }
}
