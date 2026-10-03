package br.com.brasil_saas.cadastro.controller;

import br.com.brasil_saas.cadastro.dto.ServicoRequest;
import br.com.brasil_saas.cadastro.dto.ServicoResponse;
import br.com.brasil_saas.cadastro.service.ServicoService;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import br.com.brasil_saas.shared.tenant.EmpresaDaGravacao;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cadastro/servicos")
@RequiredArgsConstructor
@Tag(name = "Cadastro - Serviços")
public class ServicoController {

    private final ServicoService servicoService;

    @PostMapping
    @PreAuthorize("hasAuthority('cadastro:servico:escrita')")
    @Operation(summary = "Criar serviço")
    public ResponseEntity<ApiResponse<ServicoResponse>> criar(@Valid @RequestBody ServicoRequest request,
                                                               @AuthenticationPrincipal AuthenticatedUser usuarioAutenticado) {
        ServicoResponse response = servicoService.criar(request, EmpresaDaGravacao.de(usuarioAutenticado));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('cadastro:servico:escrita')")
    @Operation(summary = "Atualizar serviço")
    public ResponseEntity<ApiResponse<ServicoResponse>> atualizar(@PathVariable Long id, @Valid @RequestBody ServicoRequest request) {
        ServicoResponse response = servicoService.atualizar(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('cadastro:servico:leitura')")
    @Operation(summary = "Buscar serviço por ID")
    public ResponseEntity<ApiResponse<ServicoResponse>> buscarPorId(@PathVariable Long id) {
        ServicoResponse response = servicoService.buscarPorId(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('cadastro:servico:leitura')")
    @Operation(summary = "Listar serviços com paginação e filtros")
    public ResponseEntity<ApiResponse<PageResponse<ServicoResponse>>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) Boolean ativo,
            Pageable pageable) {
        PageResponse<ServicoResponse> response = servicoService.listar(nome, codigo, ativo, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('cadastro:servico:escrita')")
    @Operation(summary = "Excluir serviço (soft delete)")
    public ResponseEntity<ApiResponse<Void>> excluir(@PathVariable Long id) {
        servicoService.excluir(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
