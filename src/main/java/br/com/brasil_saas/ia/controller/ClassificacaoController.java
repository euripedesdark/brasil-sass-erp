package br.com.brasil_saas.ia.controller;

import br.com.brasil_saas.ia.dto.ClassificacaoRequest;
import br.com.brasil_saas.ia.model.Classificacao;
import br.com.brasil_saas.ia.service.ClassificacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ia/classificacoes")
@Tag(name = "Classificacoes", description = "API para gerenciamento de classificacoes de IA")
@RequiredArgsConstructor
public class ClassificacaoController {

    private final ClassificacaoService classificacaoService;

    @PostMapping
    @Operation(summary = "Criar nova classificacao")
    @PreAuthorize("hasAuthority('IA_WRITE')")
    public ResponseEntity<Classificacao> criar(@RequestHeader("X-Empresa-Id") Long empresaId,
                                              @RequestBody ClassificacaoRequest request) {
        Classificacao classificacao = classificacaoService.criar(empresaId, request);
        return ResponseEntity.ok(classificacao);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar classificacao")
    @PreAuthorize("hasAuthority('IA_WRITE')")
    public ResponseEntity<Classificacao> atualizar(@RequestHeader("X-Empresa-Id") Long empresaId,
                                                  @PathVariable Long id,
                                                  @RequestBody ClassificacaoRequest request) {
        Classificacao classificacao = classificacaoService.atualizar(empresaId, id, request);
        return ResponseEntity.ok(classificacao);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar classificacao por ID")
    @PreAuthorize("hasAuthority('IA_READ')")
    public ResponseEntity<Classificacao> buscarPorId(@RequestHeader("X-Empresa-Id") Long empresaId,
                                                    @PathVariable Long id) {
        Classificacao classificacao = classificacaoService.buscarPorId(empresaId, id);
        return ResponseEntity.ok(classificacao);
    }

    @GetMapping
    @Operation(summary = "Listar todas as classificacoes da empresa")
    @PreAuthorize("hasAuthority('IA_READ')")
    public ResponseEntity<List<Classificacao>> listarPorEmpresa(@RequestHeader("X-Empresa-Id") Long empresaId) {
        List<Classificacao> classificacoes = classificacaoService.listarPorEmpresa(empresaId);
        return ResponseEntity.ok(classificacoes);
    }

    @GetMapping("/tipo/{tipo}")
    @Operation(summary = "Listar classificacoes por tipo")
    @PreAuthorize("hasAuthority('IA_READ')")
    public ResponseEntity<List<Classificacao>> listarPorTipo(@RequestHeader("X-Empresa-Id") Long empresaId,
                                                              @PathVariable String tipo) {
        List<Classificacao> classificacoes = classificacaoService.listarPorTipo(empresaId, tipo);
        return ResponseEntity.ok(classificacoes);
    }

    @GetMapping("/entidade/{entidadeId}")
    @Operation(summary = "Listar classificacoes por entidade")
    @PreAuthorize("hasAuthority('IA_READ')")
    public ResponseEntity<List<Classificacao>> listarPorEntidade(@RequestHeader("X-Empresa-Id") Long empresaId,
                                                                  @PathVariable Long entidadeId) {
        List<Classificacao> classificacoes = classificacaoService.listarPorEntidade(empresaId, entidadeId);
        return ResponseEntity.ok(classificacoes);
    }

    @GetMapping("/pendentes/{tipo}")
    @Operation(summary = "Listar classificacoes pendentes por tipo")
    @PreAuthorize("hasAuthority('IA_READ')")
    public ResponseEntity<List<Classificacao>> listarPendentes(@RequestHeader("X-Empresa-Id") Long empresaId,
                                                                @PathVariable String tipo) {
        List<Classificacao> classificacoes = classificacaoService.listarPendentes(empresaId, tipo);
        return ResponseEntity.ok(classificacoes);
    }

    @GetMapping("/high-confidence/{tipo}")
    @Operation(summary = "Listar classificacoes com alta confianca")
    @PreAuthorize("hasAuthority('IA_READ')")
    public ResponseEntity<List<Classificacao>> listarHighConfidence(@RequestHeader("X-Empresa-Id") Long empresaId,
                                                                    @PathVariable String tipo,
                                                                    @RequestParam Double minConfianca) {
        List<Classificacao> classificacoes = classificacaoService.listarHighConfidence(empresaId, tipo, minConfianca);
        return ResponseEntity.ok(classificacoes);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir classificacao")
    @PreAuthorize("hasAuthority('IA_DELETE')")
    public ResponseEntity<Void> excluir(@RequestHeader("X-Empresa-Id") Long empresaId,
                                        @PathVariable Long id) {
        classificacaoService.excluir(empresaId, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/aprovar")
    @Operation(summary = "Aprovar classificacao")
    @PreAuthorize("hasAuthority('IA_WRITE')")
    public ResponseEntity<Void> aprovarClassificacao(@RequestHeader("X-Empresa-Id") Long empresaId,
                                                  @PathVariable Long id,
                                                  @RequestParam String classificacaoManual) {
        classificacaoService.aprovarClassificacao(empresaId, id, classificacaoManual);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/rejeitar")
    @Operation(summary = "Rejeitar classificacao")
    @PreAuthorize("hasAuthority('IA_WRITE')")
    public ResponseEntity<Void> rejeitarClassificacao(@RequestHeader("X-Empresa-Id") Long empresaId,
                                                    @PathVariable Long id,
                                                    @RequestParam String motivo) {
        classificacaoService.rejeitarClassificacao(empresaId, id, motivo);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/classificar-texto")
    @Operation(summary = "Classificar texto")
    @PreAuthorize("hasAuthority('IA_READ')")
    public ResponseEntity<Map<String, Object>> classificarTexto(@RequestHeader("X-Empresa-Id") Long empresaId,
                                                                @RequestParam String tipo,
                                                                @RequestParam String texto) {
        Map<String, Object> resultado = classificacaoService.classificarTexto(empresaId, tipo, texto);
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/classificar-produto")
    @Operation(summary = "Classificar produto")
    @PreAuthorize("hasAuthority('IA_READ')")
    public ResponseEntity<Map<String, Object>> classificarProduto(@RequestHeader("X-Empresa-Id") Long empresaId,
                                                                  @RequestParam Long produtoId,
                                                                  @RequestParam String texto) {
        Map<String, Object> resultado = classificacaoService.classificarProduto(empresaId, produtoId, texto);
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/classificar-lote")
    @Operation(summary = "Classificar lote de entidades")
    @PreAuthorize("hasAuthority('IA_READ')")
    public ResponseEntity<Map<String, Object>> classificarLote(@RequestHeader("X-Empresa-Id") Long empresaId,
                                                                @RequestBody List<Long> entidadeIds) {
        Map<String, Object> resultado = classificacaoService.classificarLote(empresaId, entidadeIds);
        return ResponseEntity.ok(resultado);
    }
}
