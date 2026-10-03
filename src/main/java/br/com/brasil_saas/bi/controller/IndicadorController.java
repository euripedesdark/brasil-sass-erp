package br.com.brasil_saas.bi.controller;

import br.com.brasil_saas.bi.dto.IndicadorRequest;
import br.com.brasil_saas.bi.model.Indicador;
import br.com.brasil_saas.bi.service.IndicadorService;
import br.com.brasil_saas.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bi/indicadores")
@RequiredArgsConstructor
public class IndicadorController {

    private final IndicadorService indicadorService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Indicador>> criar(
            @RequestParam Long empresaId,
            @RequestBody IndicadorRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(indicadorService.criar(empresaId, request))
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Indicador>> buscarPorId(
            @RequestParam Long empresaId,
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(indicadorService.buscarPorId(empresaId, id))
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<Indicador>>> listarPorEmpresa(
            @RequestParam Long empresaId) {
        return ResponseEntity.ok(
                ApiResponse.success(indicadorService.listarPorEmpresa(empresaId))
        );
    }

    @GetMapping("/categoria/{categoria}")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<Indicador>>> listarPorCategoria(
            @RequestParam Long empresaId,
            @PathVariable String categoria) {
        return ResponseEntity.ok(
                ApiResponse.success(indicadorService.listarPorCategoria(empresaId, categoria))
        );
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<Indicador>>> listarVisiveisDashboard(
            @RequestParam Long empresaId) {
        return ResponseEntity.ok(
                ApiResponse.success(indicadorService.listarVisiveisDashboard(empresaId))
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Indicador>> atualizar(
            @RequestParam Long empresaId,
            @PathVariable Long id,
            @RequestBody IndicadorRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(indicadorService.atualizar(empresaId, id, request))
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> excluir(
            @RequestParam Long empresaId,
            @PathVariable Long id) {
        indicadorService.excluir(empresaId, id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/calcular/categoria/{categoria}")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> calcularPorCategoria(
            @RequestParam Long empresaId,
            @PathVariable String categoria) {
        return ResponseEntity.ok(
                ApiResponse.success(indicadorService.calcularIndicadores(empresaId, categoria))
        );
    }

    @PostMapping("/calcular/todos")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> calcularTodos(
            @RequestParam Long empresaId) {
        return ResponseEntity.ok(
                ApiResponse.success(indicadorService.calcularTodosIndicadores(empresaId))
        );
    }

    @PostMapping("/atualizar/valores")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> atualizarValores(
            @RequestParam Long empresaId) {
        indicadorService.atualizarValores(empresaId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/atualizar")
    @PreAuthorize("hasAnyRole('SUPERUSER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> atualizarValor(
            @RequestParam Long empresaId,
            @PathVariable Long id) {
        indicadorService.atualizarValor(empresaId, id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
