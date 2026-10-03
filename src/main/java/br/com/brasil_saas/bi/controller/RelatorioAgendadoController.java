package br.com.brasil_saas.bi.controller;

import br.com.brasil_saas.bi.dto.RelatorioAgendadoRequest;
import br.com.brasil_saas.bi.model.RelatorioAgendado;
import br.com.brasil_saas.bi.service.RelatorioAgendadoService;
import br.com.brasil_saas.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/bi/relatorios-agendados")
@RequiredArgsConstructor
public class RelatorioAgendadoController {

    private final RelatorioAgendadoService relatorioAgendadoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<RelatorioAgendado>> criar(
            @RequestParam Long empresaId,
            @RequestBody RelatorioAgendadoRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(relatorioAgendadoService.criar(empresaId, request))
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<RelatorioAgendado>> buscarPorId(
            @RequestParam Long empresaId,
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(relatorioAgendadoService.buscarPorId(empresaId, id))
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<RelatorioAgendado>>> listarPorEmpresa(
            @RequestParam Long empresaId) {
        return ResponseEntity.ok(
                ApiResponse.success(relatorioAgendadoService.listarPorEmpresa(empresaId))
        );
    }

    @GetMapping("/pendentes")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<RelatorioAgendado>>> listarPendentes(
            @RequestParam Long empresaId) {
        return ResponseEntity.ok(
                ApiResponse.success(relatorioAgendadoService.listarPendentes(empresaId))
        );
    }

    @GetMapping("/frequencia/{frequencia}")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<RelatorioAgendado>>> listarPorFrequencia(
            @RequestParam Long empresaId,
            @PathVariable String frequencia) {
        return ResponseEntity.ok(
                ApiResponse.success(relatorioAgendadoService.listarPorFrequencia(empresaId, frequencia))
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<RelatorioAgendado>> atualizar(
            @RequestParam Long empresaId,
            @PathVariable Long id,
            @RequestBody RelatorioAgendadoRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(relatorioAgendadoService.atualizar(empresaId, id, request))
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> excluir(
            @RequestParam Long empresaId,
            @PathVariable Long id) {
        relatorioAgendadoService.excluir(empresaId, id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/executar")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> executar(
            @RequestParam Long empresaId,
            @PathVariable Long id) {
        relatorioAgendadoService.executarAgendado(empresaId, id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/agendar")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> agendarProximaExecucao(
            @RequestParam Long empresaId,
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime proximaExecucao) {
        relatorioAgendadoService.definirProximaExecucao(empresaId, id, proximaExecucao);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/executar-todos")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> executarTodosPendentes(
            @RequestParam Long empresaId) {
        relatorioAgendadoService.executarPendentes();
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
