package br.com.brasil_saas.bi.controller;

import br.com.brasil_saas.bi.dto.RelatorioRequest;
import br.com.brasil_saas.bi.model.Relatorio;
import br.com.brasil_saas.bi.service.RelatorioService;
import br.com.brasil_saas.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bi/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

    private final RelatorioService relatorioService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Relatorio>> criar(
            @RequestParam Long empresaId,
            @RequestBody RelatorioRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(relatorioService.criar(empresaId, request))
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Relatorio>> buscarPorId(
            @RequestParam Long empresaId,
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(relatorioService.buscarPorId(empresaId, id))
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<Relatorio>>> listarPorEmpresa(
            @RequestParam Long empresaId) {
        return ResponseEntity.ok(
                ApiResponse.success(relatorioService.listarPorEmpresa(empresaId))
        );
    }

    @GetMapping("/categoria/{categoria}")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<Relatorio>>> listarPorCategoria(
            @RequestParam Long empresaId,
            @PathVariable String categoria) {
        return ResponseEntity.ok(
                ApiResponse.success(relatorioService.listarPorCategoria(empresaId, categoria))
        );
    }

    @GetMapping("/agendados")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<Relatorio>>> listarAgendados(
            @RequestParam Long empresaId) {
        return ResponseEntity.ok(
                ApiResponse.success(relatorioService.listarAgendados(empresaId))
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Relatorio>> atualizar(
            @RequestParam Long empresaId,
            @PathVariable Long id,
            @RequestBody RelatorioRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(relatorioService.atualizar(empresaId, id, request))
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> excluir(
            @RequestParam Long empresaId,
            @PathVariable Long id) {
        relatorioService.excluir(empresaId, id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/executar")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> executar(
            @RequestParam Long empresaId,
            @PathVariable Long id,
            @RequestBody Map<String, Object> parametros) {
        return ResponseEntity.ok(
                ApiResponse.success(relatorioService.executarRelatorio(empresaId, id, parametros))
        );
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<byte[]> exportarPdf(
            @RequestParam Long empresaId,
            @PathVariable Long id,
            @RequestParam(required = false) Map<String, Object> parametros) {
        ByteArrayOutputStream output = relatorioService.gerarPdf(empresaId, id, parametros);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("relatorio.pdf", "relatorio.pdf");
        headers.setContentLength(output.size());
        
        return ResponseEntity.ok().headers(headers).body(output.toByteArray());
    }

    @GetMapping("/{id}/excel")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam Long empresaId,
            @PathVariable Long id,
            @RequestParam(required = false) Map<String, Object> parametros) {
        ByteArrayOutputStream output = relatorioService.gerarExcel(empresaId, id, parametros);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("relatorio.xlsx", "relatorio.xlsx");
        headers.setContentLength(output.size());
        
        return ResponseEntity.ok().headers(headers).body(output.toByteArray());
    }

    @GetMapping("/{id}/csv")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<byte[]> exportarCsv(
            @RequestParam Long empresaId,
            @PathVariable Long id,
            @RequestParam(required = false) Map<String, Object> parametros) {
        ByteArrayOutputStream output = relatorioService.gerarCsv(empresaId, id, parametros);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("relatorio.csv", "relatorio.csv");
        headers.setContentLength(output.size());
        
        return ResponseEntity.ok().headers(headers).body(output.toByteArray());
    }
}
