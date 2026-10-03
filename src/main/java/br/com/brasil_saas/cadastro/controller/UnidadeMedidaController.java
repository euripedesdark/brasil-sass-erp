package br.com.brasil_saas.cadastro.controller;

import br.com.brasil_saas.cadastro.dto.UnidadeMedidaRequest;
import br.com.brasil_saas.cadastro.dto.UnidadeMedidaResponse;
import br.com.brasil_saas.cadastro.service.UnidadeMedidaService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import br.com.brasil_saas.shared.tenant.EmpresaDaGravacao;
import br.com.brasil_saas.shared.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/cadastro/unidades-medida")
@RequiredArgsConstructor
@Tag(name = "Cadastro - Unidades de Medida")
public class UnidadeMedidaController {

    private final UnidadeMedidaService unidadeMedidaService;

    @PostMapping
    @PreAuthorize("hasAuthority('cadastro:unidade:escrita')")
    @Operation(summary = "Criar unidade de medida")
    public ResponseEntity<ApiResponse<UnidadeMedidaResponse>> criar(@Valid @RequestBody UnidadeMedidaRequest request,
                                                               @AuthenticationPrincipal AuthenticatedUser usuarioAutenticado) {
        UnidadeMedidaResponse response = unidadeMedidaService.criar(request, EmpresaDaGravacao.de(usuarioAutenticado));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('cadastro:unidade:escrita')")
    @Operation(summary = "Atualizar unidade de medida")
    public ResponseEntity<ApiResponse<UnidadeMedidaResponse>> atualizar(@PathVariable Long id, @Valid @RequestBody UnidadeMedidaRequest request) {
        UnidadeMedidaResponse response = unidadeMedidaService.atualizar(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('cadastro:unidade:leitura')")
    @Operation(summary = "Buscar unidade de medida por ID")
    public ResponseEntity<ApiResponse<UnidadeMedidaResponse>> buscarPorId(@PathVariable Long id) {
        UnidadeMedidaResponse response = unidadeMedidaService.buscarPorId(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('cadastro:unidade:leitura')")
    @Operation(summary = "Listar unidades de medida")
    public ResponseEntity<ApiResponse<List<UnidadeMedidaResponse>>> listar() {
        List<UnidadeMedidaResponse> response = unidadeMedidaService.listar();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('cadastro:unidade:escrita')")
    @Operation(summary = "Excluir unidade de medida (soft delete)")
    public ResponseEntity<ApiResponse<Void>> excluir(@PathVariable Long id) {
        unidadeMedidaService.excluir(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
