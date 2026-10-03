package br.com.brasil_saas.cadastro.controller;

import br.com.brasil_saas.cadastro.model.Fornecedor;
import br.com.brasil_saas.cadastro.repository.FornecedorRepository;
import br.com.brasil_saas.shared.dto.ImagemDto;
import br.com.brasil_saas.shared.image.ImagemDocumento;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import br.com.brasil_saas.shared.service.GenericoImagemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Tag(name = "Cadastro - Logo do Fornecedor")
@RestController
@RequestMapping("/api/cadastro/fornecedores/logo")
@RequiredArgsConstructor
public class FornecedorLogoController {

    private final FornecedorRepository fornecedorRepository;
    private final GenericoImagemService imagemService;

    @PostMapping
    @PreAuthorize("hasAuthority('cadastro:fornecedor:escrita')")
    @Operation(summary = "Atualizar logo do fornecedor")
    public ResponseEntity<ImagemDto> atualizarLogo(
            @RequestParam("id") Long fornecedorId,
            @RequestParam("logo") MultipartFile arquivo,
            @AuthenticationPrincipal AuthenticatedUser usuarioAutenticado) throws IOException {
        
        Optional<Fornecedor> optionalFornecedor = fornecedorRepository.findById(fornecedorId);
        if (optionalFornecedor.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Fornecedor fornecedor = optionalFornecedor.get();
        
        // Verificar se o fornecedor pertence à mesma empresa do usuário autenticado
        if (!fornecedor.getEmpresaId().equals(usuarioAutenticado.getEmpresaId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Salvar imagem no MongoDB
        ImagemDocumento imagemDoc = imagemService.salvarImagem(
            usuarioAutenticado.getEmpresaId(), 
            "fornecedor_logo", 
            fornecedor.getId(), 
            arquivo
        );
        
        // Atualizar dados do logo no fornecedor
        fornecedor.setLogoUrl("/api/cadastro/fornecedores/" + fornecedor.getId() + "/logo"); // Caminho virtual para recuperação
        fornecedor.setLogoTipoConteudo(imagemDoc.getContentType());
        fornecedor.setLogoTamanho(imagemDoc.getTamanho());
        
        // Salvar fornecedor
        fornecedor = fornecedorRepository.save(fornecedor);
        
        ImagemDto imagemDto = new ImagemDto();
        imagemDto.setUrl(fornecedor.getLogoUrl());
        imagemDto.setTipoConteudo(fornecedor.getLogoTipoConteudo());
        imagemDto.setTamanho(fornecedor.getLogoTamanho());
        
        return ResponseEntity.ok(imagemDto);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('cadastro:fornecedor:leitura')")
    @Operation(summary = "Obter logo do fornecedor")
    public ResponseEntity<byte[]> obterLogo(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser usuarioAutenticado) {
        
        Optional<Fornecedor> optionalFornecedor = fornecedorRepository.findById(id);
        if (optionalFornecedor.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Fornecedor fornecedor = optionalFornecedor.get();
        
        // Verificar se o fornecedor pertence à mesma empresa do usuário autenticado
        if (!fornecedor.getEmpresaId().equals(usuarioAutenticado.getEmpresaId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        ImagemDocumento imagemDoc = imagemService.buscarImagem(usuarioAutenticado.getEmpresaId(), "fornecedor_logo", id);
        if (imagemDoc == null || imagemDoc.getConteudo() == null) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(imagemDoc.getContentType()));
        headers.setContentLength(imagemDoc.getConteudo().length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(imagemDoc.getConteudo());
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('cadastro:fornecedor:escrita')")
    @Operation(summary = "Remover logo do fornecedor")
    public ResponseEntity<Void> removerLogo(
            @RequestParam("id") Long fornecedorId,
            @AuthenticationPrincipal AuthenticatedUser usuarioAutenticado) {
        
        Optional<Fornecedor> optionalFornecedor = fornecedorRepository.findById(fornecedorId);
        if (optionalFornecedor.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Fornecedor fornecedor = optionalFornecedor.get();
        
        // Verificar se o fornecedor pertence à mesma empresa do usuário autenticado
        if (!fornecedor.getEmpresaId().equals(usuarioAutenticado.getEmpresaId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // Remover imagem do MongoDB
        imagemService.removerImagem(usuarioAutenticado.getEmpresaId(), "fornecedor_logo", fornecedor.getId());
        
        fornecedor.setLogoUrl(null);
        fornecedor.setLogoTipoConteudo(null);
        fornecedor.setLogoTamanho(null);
        
        fornecedorRepository.save(fornecedor);
        
        return ResponseEntity.noContent().build();
    }
}