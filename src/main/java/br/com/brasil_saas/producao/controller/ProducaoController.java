package br.com.brasil_saas.producao.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import br.com.brasil_saas.producao.model.Producao;
import br.com.brasil_saas.producao.service.ProducaoService;
import br.com.brasil_saas.producao.service.ProducaoRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/producao")
@RequiredArgsConstructor
public class ProducaoController {

    private final ProducaoService producaoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<Producao> criar(@AuthenticationPrincipal AuthenticatedUser u, @RequestBody ProducaoRequest request) {
        return ResponseEntity.ok(producaoService.criarOrdem(u.getEmpresaId(), request));
    }

    @PostMapping("/{id}/finalizar")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<Producao> finalizar(@AuthenticationPrincipal AuthenticatedUser u, @PathVariable Long id) {
        return ResponseEntity.ok(producaoService.finalizarProducao(u.getEmpresaId(), id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<List<Producao>> listar(@AuthenticationPrincipal AuthenticatedUser u) {
        return ResponseEntity.ok(producaoService.listar(u.getEmpresaId()));
    }
}
