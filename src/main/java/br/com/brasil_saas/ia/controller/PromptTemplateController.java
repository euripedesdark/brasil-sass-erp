package br.com.brasil_saas.ia.controller;

import br.com.brasil_saas.ia.model.PromptTemplate;
import br.com.brasil_saas.ia.service.PromptTemplateService;
import br.com.brasil_saas.shared.web.ApiResponse;
import br.com.brasil_saas.shared.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ia/prompt-templates")
@RequiredArgsConstructor
@Tag(name = "IA - Prompt Templates", description = "Templates de prompts para IA")
public class PromptTemplateController {

    private final PromptTemplateService promptTemplateService;

    @PostMapping
    @Operation(summary = "Salvar template de prompt")
    public ResponseEntity<ApiResponse<PromptTemplate>> save(@RequestBody PromptTemplate template) {
        PromptTemplate saved = promptTemplateService.save(template);
        return ResponseEntity.ok(ApiResponse.success(saved));
    }

    @GetMapping
    @Operation(summary = "Listar todos os templates")
    public ResponseEntity<ApiResponse<List<PromptTemplate>>> listAll(@RequestParam Long empresaId) {
        List<PromptTemplate> templates = promptTemplateService.listAll(empresaId);
        return ResponseEntity.ok(ApiResponse.success(templates));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Listar templates por categoria")
    public ResponseEntity<ApiResponse<List<PromptTemplate>>> listByCategory(
            @PathVariable String category,
            @RequestParam Long empresaId) {
        List<PromptTemplate> templates = promptTemplateService.listByCategory(category, empresaId);
        return ResponseEntity.ok(ApiResponse.success(templates));
    }

    @GetMapping("/paginated")
    @Operation(summary = "Listar templates paginados")
    public ResponseEntity<ApiResponse<PageResponse<PromptTemplate>>> listPaginated(
            @RequestParam Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<PromptTemplate> response = promptTemplateService.listPaginated(empresaId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter template por ID")
    public ResponseEntity<ApiResponse<PromptTemplate>> getById(
            @PathVariable Long id,
            @RequestParam Long empresaId) {
        PromptTemplate template = promptTemplateService.getById(id, empresaId);
        return ResponseEntity.ok(ApiResponse.success(template));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir template")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @RequestParam Long empresaId) {
        promptTemplateService.delete(id, empresaId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
