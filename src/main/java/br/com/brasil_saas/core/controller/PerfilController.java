package br.com.brasil_saas.core.controller;

import br.com.brasil_saas.core.model.Usuario;
import br.com.brasil_saas.core.repository.UsuarioRepository;
import br.com.brasil_saas.shared.exception.BusinessException;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import br.com.brasil_saas.shared.security.AuthenticatedUser;
import br.com.brasil_saas.shared.web.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Autoatendimento do usuário logado: ver e alterar os próprios dados.
 * Perfis e permissões são gerenciados em /api/superadmin/usuarios.
 */
@RestController
@RequestMapping("/api/core/perfil")
@RequiredArgsConstructor
public class PerfilController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<ApiResponse<MeuPerfilResponse>> meuPerfil(@AuthenticationPrincipal AuthenticatedUser user) {
        Usuario usuario = obter(user);
        return ResponseEntity.ok(ApiResponse.success(MeuPerfilResponse.from(usuario)));
    }

    @PutMapping
    @Transactional
    public ResponseEntity<ApiResponse<MeuPerfilResponse>> atualizar(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody AtualizacaoRequest request) {

        Usuario usuario = obter(user);

        if (request.nome() != null && !request.nome().isBlank()) {
            usuario.setNome(request.nome().trim());
        }
        if (request.email() != null && !request.email().isBlank()) {
            usuario.setEmail(request.email().trim());
        }

        usuarioRepository.save(usuario);
        return ResponseEntity.ok(ApiResponse.success(MeuPerfilResponse.from(usuario)));
    }

    @PutMapping("/senha")
    public ResponseEntity<ApiResponse<Void>> alterarSenha(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody TrocaSenhaRequest request) {

        Usuario usuario = obter(user);

        if (usuario.getSenhaHash() == null || !passwordEncoder.matches(request.senhaAtual(), usuario.getSenhaHash())) {
            throw new BusinessException("Senha atual incorreta");
        }
        if (!request.novaSenha().equals(request.confirmarSenha())) {
            throw new BusinessException("A confirmação não confere com a nova senha");
        }
        if (request.novaSenha().equals(request.senhaAtual())) {
            throw new BusinessException("A nova senha deve ser diferente da atual");
        }

        usuario.setSenhaHash(passwordEncoder.encode(request.novaSenha()));
        // Zera o bloqueio por tentativas, ja que o dono se autenticou com a senha antiga
        usuario.setTentativasLogin(0);
        usuarioRepository.save(usuario);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private Usuario obter(AuthenticatedUser user) {
        return usuarioRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    public record AtualizacaoRequest(@Size(max = 150) String nome, @Size(max = 150) String email) {
    }

    public record TrocaSenhaRequest(
            @NotBlank String senhaAtual,
            @NotBlank @Size(min = 8, max = 100) String novaSenha,
            @NotBlank String confirmarSenha) {
    }

    public record MeuPerfilResponse(
            Long id, String nome, String username, String email,
            Long empresaId, String fotoUrl, List<String> perfis) {

        static MeuPerfilResponse from(Usuario u) {
            List<String> perfis = u.getPerfis() == null ? List.of()
                    : u.getPerfis().stream().map(p -> p.getNome()).sorted().toList();
            return new MeuPerfilResponse(u.getId(), u.getNome(), u.getUsername(), u.getEmail(),
                    u.getEmpresaId(), u.getFotoUrl(), perfis);
        }
    }
}
