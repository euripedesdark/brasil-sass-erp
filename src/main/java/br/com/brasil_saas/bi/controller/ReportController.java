package br.com.brasil_saas.bi.controller;

import br.com.brasil_saas.bi.dto.ReportRequest;
import br.com.brasil_saas.bi.model.Report;
import br.com.brasil_saas.bi.service.ReportService;
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
import java.util.Map;

@RestController
@RequestMapping("/api/bi/reports")
@RequiredArgsConstructor
@Tag(name = "BI - Relatorios", description = "API de relatrios gerenciais")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN','SUPERUSER')")
    @Operation(summary = "Criar relatrio")
    public ResponseEntity<ApiResponse<Report>> create(
            @RequestBody ReportRequest request,
            @AuthenticationPrincipal AuthenticatedUser u) {
        Report report = reportService.create(request, u.getEmpresaId());
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN','SUPERUSER')")
    @Operation(summary = "Atualizar relatrio")
    public ResponseEntity<ApiResponse<Report>> update(
            @PathVariable Long id,
            @RequestBody ReportRequest request,
            @AuthenticationPrincipal AuthenticatedUser u) {
        Report report = reportService.update(id, request, u.getEmpresaId());
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obter relatrio por ID")
    public ResponseEntity<ApiResponse<Report>> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser u) {
        Report report = reportService.getById(id, u.getEmpresaId());
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar relatrios")
    public ResponseEntity<ApiResponse<PageResponse<Report>>> listAll(
            @AuthenticationPrincipal AuthenticatedUser u,
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<Report> response = reportService.listAll(u.getEmpresaId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar relatrios por categoria")
    public ResponseEntity<ApiResponse<List<Report>>> listByCategory(
            @PathVariable String category,
            @AuthenticationPrincipal AuthenticatedUser u) {
        List<Report> reports = reportService.listByCategory(category, u.getEmpresaId());
        return ResponseEntity.ok(ApiResponse.success(reports));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN','SUPERUSER')")
    @Operation(summary = "Excluir relatrio")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser u) {
        reportService.delete(id, u.getEmpresaId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/generate")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Gerar relatrio")
    public ResponseEntity<byte[]> generateReport(
            @PathVariable Long id,
            @RequestBody Map<String, Object> parameters,
            @RequestParam String outputType,
            @AuthenticationPrincipal AuthenticatedUser u) {
        byte[] reportData = reportService.generateReport(id, parameters, outputType, u.getEmpresaId());
        return ResponseEntity.ok()
                .header("Content-Type", getContentType(outputType))
                .header("Content-Disposition", "attachment; filename=\"report.\"" + getFileExtension(outputType))
                .body(reportData);
    }

    @PostMapping("/{id}/schedule")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN','SUPERUSER')")
    @Operation(summary = "Agendar relatrio")
    public ResponseEntity<ApiResponse<Void>> scheduleReport(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser u) {
        reportService.scheduleReport(id, u.getEmpresaId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/unschedule")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN','SUPERUSER')")
    @Operation(summary = "Desagendar relatrio")
    public ResponseEntity<ApiResponse<Void>> unscheduleReport(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser u) {
        reportService.unscheduleReport(id, u.getEmpresaId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private String getContentType(String outputType) {
        return switch (outputType.toLowerCase()) {
            case "pdf" -> "application/pdf";
            case "excel", "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "csv" -> "text/csv";
            case "html" -> "text/html";
            default -> "application/octet-stream";
        };
    }

    private String getFileExtension(String outputType) {
        return switch (outputType.toLowerCase()) {
            case "excel" -> "xlsx";
            default -> outputType.toLowerCase();
        };
    }
}
