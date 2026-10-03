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

import java.util.List;

@RestController
@RequestMapping("/api/bi/kpis")
@RequiredArgsConstructor
@Tag(name = "BI - KPIs", description = "API de indicadores de desempenho")
public class KpiController {

    private final KpiService kpiService;

    @PostMapping
    @Operation(summary = "Criar KPI")
    public ResponseEntity<ApiResponse<KpiResponse>> create(
            @RequestBody KpiRequest request,
            @RequestParam Long empresaId) {
        KpiResponse response = kpiService.create(request, empresaId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar KPI")
    public ResponseEntity<ApiResponse<KpiResponse>> update(
            @PathVariable Long id,
            @RequestBody KpiRequest request,
            @RequestParam Long empresaId) {
        KpiResponse response = kpiService.update(id, request, empresaId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter KPI por ID")
    public ResponseEntity<ApiResponse<KpiResponse>> getById(
            @PathVariable Long id,
            @RequestParam Long empresaId) {
        KpiResponse response = kpiService.getById(id, empresaId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Listar KPIs")
    public ResponseEntity<ApiResponse<PageResponse<KpiResponse>>> listAll(
            @RequestParam Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<KpiResponse> response = kpiService.listAll(empresaId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/type/{kpiType}")
    @Operation(summary = "Listar KPIs por tipo")
    public ResponseEntity<ApiResponse<List<KpiResponse>>> listByType(
            @PathVariable String kpiType,
            @RequestParam Long empresaId) {
        List<KpiResponse> response = kpiService.listByType(kpiType, empresaId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir KPI")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @RequestParam Long empresaId) {
        kpiService.delete(id, empresaId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Atualizar valores de todos os KPIs")
    public ResponseEntity<ApiResponse<Void>> refreshAll(@RequestParam Long empresaId) {
        kpiService.refreshKpiValues(empresaId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{id}/calculate")
    @Operation(summary = "Calcular valor de um KPI")
    public ResponseEntity<ApiResponse<Object>> calculateValue(
            @PathVariable Long id,
            @RequestParam Long empresaId) {
        Object value = kpiService.calculateKpiValue(id, empresaId);
        return ResponseEntity.ok(ApiResponse.success(value));
    }
}
