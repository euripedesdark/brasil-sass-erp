package br.com.brasil_saas.ia.controller;

import br.com.brasil_saas.ia.dto.AnalisePreditivaRequest;
import br.com.brasil_saas.ia.model.AnalisePreditiva;
import br.com.brasil_saas.ia.service.AnalisePreditivaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ia/analises")
@Tag(name = "Analises Preditivas", description = "API para gerenciamento de analises preditivas")
@RequiredArgsConstructor
public class AnalisePreditivaController {

    private final AnalisePreditivaService analiseService;

    @PostMapping
    @Operation(summary = "Criar nova analise preditiva")
    @PreAuthorize("hasAuthority('IA_WRITE')")
    public ResponseEntity<AnalisePreditiva> criar(@RequestHeader("X-Empresa-Id") Long empresaId,
                                                @RequestBody AnalisePreditivaRequest request) {
        AnalisePreditiva analise = analiseService.criar(empresaId, request);
        return ResponseEntity.ok(analise);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar analise preditiva")
    @PreAuthorize("hasAuthority('IA_WRITE')")
    public ResponseEntity<AnalisePreditiva> atualizar(@RequestHeader("X-Empresa-Id") Long empresaId,
                                                    @PathVariable Long id,
                                                    @RequestBody AnalisePreditivaRequest request) {
        AnalisePreditiva analise = analiseService.atualizar(empresaId, id, request);
        return ResponseEntity.ok(analise);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar analise por ID")
    @PreAuthorize("hasAuthority('IA_READ')")
    public ResponseEntity<AnalisePreditiva> buscarPorId(@RequestHeader("X-Empresa-Id") Long empresaId,
                                                        @PathVariable Long id) {
        AnalisePreditiva analise = analiseService.buscarPorId(empresaId, id);
        return ResponseEntity.ok(analise);
    }

    @GetMapping
    @Operation(summary = "Listar todas as analises da empresa")
    @PreAuthorize("hasAuthority('IA_READ')")
    public ResponseEntity<List<AnalisePreditiva>> listarPorEmpresa(@RequestHeader("X-Empresa-Id") Long empresaId) {
        List<AnalisePreditiva> analises = analiseService.listarPorEmpresa(empresaId);
        return ResponseEntity.ok(analises);
    }

    @GetMapping("/tipo/{tipo}")
    @Operation(summary = "Listar analises por tipo")
    @PreAuthorize("hasAuthority('IA_READ')")
    public ResponseEntity<List<AnalisePreditiva>> listarPorTipo(@RequestHeader("X-Empresa-Id") Long empresaId,
                                                                @PathVariable String tipo) {
        List<AnalisePreditiva> analises = analiseService.listarPorTipo(empresaId, tipo);
        return ResponseEntity.ok(analises);
    }

    @GetMapping("/entidade/{entidade}/{entidadeId}")
    @Operation(summary = "Listar analises por entidade")
    @PreAuthorize("hasAuthority('IA_READ')")
    public ResponseEntity<List<AnalisePreditiva>> listarPorEntidade(@RequestHeader("X-Empresa-Id") Long empresaId,
                                                                    @PathVariable String entidade,
                                                                    @PathVariable Long entidadeId) {
        List<AnalisePreditiva> analises = analiseService.listarPorEntidade(empresaId, entidade, entidadeId);
        return ResponseEntity.ok(analises);
    }

    @GetMapping("/pendentes")
    @Operation(summary = "Listar analises pendentes")
    @PreAuthorize("hasAuthority('IA_READ')")
    public ResponseEntity<List<AnalisePreditiva>> listarPendentes(@RequestHeader("X-Empresa-Id") Long empresaId) {
        List<AnalisePreditiva> analises = analiseService.listarPendentes(empresaId);
        return ResponseEntity.ok(analises);
    }

    @GetMapping("/high-confidence")
    @Operation(summary = "Listar analises com alta confianca")
    @PreAuthorize("hasAuthority('IA_READ')")
    public ResponseEntity<List<AnalisePreditiva>> listarHighConfidence(@RequestHeader("X-Empresa-Id") Long empresaId,
                                                                        @RequestParam String tipo,
                                                                        @RequestParam Double minConfianca) {
        List<AnalisePreditiva> analises = analiseService.listarHighConfidence(empresaId, tipo, minConfianca);
        return ResponseEntity.ok(analises);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir analise preditiva")
    @PreAuthorize("hasAuthority('IA_DELETE')")
    public ResponseEntity<Void> excluir(@RequestHeader("X-Empresa-Id") Long empresaId,
                                        @PathVariable Long id) {
        analiseService.excluir(empresaId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/prever-vendas")
    @Operation(summary = "Prever vendas para um produto")
    @PreAuthorize("hasAuthority('IA_READ')")
    public ResponseEntity<Map<String, Object>> preverVendas(@RequestHeader("X-Empresa-Id") Long empresaId,
                                                             @RequestParam Long produtoId,
                                                             @RequestParam Integer dias) {
        Map<String, Object> resultado = analiseService.preverVendas(empresaId, produtoId, dias);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/prever-estoque")
    @Operation(summary = "Prever estoque para um produto")
    @PreAuthorize("hasAuthority('IA_READ')")
    public ResponseEntity<Map<String, Object>> preverEstoque(@RequestHeader("X-Empresa-Id") Long empresaId,
                                                             @RequestParam Long produtoId,
                                                             @RequestParam Integer dias) {
        Map<String, Object> resultado = analiseService.preverEstoque(empresaId, produtoId, dias);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/prever-financeiro")
    @Operation(summary = "Prever financeiro")
    @PreAuthorize("hasAuthority('IA_READ')")
    public ResponseEntity<Map<String, Object>> preverFinanceiro(@RequestHeader("X-Empresa-Id") Long empresaId,
                                                               @RequestParam Integer dias) {
        Map<String, Object> resultado = analiseService.preverFinanceiro(empresaId, dias);
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/executar-pendentes")
    @Operation(summary = "Executar todas as analises pendentes")
    @PreAuthorize("hasAuthority('IA_WRITE')")
    public ResponseEntity<Void> executarAnalisesPendentes() {
        analiseService.executarAnalisesPendentes();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/recalcular")
    @Operation(summary = "Recalcular analise")
    @PreAuthorize("hasAuthority('IA_WRITE')")
    public ResponseEntity<Void> recalcularAnalise(@RequestHeader("X-Empresa-Id") Long empresaId,
                                               @PathVariable Long id) {
        analiseService.recalcularAnalise(empresaId, id);
        return ResponseEntity.ok().build();
    }
}
