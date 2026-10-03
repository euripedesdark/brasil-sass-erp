package br.com.brasil_saas.core.service.impl;

import br.com.brasil_saas.core.model.Perfil;
import br.com.brasil_saas.core.model.Usuario;
import br.com.brasil_saas.core.repository.PerfilRepository;
import br.com.brasil_saas.core.repository.UsuarioRepository;
import br.com.brasil_saas.core.service.PermissionService;
import br.com.brasil_saas.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;

    @Override
    @Transactional
    public void atribuirPerfil(Usuario usuario, Perfil perfil, Usuario executor) {
        validarHierarquia(usuario, perfil, executor);

        Usuario updatedUser = usuarioRepository.findById(usuario.getId()).orElseThrow();
        updatedUser.getPerfis().add(perfil);
        usuarioRepository.save(updatedUser);
    }

    @Override
    @Transactional
    public void removerPerfil(Usuario usuario, Perfil perfil, Usuario executor) {
        validarHierarquia(usuario, perfil, executor);

        Usuario updatedUser = usuarioRepository.findById(usuario.getId()).orElseThrow();
        updatedUser.getPerfis().remove(perfil);
        usuarioRepository.save(updatedUser);
    }

    private void validarHierarquia(Usuario usuario, Perfil perfil, Usuario executor) {
        // Pega o nível mais alto do executor
        int executorNivel = executor.getPerfis().stream()
                .mapToInt(Perfil::getHierarquiaNivel)
                .min()
                .orElse(Integer.MAX_VALUE);

        // Pega o nível mais alto do usuário alvo
        int alvoNivel = usuario.getPerfis().stream()
                .mapToInt(Perfil::getHierarquiaNivel)
                .min()
                .orElse(Integer.MAX_VALUE);

        // Regra:
        // Administrador (0) -> muda tudo
        // Diretoria (1) -> muda tudo exceto Administrador (0)
        // Gerente (2) -> muda tudo exceto Diretoria (1) e Administrador (0)

        if (executorNivel > alvoNivel) {
            throw new BusinessException("Você não tem permissão para alterar usuários de nível superior.");
        }

        // Verificação específica para o perfil que está sendo atribuído
        if (perfil.getHierarquiaNivel() != null && perfil.getHierarquiaNivel() < executorNivel) {
            throw new BusinessException("Você não pode atribuir um perfil de nível superior ao seu próprio.");
        }
    }
}
