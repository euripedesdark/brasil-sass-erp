package br.com.brasil_saas.producao.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import br.com.brasil_saas.producao.model.RomaneioProducao;
import br.com.brasil_saas.producao.service.RomaneioProducaoRequest;
import br.com.brasil_saas.producao.service.RomaneioProducaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/producao/romaneios")
@RequiredArgsConstructor
public class RomaneioProducaoController {

    private final RomaneioProducaoService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<RomaneioProducao> criar(
        @AuthenticationPrincipal AuthenticatedUser u,
        @RequestBody RomaneioProducaoRequest request
    ) {
        return ResponseEntity.ok(service.criar(u.getEmpresaId(), request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<List<RomaneioProducao>> listar(@AuthenticationPrincipal AuthenticatedUser u) {
        return ResponseEntity.ok(service.listar(u.getEmpresaId()));
    }
}
