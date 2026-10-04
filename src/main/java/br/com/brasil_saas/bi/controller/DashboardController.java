package br.com.brasil_saas.bi.controller;

import br.com.brasil_saas.bi.dto.DashboardRequest;
import br.com.brasil_saas.bi.model.Dashboard;
import br.com.brasil_saas.bi.service.DashboardService;
import br.com.brasil_saas.shared.dto.ApiResponse;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bi/dashboards")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Dashboard>> criar(
            @AuthenticationPrincipal AuthenticatedUser u,
            @RequestBody DashboardRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(dashboardService.criar(u.getEmpresaId(), request))
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Dashboard>> buscarPorId(
            @AuthenticationPrincipal AuthenticatedUser u,
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(dashboardService.buscarPorId(u.getEmpresaId(), id))
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<Dashboard>>> listarPorEmpresa(
            @AuthenticationPrincipal AuthenticatedUser u) {
        return ResponseEntity.ok(
                ApiResponse.success(dashboardService.listarPorEmpresa(u.getEmpresaId()))
        );
    }

    @GetMapping("/publicos")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<Dashboard>>> listarPublicos(
            @AuthenticationPrincipal AuthenticatedUser u) {
        return ResponseEntity.ok(
                ApiResponse.success(dashboardService.listarPublicos(u.getEmpresaId()))
        );
    }

    @GetMapping("/tipo/{tipo}")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<Dashboard>>> listarPorTipo(
            @AuthenticationPrincipal AuthenticatedUser u,
            @PathVariable String tipo) {
        return ResponseEntity.ok(
                ApiResponse.success(dashboardService.listarPorTipo(u.getEmpresaId(), tipo))
        );
    }

    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<Dashboard>>> listarPorUsuario(
            @AuthenticationPrincipal AuthenticatedUser u,
            @PathVariable Long usuarioId) {
        return ResponseEntity.ok(
                ApiResponse.success(dashboardService.listarPorUsuario(u.getEmpresaId(), usuarioId))
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Dashboard>> atualizar(
            @AuthenticationPrincipal AuthenticatedUser u,
            @PathVariable Long id,
            @RequestBody DashboardRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(dashboardService.atualizar(u.getEmpresaId(), id, request))
        );
    }

    @PostMapping("/{id}/duplicar")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Dashboard>> duplicar(
            @AuthenticationPrincipal AuthenticatedUser u,
            @PathVariable Long id,
            @RequestParam String novoNome) {
        return ResponseEntity.ok(
                ApiResponse.success(dashboardService.duplicar(u.getEmpresaId(), id, novoNome))
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> excluir(
            @AuthenticationPrincipal AuthenticatedUser u,
            @PathVariable Long id) {
        dashboardService.excluir(u.getEmpresaId(), id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
