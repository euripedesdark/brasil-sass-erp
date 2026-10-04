package br.com.brasil_saas.bi.controller;

import br.com.brasil_saas.bi.dto.RelatorioAgendadoRequest;
import br.com.brasil_saas.bi.model.RelatorioAgendado;
import br.com.brasil_saas.bi.service.RelatorioAgendadoService;
import br.com.brasil_saas.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
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
            @AuthenticationPrincipal AuthenticatedUser u,
            @RequestBody RelatorioAgendadoRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(relatorioAgendadoService.criar(u.getEmpresaId(), request))
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<RelatorioAgendado>> buscarPorId(
            @AuthenticationPrincipal AuthenticatedUser u,
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(relatorioAgendadoService.buscarPorId(u.getEmpresaId(), id))
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<RelatorioAgendado>>> listarPorEmpresa(
            @AuthenticationPrincipal AuthenticatedUser u) {
        return ResponseEntity.ok(
                ApiResponse.success(relatorioAgendadoService.listarPorEmpresa(u.getEmpresaId()))
        );
    }

    @GetMapping("/pendentes")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<RelatorioAgendado>>> listarPendentes(
            @AuthenticationPrincipal AuthenticatedUser u) {
        return ResponseEntity.ok(
                ApiResponse.success(relatorioAgendadoService.listarPendentes(u.getEmpresaId()))
        );
    }

    @GetMapping("/frequencia/{frequencia}")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<RelatorioAgendado>>> listarPorFrequencia(
            @AuthenticationPrincipal AuthenticatedUser u,
            @PathVariable String frequencia) {
        return ResponseEntity.ok(
                ApiResponse.success(relatorioAgendadoService.listarPorFrequencia(u.getEmpresaId(), frequencia))
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<RelatorioAgendado>> atualizar(
            @AuthenticationPrincipal AuthenticatedUser u,
            @PathVariable Long id,
            @RequestBody RelatorioAgendadoRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(relatorioAgendadoService.atualizar(u.getEmpresaId(), id, request))
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> excluir(
            @AuthenticationPrincipal AuthenticatedUser u,
            @PathVariable Long id) {
        relatorioAgendadoService.excluir(u.getEmpresaId(), id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/executar")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> executar(
            @AuthenticationPrincipal AuthenticatedUser u,
            @PathVariable Long id) {
        relatorioAgendadoService.executarAgendado(u.getEmpresaId(), id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/agendar")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> agendarProximaExecucao(
            @AuthenticationPrincipal AuthenticatedUser u,
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime proximaExecucao) {
        relatorioAgendadoService.definirProximaExecucao(u.getEmpresaId(), id, proximaExecucao);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/executar-todos")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> executarTodosPendentes(
            @AuthenticationPrincipal AuthenticatedUser u) {
        relatorioAgendadoService.executarPendentes();
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
