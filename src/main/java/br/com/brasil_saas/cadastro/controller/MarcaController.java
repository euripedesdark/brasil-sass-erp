package br.com.brasil_saas.cadastro.controller;

import br.com.brasil_saas.cadastro.dto.MarcaRequest;
import br.com.brasil_saas.cadastro.dto.MarcaResponse;
import br.com.brasil_saas.cadastro.service.MarcaService;
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
@RequestMapping("/api/cadastro/marcas")
@RequiredArgsConstructor
@Tag(name = "Cadastro - Marcas")
public class MarcaController {

    private final MarcaService marcaService;

    @PostMapping
    @PreAuthorize("hasAuthority('cadastro:marca:escrita')")
    @Operation(summary = "Criar marca")
    public ResponseEntity<ApiResponse<MarcaResponse>> criar(@Valid @RequestBody MarcaRequest request,
                                                               @AuthenticationPrincipal AuthenticatedUser usuarioAutenticado) {
        MarcaResponse response = marcaService.criar(request, EmpresaDaGravacao.de(usuarioAutenticado));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('cadastro:marca:escrita')")
    @Operation(summary = "Atualizar marca")
    public ResponseEntity<ApiResponse<MarcaResponse>> atualizar(@PathVariable Long id, @Valid @RequestBody MarcaRequest request) {
        MarcaResponse response = marcaService.atualizar(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('cadastro:marca:leitura')")
    @Operation(summary = "Buscar marca por ID")
    public ResponseEntity<ApiResponse<MarcaResponse>> buscarPorId(@PathVariable Long id) {
        MarcaResponse response = marcaService.buscarPorId(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('cadastro:marca:leitura')")
    @Operation(summary = "Listar marcas")
    public ResponseEntity<ApiResponse<List<MarcaResponse>>> listar() {
        List<MarcaResponse> response = marcaService.listar();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('cadastro:marca:escrita')")
    @Operation(summary = "Excluir marca (soft delete)")
    public ResponseEntity<ApiResponse<Void>> excluir(@PathVariable Long id) {
        marcaService.excluir(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
