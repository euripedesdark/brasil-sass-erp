package br.com.brasil_saas.bi.controller;

import br.com.brasil_saas.bi.dto.DashboardRequest;
import br.com.brasil_saas.bi.model.Dashboard;
import br.com.brasil_saas.bi.service.DashboardService;
import br.com.brasil_saas.shared.dto.ApiResponse;
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
            @RequestParam Long empresaId,
            @RequestBody DashboardRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(dashboardService.criar(empresaId, request))
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Dashboard>> buscarPorId(
            @RequestParam Long empresaId,
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(dashboardService.buscarPorId(empresaId, id))
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<Dashboard>>> listarPorEmpresa(
            @RequestParam Long empresaId) {
        return ResponseEntity.ok(
                ApiResponse.success(dashboardService.listarPorEmpresa(empresaId))
        );
    }

    @GetMapping("/publicos")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<Dashboard>>> listarPublicos(
            @RequestParam Long empresaId) {
        return ResponseEntity.ok(
                ApiResponse.success(dashboardService.listarPublicos(empresaId))
        );
    }

    @GetMapping("/tipo/{tipo}")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<Dashboard>>> listarPorTipo(
            @RequestParam Long empresaId,
            @PathVariable String tipo) {
        return ResponseEntity.ok(
                ApiResponse.success(dashboardService.listarPorTipo(empresaId, tipo))
        );
    }

    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<Dashboard>>> listarPorUsuario(
            @RequestParam Long empresaId,
            @PathVariable Long usuarioId) {
        return ResponseEntity.ok(
                ApiResponse.success(dashboardService.listarPorUsuario(empresaId, usuarioId))
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Dashboard>> atualizar(
            @RequestParam Long empresaId,
            @PathVariable Long id,
            @RequestBody DashboardRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(dashboardService.atualizar(empresaId, id, request))
        );
    }

    @PostMapping("/{id}/duplicar")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Dashboard>> duplicar(
            @RequestParam Long empresaId,
            @PathVariable Long id,
            @RequestParam String novoNome) {
        return ResponseEntity.ok(
                ApiResponse.success(dashboardService.duplicar(empresaId, id, novoNome))
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> excluir(
            @RequestParam Long empresaId,
            @PathVariable Long id) {
        dashboardService.excluir(empresaId, id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
