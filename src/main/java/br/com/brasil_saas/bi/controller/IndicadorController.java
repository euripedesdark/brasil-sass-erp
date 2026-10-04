package br.com.brasil_saas.bi.controller;

import br.com.brasil_saas.bi.dto.IndicadorRequest;
import br.com.brasil_saas.bi.model.Indicador;
import br.com.brasil_saas.bi.service.IndicadorService;
import br.com.brasil_saas.shared.dto.ApiResponse;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bi/indicadores")
@RequiredArgsConstructor
public class IndicadorController {

    private final IndicadorService indicadorService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Indicador>> criar(
            @AuthenticationPrincipal AuthenticatedUser u,
            @RequestBody IndicadorRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(indicadorService.criar(u.getEmpresaId(), request))
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Indicador>> buscarPorId(
            @AuthenticationPrincipal AuthenticatedUser u,
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(indicadorService.buscarPorId(u.getEmpresaId(), id))
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<Indicador>>> listarPorEmpresa(
            @AuthenticationPrincipal AuthenticatedUser u) {
        return ResponseEntity.ok(
                ApiResponse.success(indicadorService.listarPorEmpresa(u.getEmpresaId()))
        );
    }

    @GetMapping("/categoria/{categoria}")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<Indicador>>> listarPorCategoria(
            @AuthenticationPrincipal AuthenticatedUser u,
            @PathVariable String categoria) {
        return ResponseEntity.ok(
                ApiResponse.success(indicadorService.listarPorCategoria(u.getEmpresaId(), categoria))
        );
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<Indicador>>> listarVisiveisDashboard(
            @AuthenticationPrincipal AuthenticatedUser u) {
        return ResponseEntity.ok(
                ApiResponse.success(indicadorService.listarVisiveisDashboard(u.getEmpresaId()))
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Indicador>> atualizar(
            @AuthenticationPrincipal AuthenticatedUser u,
            @PathVariable Long id,
            @RequestBody IndicadorRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(indicadorService.atualizar(u.getEmpresaId(), id, request))
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> excluir(
            @AuthenticationPrincipal AuthenticatedUser u,
            @PathVariable Long id) {
        indicadorService.excluir(u.getEmpresaId(), id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/calcular/categoria/{categoria}")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> calcularPorCategoria(
            @AuthenticationPrincipal AuthenticatedUser u,
            @PathVariable String categoria) {
        return ResponseEntity.ok(
                ApiResponse.success(indicadorService.calcularIndicadores(u.getEmpresaId(), categoria))
        );
    }

    @PostMapping("/calcular/todos")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> calcularTodos(
            @AuthenticationPrincipal AuthenticatedUser u) {
        return ResponseEntity.ok(
                ApiResponse.success(indicadorService.calcularTodosIndicadores(u.getEmpresaId()))
        );
    }

    @PostMapping("/atualizar/valores")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> atualizarValores(
            @AuthenticationPrincipal AuthenticatedUser u) {
        indicadorService.atualizarValores(u.getEmpresaId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/atualizar")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> atualizarValor(
            @AuthenticationPrincipal AuthenticatedUser u,
            @PathVariable Long id) {
        indicadorService.atualizarValor(u.getEmpresaId(), id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
