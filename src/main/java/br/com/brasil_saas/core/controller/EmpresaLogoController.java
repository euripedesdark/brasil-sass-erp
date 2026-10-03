package br.com.brasil_saas.core.controller;

import br.com.brasil_saas.core.model.Empresa;
import br.com.brasil_saas.core.repository.EmpresaRepository;
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

@Tag(name = "Core - Logo da Empresa")
@RestController
@RequestMapping("/api/core/empresas")
@RequiredArgsConstructor
public class EmpresaLogoController {

    private final EmpresaRepository empresaRepository;
    private final GenericoImagemService imagemService;

    @PostMapping(value = "/{id}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('core:empresa:escrita')")
    @Operation(summary = "Atualizar logo da empresa")
    public ResponseEntity<ImagemDto> atualizarLogo(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser usuarioAutenticado,
            @RequestParam("logo") MultipartFile arquivo) throws IOException {
        
        // O id do caminho e conferido contra o do token: a rota aceitar
        // qualquer id daria a ilusao de trocar a imagem de outra empresa.
        if (!usuarioAutenticado.getEmpresaId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Optional<Empresa> optionalEmpresa = empresaRepository.findById(usuarioAutenticado.getEmpresaId());
        if (optionalEmpresa.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Empresa empresa = optionalEmpresa.get();
        
        // Salvar imagem no MongoDB
        ImagemDocumento imagemDoc = imagemService.salvarImagem(
            empresa.getId(), // empresa como tenant usa seu próprio ID
            "empresa_logo", 
            empresa.getId(), 
            arquivo
        );
        
        // Atualizar dados do logo na empresa
        empresa.setLogoUrl("/api/core/empresas/" + empresa.getId() + "/logo"); // Caminho virtual para recuperação
        empresa.setLogoTipoConteudo(imagemDoc.getContentType());
        empresa.setLogoTamanho(imagemDoc.getTamanho());
        
        // Salvar empresa
        empresa = empresaRepository.save(empresa);
        
        ImagemDto imagemDto = new ImagemDto();
        imagemDto.setUrl(empresa.getLogoUrl());
        imagemDto.setTipoConteudo(empresa.getLogoTipoConteudo());
        imagemDto.setTamanho(empresa.getLogoTamanho());
        
        return ResponseEntity.ok(imagemDto);
    }

    @GetMapping("/{id}/logo")
    @PreAuthorize("hasAuthority('core:empresa:leitura')")
    @Operation(summary = "Obter logo da empresa")
    public ResponseEntity<byte[]> obterLogo(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser usuarioAutenticado) {
        
        // Somente permitir acesso ao logo da empresa do usuário autenticado
        if (!id.equals(usuarioAutenticado.getEmpresaId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ImagemDocumento imagemDoc = imagemService.buscarImagem(id, "empresa_logo", id); // empresa_id = id para o tenant
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

    @DeleteMapping("/{id}/logo")
    @PreAuthorize("hasAuthority('core:empresa:escrita')")
    @Operation(summary = "Remover logo da empresa")
    public ResponseEntity<Void> removerLogo(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser usuarioAutenticado) {
        
        // O id do caminho e conferido contra o do token: a rota aceitar
        // qualquer id daria a ilusao de trocar a imagem de outra empresa.
        if (!usuarioAutenticado.getEmpresaId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Optional<Empresa> optionalEmpresa = empresaRepository.findById(usuarioAutenticado.getEmpresaId());
        if (optionalEmpresa.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Empresa empresa = optionalEmpresa.get();
        
        // Remover imagem do MongoDB
        imagemService.removerImagem(empresa.getId(), "empresa_logo", empresa.getId());
        
        empresa.setLogoUrl(null);
        empresa.setLogoTipoConteudo(null);
        empresa.setLogoTamanho(null);
        
        empresaRepository.save(empresa);
        
        return ResponseEntity.noContent().build();
    }
}