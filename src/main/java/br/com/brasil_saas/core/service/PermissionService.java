package br.com.brasil_saas.core.service;

import br.com.brasil_saas.core.model.Perfil;
import br.com.brasil_saas.core.model.Usuario;
import br.com.brasil_saas.shared.exception.BusinessException;

public interface PermissionService {
    void atribuirPerfil(Usuario usuario, Perfil perfil, Usuario executor);
    void removerPerfil(Usuario usuario, Perfil perfil, Usuario executor);
}
