package br.com.brasil_saas.core.controller;

import br.com.brasil_saas.core.model.Usuario;
import br.com.brasil_saas.core.repository.UsuarioRepository;
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

@Tag(name = "Core - Foto do Usuário")
@RestController
@RequestMapping("/api/core/usuarios")
@RequiredArgsConstructor
public class UsuarioFotoController {

    private final UsuarioRepository usuarioRepository;
    private final GenericoImagemService imagemService;

    @PostMapping(value = "/{id}/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('core:usuario:escrita')")
    @Operation(summary = "Atualizar foto do perfil do usuário")
    public ResponseEntity<ImagemDto> atualizarFoto(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser usuarioAutenticado,
            @RequestParam("foto") MultipartFile arquivo) throws IOException {
        
        // O id do caminho e conferido contra o do token: sem isso, qualquer
        // usuario autenticado trocaria a foto de outro.
        if (!usuarioAutenticado.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Optional<Usuario> optionalUsuario = usuarioRepository.findById(usuarioAutenticado.getId());
        if (optionalUsuario.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Usuario usuario = optionalUsuario.get();
        
        // Salvar imagem no MongoDB
        ImagemDocumento imagemDoc = imagemService.salvarImagem(
            usuarioAutenticado.getEmpresaId(), 
            "usuario_foto", 
            usuario.getId(), 
            arquivo
        );
        
        // Atualizar dados da imagem no usuário
        usuario.setFotoUrl("/api/core/usuarios/" + usuario.getId() + "/foto"); // Caminho virtual para recuperação
        usuario.setFotoTipoConteudo(imagemDoc.getContentType());
        usuario.setFotoTamanho(imagemDoc.getTamanho());
        
        // Salvar usuário
        usuario = usuarioRepository.save(usuario);
        
        ImagemDto imagemDto = new ImagemDto();
        imagemDto.setUrl(usuario.getFotoUrl());
        imagemDto.setTipoConteudo(usuario.getFotoTipoConteudo());
        imagemDto.setTamanho(usuario.getFotoTamanho());
        
        return ResponseEntity.ok(imagemDto);
    }

    @GetMapping("/{id}/foto")
    @PreAuthorize("hasAuthority('core:usuario:leitura')")
    @Operation(summary = "Obter foto do usuário")
    public ResponseEntity<byte[]> obterFoto(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser usuarioAutenticado) {
        
        // Verificar se o usuário está acessando sua própria foto ou tem permissão adequada
        if (!id.equals(usuarioAutenticado.getId())) {
            // Se não for o próprio usuário, verificar se tem permissão de leitura de outros usuários
            // Por padrão, vamos permitir apenas acesso à própria foto
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ImagemDocumento imagemDoc = imagemService.buscarImagem(usuarioAutenticado.getEmpresaId(), "usuario_foto", id);
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

    @DeleteMapping("/{id}/foto")
    @PreAuthorize("hasAuthority('core:usuario:escrita')")
    @Operation(summary = "Remover foto do perfil do usuário")
    public ResponseEntity<Void> removerFoto(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser usuarioAutenticado) {
        
        // O id do caminho e conferido contra o do token: sem isso, qualquer
        // usuario autenticado trocaria a foto de outro.
        if (!usuarioAutenticado.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Optional<Usuario> optionalUsuario = usuarioRepository.findById(usuarioAutenticado.getId());
        if (optionalUsuario.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Usuario usuario = optionalUsuario.get();
        
        // Remover imagem do MongoDB
        imagemService.removerImagem(usuarioAutenticado.getEmpresaId(), "usuario_foto", usuario.getId());
        
        usuario.setFotoUrl(null);
        usuario.setFotoTipoConteudo(null);
        usuario.setFotoTamanho(null);
        
        usuarioRepository.save(usuario);
        
        return ResponseEntity.noContent().build();
    }
}