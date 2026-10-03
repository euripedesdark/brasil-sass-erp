package br.com.brasil_saas.cadastro.controller;

import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import java.util.List;
import br.com.brasil_saas.cadastro.dto.ProdutoRequest;
import br.com.brasil_saas.cadastro.dto.ProdutoResponse;
import br.com.brasil_saas.cadastro.service.ProdutoService;
import br.com.brasil_saas.cadastro.image.ImagemProdutoDocumento;
import br.com.brasil_saas.cadastro.image.ProdutoImagemService;
import br.com.brasil_saas.shared.web.ApiResponse;
import br.com.brasil_saas.shared.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/cadastro/produtos")
@RequiredArgsConstructor
@Tag(name = "Cadastro - Produtos")
public class ProdutoController {

    private final ProdutoService produtoService;
    private final ProdutoImagemService produtoImagemService;

    @PostMapping
    @PreAuthorize("hasAuthority('cadastro:produto:escrita')")
    @Operation(summary = "Criar produto")
    public ResponseEntity<ApiResponse<ProdutoResponse>> criar(
            @Valid @RequestBody ProdutoRequest request,
            @AuthenticationPrincipal AuthenticatedUser usuario) {
        ProdutoResponse response = produtoService.criar(request, usuario.getEmpresaId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('cadastro:produto:escrita')")
    @Operation(summary = "Atualizar produto")
    public ResponseEntity<ApiResponse<ProdutoResponse>> atualizar(@PathVariable Long id, @Valid @RequestBody ProdutoRequest request,
            @AuthenticationPrincipal AuthenticatedUser usuario) {
        ProdutoResponse response = produtoService.atualizar(usuario.getEmpresaId(), id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('cadastro:produto:leitura')")
    @Operation(summary = "Buscar produto por ID")
    public ResponseEntity<ApiResponse<ProdutoResponse>> buscarPorId(@PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser usuario) {
        ProdutoResponse response = produtoService.buscarPorId(usuario.getEmpresaId(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('cadastro:produto:leitura')")
    @Operation(summary = "Listar produtos com paginação e filtros")
    public ResponseEntity<ApiResponse<PageResponse<ProdutoResponse>>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long marcaId,
            @RequestParam(required = false) Boolean ativo,
            Pageable pageable,
            @AuthenticationPrincipal AuthenticatedUser usuario) {
        PageResponse<ProdutoResponse> response = produtoService.listar(usuario.getEmpresaId(), nome, codigo, categoriaId, marcaId, ativo, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('cadastro:produto:escrita')")
    @Operation(summary = "Excluir produto (soft delete)")
    public ResponseEntity<ApiResponse<Void>> excluir(@PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser usuario) {
        produtoService.excluir(usuario.getEmpresaId(), id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping(value = "/{id}/imagens", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('cadastro:produto:escrita')")
    @Operation(summary = "Enviar imagem do produto para o MongoDB")
    public ResponseEntity<ApiResponse<ImagemResponse>> enviarImagem(@PathVariable Long id,
            @RequestPart("arquivo") MultipartFile arquivo,
            @RequestParam(defaultValue = "false") Boolean principal) throws java.io.IOException {
        ImagemProdutoDocumento imagem = produtoImagemService.salvarUpload(id, arquivo, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(ImagemResponse.from(imagem)));
    }

    /**
     * Lista as imagens do produto.
     *
     * Sem esta rota a tela so conseguia exibir uma imagem se ainda tivesse o id
     * devolvido no upload; ao recarregar, todas sumiam da lista.
     */
    @GetMapping("/{id}/imagens")
    @PreAuthorize("hasAuthority('cadastro:produto:leitura')")
    @Operation(summary = "Listar imagens do produto")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listarImagens(
            @PathVariable Long id) {
        // A listagem vem da referencia no Postgres (id, ordem, url). Os bytes
        // continuam no Mongo, lidos por GET /{id}/imagens/{imagemId} — o
        // mesmo desenho que o NFS-e ja usa para XML e PDF.
        // A listagem vem da referencia no Postgres (id, ordem, url). Os bytes
        // continuam no Mongo, lidos por GET /{id}/imagens/{imagemId} — o
        // mesmo desenho que o NFS-e ja usa para XML e PDF.
        List<Map<String, Object>> imagens = new java.util.ArrayList<>();
        for (br.com.brasil_saas.cadastro.model.ProdutoImagem ref : produtoImagemService.listar(id)) {
            // O id devolvido tem de ser o do documento no Mongo, nao o da
            // referencia no Postgres: e por ele que o GET dos bytes funciona.
            // Devolvendo o id da referencia, a tela montaria uma URL que da
            // 404 — foi o que aconteceu na primeira versao.
            String idMongo = ref.getUrl() == null ? null
                    : ref.getUrl().substring(ref.getUrl().lastIndexOf('/') + 1);
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("id", idMongo);
            m.put("ordem", ref.getOrdem());
            m.put("principal", Boolean.TRUE.equals(ref.getPrincipal()));
            m.put("url", ref.getUrl());
            imagens.add(m);
        }
        return ResponseEntity.ok(ApiResponse.success(imagens));
    }

    @GetMapping("/{id}/imagens/{imagemId}")
    @PreAuthorize("hasAuthority('cadastro:produto:leitura')")
    @Operation(summary = "Baixar imagem do produto armazenada no MongoDB")
    public ResponseEntity<byte[]> obterImagem(@PathVariable Long id, @PathVariable String imagemId) {
        ImagemProdutoDocumento imagem = produtoImagemService.buscar(imagemId);
        if (!id.equals(imagem.getProdutoId())) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(imagem.getContentType()))
                .contentLength(imagem.getTamanho()).body(imagem.getConteudo());
    }

    public record ImagemResponse(String id, String nomeArquivo, String contentType, long tamanho, String url) {
        static ImagemResponse from(ImagemProdutoDocumento imagem) {
            return new ImagemResponse(imagem.getId(), imagem.getNomeArquivo(), imagem.getContentType(), imagem.getTamanho(),
                    "/api/cadastro/produtos/" + imagem.getProdutoId() + "/imagens/" + imagem.getId());
        }
    }
}
