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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bi/reports")
@RequiredArgsConstructor
@Tag(name = "BI - Relatorios", description = "API de relatrios gerenciais")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @Operation(summary = "Criar relatrio")
    public ResponseEntity<ApiResponse<Report>> create(
            @RequestBody ReportRequest request,
            @RequestParam Long empresaId) {
        Report report = reportService.create(request, empresaId);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar relatrio")
    public ResponseEntity<ApiResponse<Report>> update(
            @PathVariable Long id,
            @RequestBody ReportRequest request,
            @RequestParam Long empresaId) {
        Report report = reportService.update(id, request, empresaId);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter relatrio por ID")
    public ResponseEntity<ApiResponse<Report>> getById(
            @PathVariable Long id,
            @RequestParam Long empresaId) {
        Report report = reportService.getById(id, empresaId);
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping
    @Operation(summary = "Listar relatrios")
    public ResponseEntity<ApiResponse<PageResponse<Report>>> listAll(
            @RequestParam Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<Report> response = reportService.listAll(empresaId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Listar relatrios por categoria")
    public ResponseEntity<ApiResponse<List<Report>>> listByCategory(
            @PathVariable String category,
            @RequestParam Long empresaId) {
        List<Report> reports = reportService.listByCategory(category, empresaId);
        return ResponseEntity.ok(ApiResponse.success(reports));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir relatrio")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @RequestParam Long empresaId) {
        reportService.delete(id, empresaId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/generate")
    @Operation(summary = "Gerar relatrio")
    public ResponseEntity<byte[]> generateReport(
            @PathVariable Long id,
            @RequestBody Map<String, Object> parameters,
            @RequestParam String outputType,
            @RequestParam Long empresaId) {
        byte[] reportData = reportService.generateReport(id, parameters, outputType, empresaId);
        return ResponseEntity.ok()
                .header("Content-Type", getContentType(outputType))
                .header("Content-Disposition", "attachment; filename=\"report.\"" + getFileExtension(outputType))
                .body(reportData);
    }

    @PostMapping("/{id}/schedule")
    @Operation(summary = "Agendar relatrio")
    public ResponseEntity<ApiResponse<Void>> scheduleReport(
            @PathVariable Long id,
            @RequestParam Long empresaId) {
        reportService.scheduleReport(id, empresaId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/unschedule")
    @Operation(summary = "Desagendar relatrio")
    public ResponseEntity<ApiResponse<Void>> unscheduleReport(
            @PathVariable Long id,
            @RequestParam Long empresaId) {
        reportService.unscheduleReport(id, empresaId);
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
