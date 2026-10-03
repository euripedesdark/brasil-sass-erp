package br.com.brasil_saas.shared.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * Principal autenticado com id e empresaId para auditoria e multi-tenancy.
 */
@Getter
public class AuthenticatedUser extends User {

    private final Long id;
    private final Long empresaId;

    public AuthenticatedUser(Long id, String username, String password, boolean enabled,
                             Long empresaId, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, true, true, true, authorities);
        this.id = id;
        this.empresaId = empresaId;
    }
}
