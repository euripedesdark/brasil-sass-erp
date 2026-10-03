package br.com.brasil_saas.cadastro.controller;

import br.com.brasil_saas.cadastro.service.FornecedorService;
import br.com.brasil_saas.cadastro.service.dto.FornecedorDtos;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import br.com.brasil_saas.shared.web.PageResponse;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/cadastro/fornecedores")
@RequiredArgsConstructor
public class FornecedorController {

    private final FornecedorService service;

    @GetMapping
    @PreAuthorize("hasAuthority('cadastro:fornecedor:leitura')")
    public PageResponse<FornecedorDtos.Response> listar(@AuthenticationPrincipal AuthenticatedUser usuario,
                                                        @PageableDefault(size = 20) Pageable pageable) {
        return service.listar(usuario.getEmpresaId(), pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('cadastro:fornecedor:leitura')")
    public FornecedorDtos.Response buscar(@AuthenticationPrincipal AuthenticatedUser usuario, @PathVariable Long id) {
        return service.buscarPorId(usuario.getEmpresaId(), id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('cadastro:fornecedor:escrita')")
    public ResponseEntity<FornecedorDtos.Response> criar(@AuthenticationPrincipal AuthenticatedUser usuario,
                                                         @Valid @RequestBody FornecedorDtos.Request request) {
        FornecedorDtos.Response response = service.criar(usuario.getEmpresaId(), request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('cadastro:fornecedor:escrita')")
    public FornecedorDtos.Response atualizar(@AuthenticationPrincipal AuthenticatedUser usuario,
                                             @PathVariable Long id, @Valid @RequestBody FornecedorDtos.Request request) {
        return service.atualizar(usuario.getEmpresaId(), id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('cadastro:fornecedor:escrita')")
    public ResponseEntity<Void> excluir(@AuthenticationPrincipal AuthenticatedUser usuario, @PathVariable Long id) {
        service.excluir(usuario.getEmpresaId(), id);
        return ResponseEntity.noContent().build();
    }
}
