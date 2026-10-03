package br.com.brasil_saas.cadastro.controller;

import br.com.brasil_saas.cadastro.dto.PessoaRequest;
import br.com.brasil_saas.cadastro.dto.PessoaResponse;
import br.com.brasil_saas.cadastro.service.PessoaService;
import br.com.brasil_saas.shared.web.ApiResponse;
import br.com.brasil_saas.shared.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import br.com.brasil_saas.shared.security.AuthenticatedUser;

@RestController
@RequestMapping("/api/cadastro/pessoas")
@RequiredArgsConstructor
@Tag(name = "Cadastro - Pessoas")
public class PessoaController {

    private final PessoaService pessoaService;

    @PostMapping
    @PreAuthorize("hasAuthority('cadastro:pessoa:escrita')")
    @Operation(summary = "Criar pessoa")
    public ResponseEntity<ApiResponse<PessoaResponse>> criar(
            @Valid @RequestBody PessoaRequest request,
            @AuthenticationPrincipal AuthenticatedUser usuarioAutenticado) {
        // A empresa vem do token. Sem isso o INSERT fica com empresa_id nulo e
        // o banco recusa com violacao de not-null, devolvida como 409 — que
        // parece duplicidade mas e falta do tenant.
        PessoaResponse response = pessoaService.criar(request, usuarioAutenticado.getEmpresaId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('cadastro:pessoa:escrita')")
    @Operation(summary = "Atualizar pessoa")
    public ResponseEntity<ApiResponse<PessoaResponse>> atualizar(@PathVariable Long id, @Valid @RequestBody PessoaRequest request) {
        PessoaResponse response = pessoaService.atualizar(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('cadastro:pessoa:leitura')")
    @Operation(summary = "Buscar pessoa por ID")
    public ResponseEntity<ApiResponse<PessoaResponse>> buscarPorId(@PathVariable Long id) {
        PessoaResponse response = pessoaService.buscarPorId(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('cadastro:pessoa:leitura')")
    @Operation(summary = "Listar pessoas com paginação e filtros")
    public ResponseEntity<ApiResponse<PageResponse<PessoaResponse>>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String documento,
            Pageable pageable) {
        PageResponse<PessoaResponse> response = pessoaService.listar(nome, documento, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('cadastro:pessoa:escrita')")
    @Operation(summary = "Excluir pessoa (soft delete)")
    public ResponseEntity<ApiResponse<Void>> excluir(@PathVariable Long id) {
        pessoaService.excluir(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
