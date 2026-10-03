package br.com.brasil_saas.ia.controller;

import br.com.brasil_saas.ia.dto.EmbeddingRequest;
import br.com.brasil_saas.ia.model.Embedding;
import br.com.brasil_saas.ia.service.EmbeddingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ia/embeddings")
@Tag(name = "Embeddings", description = "API para gerenciamento de embeddings")
@RequiredArgsConstructor
public class EmbeddingController {

    private final EmbeddingService embeddingService;

    @PostMapping
    @Operation(summary = "Criar novo embedding")
    @PreAuthorize("hasAuthority('IA_WRITE')")
    public ResponseEntity<Embedding> criar(@RequestHeader("X-Empresa-Id") Long empresaId,
                                           @RequestBody EmbeddingRequest request) {
        Embedding embedding = embeddingService.criar(empresaId, request);
        return ResponseEntity.ok(embedding);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar embedding")
    @PreAuthorize("hasAuthority('IA_WRITE')")
    public ResponseEntity<Embedding> atualizar(@RequestHeader("X-Empresa-Id") Long empresaId,
                                              @PathVariable Long id,
                                              @RequestBody EmbeddingRequest request) {
        Embedding embedding = embeddingService.atualizar(empresaId, id, request);
        return ResponseEntity.ok(embedding);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar embedding por ID")
    @PreAuthorize("hasAuthority('IA_READ')")
    public ResponseEntity<Embedding> buscarPorId(@RequestHeader("X-Empresa-Id") Long empresaId,
                                                 @PathVariable Long id) {
        Embedding embedding = embeddingService.buscarPorId(empresaId, id);
        return ResponseEntity.ok(embedding);
    }

    @GetMapping
    @Operation(summary = "Listar todos os embeddings da empresa")
    @PreAuthorize("hasAuthority('IA_READ')")
    public ResponseEntity<List<Embedding>> listarPorEmpresa(@RequestHeader("X-Empresa-Id") Long empresaId) {
        List<Embedding> embeddings = embeddingService.listarPorEmpresa(empresaId);
        return ResponseEntity.ok(embeddings);
    }

    @GetMapping("/entidade-tipo/{entidadeTipo}")
    @Operation(summary = "Listar embeddings por tipo de entidade")
    @PreAuthorize("hasAuthority('IA_READ')")
    public ResponseEntity<List<Embedding>> listarPorEntidadeTipo(@RequestHeader("X-Empresa-Id") Long empresaId,
                                                                  @PathVariable String entidadeTipo) {
        List<Embedding> embeddings = embeddingService.listarPorEntidadeTipo(empresaId, entidadeTipo);
        return ResponseEntity.ok(embeddings);
    }

    @GetMapping("/entidade/{entidadeTipo}/{entidadeId}")
    @Operation(summary = "Listar embeddings por entidade")
    @PreAuthorize("hasAuthority('IA_READ')")
    public ResponseEntity<List<Embedding>> listarPorEntidade(@RequestHeader("X-Empresa-Id") Long empresaId,
                                                              @PathVariable String entidadeTipo,
                                                              @PathVariable Long entidadeId) {
        List<Embedding> embeddings = embeddingService.listarPorEntidade(empresaId, entidadeTipo, entidadeId);
        return ResponseEntity.ok(embeddings);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir embedding")
    @PreAuthorize("hasAuthority('IA_DELETE')")
    public ResponseEntity<Void> excluir(@RequestHeader("X-Empresa-Id") Long empresaId,
                                        @PathVariable Long id) {
        embeddingService.excluir(empresaId, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/entidade/{entidadeTipo}/{entidadeId}")
    @Operation(summary = "Excluir embeddings por entidade")
    @PreAuthorize("hasAuthority('IA_DELETE')")
    public ResponseEntity<Void> excluirPorEntidade(@RequestHeader("X-Empresa-Id") Long empresaId,
                                                 @PathVariable String entidadeTipo,
                                                 @PathVariable Long entidadeId) {
        embeddingService.excluirPorEntidade(empresaId, entidadeTipo, entidadeId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/gerar")
    @Operation(summary = "Gerar embedding para texto")
    @PreAuthorize("hasAuthority('IA_READ')")
    public ResponseEntity<float[]> gerarEmbedding(@RequestHeader("X-Empresa-Id") Long empresaId,
                                                  @RequestParam String texto) {
        float[] embedding = embeddingService.gerarEmbedding(empresaId, texto);
        return ResponseEntity.ok(embedding);
    }

    @GetMapping("/similares")
    @Operation(summary = "Buscar embeddings similares")
    @PreAuthorize("hasAuthority('IA_READ')")
    public ResponseEntity<Map<String, Object>> buscarSimilares(@RequestHeader("X-Empresa-Id") Long empresaId,
                                                                @RequestParam String entidadeTipo,
                                                                @RequestParam String texto,
                                                                @RequestParam(required = false, defaultValue = "5") Integer limite) {
        Map<String, Object> resultado = embeddingService.buscarSimilares(empresaId, entidadeTipo, texto, limite);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/mais-similar")
    @Operation(summary = "Buscar embedding mais similar")
    @PreAuthorize("hasAuthority('IA_READ')")
    public ResponseEntity<Map<String, Object>> buscarMaisSimilar(@RequestHeader("X-Empresa-Id") Long empresaId,
                                                                 @RequestParam String entidadeTipo,
                                                                 @RequestParam String texto) {
        Map<String, Object> resultado = embeddingService.buscarMaisSimilar(empresaId, entidadeTipo, texto);
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/reindexar")
    @Operation(summary = "Reindexar embeddings")
    @PreAuthorize("hasAuthority('IA_WRITE')")
    public ResponseEntity<Void> reindexarEmbeddings(@RequestHeader("X-Empresa-Id") Long empresaId,
                                                   @RequestParam String entidadeTipo) {
        embeddingService.reindexarEmbeddings(empresaId, entidadeTipo);
        return ResponseEntity.ok().build();
    }
}
