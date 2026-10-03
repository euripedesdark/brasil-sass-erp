package br.com.brasil_saas.producao.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import br.com.brasil_saas.producao.model.ApontamentoProducao;
import br.com.brasil_saas.producao.service.ApontamentoProducaoService;
import br.com.brasil_saas.producao.service.ApontamentoProducaoRequest;
import br.com.brasil_saas.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/producao/apontamentos")
@RequiredArgsConstructor
public class ApontamentoProducaoController {

    private final ApontamentoProducaoService apontamentoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<ApontamentoProducao>> criar(
            @AuthenticationPrincipal AuthenticatedUser u,
            @RequestBody ApontamentoProducaoRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(apontamentoService.criar(u.getEmpresaId(), request))
        );
    }

    @GetMapping("/por-producao/{producaoId}")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<ApontamentoProducao>>> listarPorProducao(
            @AuthenticationPrincipal AuthenticatedUser u,
            @PathVariable Long producaoId) {
        return ResponseEntity.ok(
                ApiResponse.success(apontamentoService.listarPorProducao(u.getEmpresaId(), producaoId))
        );
    }

    @GetMapping("/por-funcionario/{funcionarioId}")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<ApontamentoProducao>>> listarPorFuncionario(
            @AuthenticationPrincipal AuthenticatedUser u,
            @PathVariable Long funcionarioId) {
        return ResponseEntity.ok(
                ApiResponse.success(apontamentoService.listarPorFuncionario(u.getEmpresaId(), funcionarioId))
        );
    }

    @GetMapping("/por-periodo")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<ApontamentoProducao>>> listarPorPeriodo(
            @AuthenticationPrincipal AuthenticatedUser u,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim) {
        return ResponseEntity.ok(
                ApiResponse.success(apontamentoService.listarPorPeriodo(u.getEmpresaId(), dataInicio, dataFim))
        );
    }

    @GetMapping("/por-status")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<ApontamentoProducao>>> listarPorStatus(
            @AuthenticationPrincipal AuthenticatedUser u,
            @RequestParam String status) {
        return ResponseEntity.ok(
                ApiResponse.success(apontamentoService.listarPorStatus(u.getEmpresaId(), status))
        );
    }

    @PutMapping("/{id}/finalizar")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<ApontamentoProducao>> finalizar(
            @AuthenticationPrincipal AuthenticatedUser u,
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(apontamentoService.finalizar(u.getEmpresaId(), id))
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> excluir(
            @AuthenticationPrincipal AuthenticatedUser u,
            @PathVariable Long id) {
        apontamentoService.excluir(u.getEmpresaId(), id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/estatisticas/{producaoId}")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Object>> getEstatisticas(
            @AuthenticationPrincipal AuthenticatedUser u,
            @PathVariable Long producaoId) {
        return ResponseEntity.ok(
                ApiResponse.success(apontamentoService.getEstatisticas(u.getEmpresaId(), producaoId))
        );
    }
}
