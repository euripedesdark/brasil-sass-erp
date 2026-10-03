package br.com.brasil_saas.ia.controller;

import br.com.brasil_saas.ia.dto.PromptRequest;
import br.com.brasil_saas.ia.model.Prompt;
import br.com.brasil_saas.ia.service.PromptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ia/prompts")
@Tag(name = "Prompts", description = "API para gerenciamento de prompts de IA")
@RequiredArgsConstructor
public class PromptController {

    private final PromptService promptService;

    @PostMapping
    @Operation(summary = "Criar novo prompt")
    @PreAuthorize("hasAuthority('IA_WRITE')")
    public ResponseEntity<Prompt> criar(@RequestHeader("X-Empresa-Id") Long empresaId,
                                        @RequestBody PromptRequest request) {
        Prompt prompt = promptService.criar(empresaId, request);
        return ResponseEntity.ok(prompt);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar prompt")
    @PreAuthorize("hasAuthority('IA_WRITE')")
    public ResponseEntity<Prompt> atualizar(@RequestHeader("X-Empresa-Id") Long empresaId,
                                            @PathVariable Long id,
                                            @RequestBody PromptRequest request) {
        Prompt prompt = promptService.atualizar(empresaId, id, request);
        return ResponseEntity.ok(prompt);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar prompt por ID")
    @PreAuthorize("hasAuthority('IA_READ')")
    public ResponseEntity<Prompt> buscarPorId(@RequestHeader("X-Empresa-Id") Long empresaId,
                                              @PathVariable Long id) {
        Prompt prompt = promptService.buscarPorId(empresaId, id);
        return ResponseEntity.ok(prompt);
    }

    @GetMapping
    @Operation(summary = "Listar todos os prompts da empresa")
    @PreAuthorize("hasAuthority('IA_READ')")
    public ResponseEntity<List<Prompt>> listarPorEmpresa(@RequestHeader("X-Empresa-Id") Long empresaId) {
        List<Prompt> prompts = promptService.listarPorEmpresa(empresaId);
        return ResponseEntity.ok(prompts);
    }

    @GetMapping("/publicos")
    @Operation(summary = "Listar prompts publicos")
    @PreAuthorize("hasAuthority('IA_READ')")
    public ResponseEntity<List<Prompt>> listarPublicos(@RequestHeader("X-Empresa-Id") Long empresaId) {
        List<Prompt> prompts = promptService.listarPublicos(empresaId);
        return ResponseEntity.ok(prompts);
    }

    @GetMapping("/categoria/{categoria}")
    @Operation(summary = "Listar prompts por categoria")
    @PreAuthorize("hasAuthority('IA_READ')")
    public ResponseEntity<List<Prompt>> listarPorCategoria(@RequestHeader("X-Empresa-Id") Long empresaId,
                                                            @PathVariable String categoria) {
        List<Prompt> prompts = promptService.listarPorCategoria(empresaId, categoria);
        return ResponseEntity.ok(prompts);
    }

    @GetMapping("/favoritos")
    @Operation(summary = "Listar prompts favoritos")
    @PreAuthorize("hasAuthority('IA_READ')")
    public ResponseEntity<List<Prompt>> listarFavoritos(@RequestHeader("X-Empresa-Id") Long empresaId) {
        List<Prompt> prompts = promptService.listarFavoritos(empresaId);
        return ResponseEntity.ok(prompts);
    }

    @GetMapping("/mais-usados")
    @Operation(summary = "Listar prompts mais usados")
    @PreAuthorize("hasAuthority('IA_READ')")
    public ResponseEntity<List<Prompt>> listarMaisUsados(@RequestHeader("X-Empresa-Id") Long empresaId) {
        List<Prompt> prompts = promptService.listarMaisUsados(empresaId);
        return ResponseEntity.ok(prompts);
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar prompts por termo")
    @PreAuthorize("hasAuthority('IA_READ')")
    public ResponseEntity<List<Prompt>> buscarPorTermo(@RequestHeader("X-Empresa-Id") Long empresaId,
                                                      @RequestParam String termo) {
        List<Prompt> prompts = promptService.buscarPorTermo(empresaId, termo);
        return ResponseEntity.ok(prompts);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir prompt")
    @PreAuthorize("hasAuthority('IA_DELETE')")
    public ResponseEntity<Void> excluir(@RequestHeader("X-Empresa-Id") Long empresaId,
                                        @PathVariable Long id) {
        promptService.excluir(empresaId, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/favorito")
    @Operation(summary = "Alternar favorito do prompt")
    @PreAuthorize("hasAuthority('IA_WRITE')")
    public ResponseEntity<Void> alternarFavorito(@RequestHeader("X-Empresa-Id") Long empresaId,
                                                @PathVariable Long id) {
        promptService.alternarFavorito(empresaId, id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/incrementar-uso")
    @Operation(summary = "Incrementar contador de uso do prompt")
    @PreAuthorize("hasAuthority('IA_WRITE')")
    public ResponseEntity<Void> incrementarContadorUso(@RequestHeader("X-Empresa-Id") Long empresaId,
                                                    @PathVariable Long id) {
        promptService.incrementarContadorUso(empresaId, id);
        return ResponseEntity.ok().build();
    }
}
