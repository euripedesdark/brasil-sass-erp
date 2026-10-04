package br.com.brasil_saas.bi.controller;

import br.com.brasil_saas.bi.dto.RelatorioRequest;
import br.com.brasil_saas.bi.model.Relatorio;
import br.com.brasil_saas.bi.service.RelatorioService;
import br.com.brasil_saas.shared.dto.ApiResponse;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @AuthenticationPrincipal AuthenticatedUser u,
            @RequestBody RelatorioRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(relatorioService.criar(u.getEmpresaId(), request))
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Relatorio>> buscarPorId(
            @AuthenticationPrincipal AuthenticatedUser u,
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(relatorioService.buscarPorId(u.getEmpresaId(), id))
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<Relatorio>>> listarPorEmpresa(
            @AuthenticationPrincipal AuthenticatedUser u) {
        return ResponseEntity.ok(
                ApiResponse.success(relatorioService.listarPorEmpresa(u.getEmpresaId()))
        );
    }

    @GetMapping("/categoria/{categoria}")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<Relatorio>>> listarPorCategoria(
            @AuthenticationPrincipal AuthenticatedUser u,
            @PathVariable String categoria) {
        return ResponseEntity.ok(
                ApiResponse.success(relatorioService.listarPorCategoria(u.getEmpresaId(), categoria))
        );
    }

    @GetMapping("/agendados")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<Relatorio>>> listarAgendados(
            @AuthenticationPrincipal AuthenticatedUser u) {
        return ResponseEntity.ok(
                ApiResponse.success(relatorioService.listarAgendados(u.getEmpresaId()))
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Relatorio>> atualizar(
            @AuthenticationPrincipal AuthenticatedUser u,
            @PathVariable Long id,
            @RequestBody RelatorioRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(relatorioService.atualizar(u.getEmpresaId(), id, request))
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> excluir(
            @AuthenticationPrincipal AuthenticatedUser u,
            @PathVariable Long id) {
        relatorioService.excluir(u.getEmpresaId(), id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/executar")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> executar(
            @AuthenticationPrincipal AuthenticatedUser u,
            @PathVariable Long id,
            @RequestBody Map<String, Object> parametros) {
        return ResponseEntity.ok(
                ApiResponse.success(relatorioService.executarRelatorio(u.getEmpresaId(), id, parametros))
        );
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<byte[]> exportarPdf(
            @AuthenticationPrincipal AuthenticatedUser u,
            @PathVariable Long id,
            @RequestParam(required = false) Map<String, Object> parametros) {
        ByteArrayOutputStream output = relatorioService.gerarPdf(u.getEmpresaId(), id, parametros);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("relatorio.pdf", "relatorio.pdf");
        headers.setContentLength(output.size());
        
        return ResponseEntity.ok().headers(headers).body(output.toByteArray());
    }

    @GetMapping("/{id}/excel")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<byte[]> exportarExcel(
            @AuthenticationPrincipal AuthenticatedUser u,
            @PathVariable Long id,
            @RequestParam(required = false) Map<String, Object> parametros) {
        ByteArrayOutputStream output = relatorioService.gerarExcel(u.getEmpresaId(), id, parametros);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("relatorio.xlsx", "relatorio.xlsx");
        headers.setContentLength(output.size());
        
        return ResponseEntity.ok().headers(headers).body(output.toByteArray());
    }

    @GetMapping("/{id}/csv")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<byte[]> exportarCsv(
            @AuthenticationPrincipal AuthenticatedUser u,
            @PathVariable Long id,
            @RequestParam(required = false) Map<String, Object> parametros) {
        ByteArrayOutputStream output = relatorioService.gerarCsv(u.getEmpresaId(), id, parametros);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("relatorio.csv", "relatorio.csv");
        headers.setContentLength(output.size());
        
        return ResponseEntity.ok().headers(headers).body(output.toByteArray());
    }
}
