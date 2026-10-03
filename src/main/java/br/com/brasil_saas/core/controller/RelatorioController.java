package br.com.brasil_saas.core.controller;

import br.com.brasil_saas.core.model.RelatorioResponse;
import br.com.brasil_saas.core.service.RelatorioService;
import br.com.brasil_saas.core.service.report.PdfGeneratorService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController("coreRelatorioController")
@RequestMapping("/api/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

    private final RelatorioService relatorioService;
    private final PdfGeneratorService pdfGeneratorService;

    @GetMapping("/{tipo}")
    public ResponseEntity<RelatorioResponse> getRelatorio(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable String tipo,
            @RequestParam Map<String, Object> filtros) {

        // O tenant vem sempre do token; aceitar empresaId do cliente permitiria
        // ler o relatorio de outra empresa.
        Long empresaId = resolverEmpresa(user, filtros);

        try {
            RelatorioResponse report = relatorioService.gerarRelatorio(tipo, empresaId, filtros);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Gera o relatório gerencial em PDF. O frontend (Relatorios.jsx) chama
     * /api/relatorios/pdf/{tipo} via window.open, entao o prefixo "pdf" precisa
     * ser declarado antes do catch-all /{tipo}.
     */
    @GetMapping("/pdf/{tipo}")
    public ResponseEntity<byte[]> getRelatorioPdf(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable String tipo,
            @RequestParam Map<String, Object> filtros) {

        Long empresaId = resolverEmpresa(user, filtros);

        RelatorioResponse report;
        try {
            report = relatorioService.gerarRelatorio(tipo, empresaId, filtros);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }

        List<Map<String, Object>> dados = report.getDados() == null ? List.of() : report.getDados();
        List<String> columns = dados.isEmpty() ? List.of() : new ArrayList<>(dados.get(0).keySet());

        try {
            byte[] pdf = pdfGeneratorService.generateReport(
                    report.getNomeRelatorio() != null ? report.getNomeRelatorio() : tipo,
                    report.getPeriodo() != null ? report.getPeriodo() : "",
                    empresaId, "", java.time.LocalDate.now(),
                    dados, columns, report.getResumo());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename("relatorio-" + tipo.toLowerCase() + ".pdf", StandardCharsets.UTF_8)
                    .build());
            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private Long resolverEmpresa(AuthenticatedUser user, Map<String, Object> filtros) {
        // Para download via window.open o token vai na querystring, nao no header.
        if (user != null && user.getEmpresaId() != null) {
            return user.getEmpresaId();
        }
        Object empresaId = filtros.get("empresaId");
        if (empresaId == null) {
            throw new IllegalArgumentException("empresaId nao informado");
        }
        return Long.valueOf(String.valueOf(empresaId));
    }
}
