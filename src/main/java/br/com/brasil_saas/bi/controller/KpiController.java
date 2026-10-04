package br.com.brasil_saas.bi.controller;

import br.com.brasil_saas.bi.dto.KpiRequest;
import br.com.brasil_saas.bi.dto.KpiResponse;
import br.com.brasil_saas.bi.service.KpiService;
import br.com.brasil_saas.shared.web.ApiResponse;
import br.com.brasil_saas.shared.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import br.com.brasil_saas.shared.security.AuthenticatedUser;

import java.util.List;

@RestController
@RequestMapping("/api/bi/kpis")
@RequiredArgsConstructor
@Tag(name = "BI - KPIs", description = "API de indicadores de desempenho")
public class KpiController {

    private final KpiService kpiService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN','SUPERUSER')")
    @Operation(summary = "Criar KPI")
    public ResponseEntity<ApiResponse<KpiResponse>> create(
            @AuthenticationPrincipal AuthenticatedUser u,
            @RequestBody KpiRequest request) {
        KpiResponse response = kpiService.create(request, u.getEmpresaId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN','SUPERUSER')")
    @Operation(summary = "Atualizar KPI")
    public ResponseEntity<ApiResponse<KpiResponse>> update(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser u,
            @RequestBody KpiRequest request) {
        KpiResponse response = kpiService.update(id, request, u.getEmpresaId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obter KPI por ID")
    public ResponseEntity<ApiResponse<KpiResponse>> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser u) {
        KpiResponse response = kpiService.getById(id, u.getEmpresaId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar KPIs")
    public ResponseEntity<ApiResponse<PageResponse<KpiResponse>>> listAll(
            @AuthenticationPrincipal AuthenticatedUser u,
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<KpiResponse> response = kpiService.listAll(u.getEmpresaId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/type/{kpiType}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar KPIs por tipo")
    public ResponseEntity<ApiResponse<List<KpiResponse>>> listByType(
            @PathVariable String kpiType,
            @AuthenticationPrincipal AuthenticatedUser u) {
        List<KpiResponse> response = kpiService.listByType(kpiType, u.getEmpresaId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN','SUPERUSER')")
    @Operation(summary = "Excluir KPI")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser u) {
        kpiService.delete(id, u.getEmpresaId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/refresh")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN','SUPERUSER')")
    @Operation(summary = "Atualizar valores de todos os KPIs")
    public ResponseEntity<ApiResponse<Void>> refreshAll(@RequestParam Long empresaId) {
        kpiService.refreshKpiValues(u.getEmpresaId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{id}/calculate")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Calcular valor de um KPI")
    public ResponseEntity<ApiResponse<Object>> calculateValue(
            @PathVariable Long id,
            @RequestParam Long empresaId) {
        Object value = kpiService.calculateKpiValue(id, u.getEmpresaId());
        return ResponseEntity.ok(ApiResponse.success(value));
    }
}
