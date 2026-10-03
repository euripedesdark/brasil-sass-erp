package br.com.brasil_saas.shared.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Carrega o usuário e as permissões dele de duas fontes.
 *
 * <p>As permissões existentes no Postgres continuam sendo carregadas como antes.
 * Os grupos recebidos do Auth Service são traduzidos somente pelos mapeamentos
 * explícitos de AD abaixo. Um grupo que não esteja no contrato não vira uma
 * authority do Spring Security.
 */
@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final String SQL_USER = """
            SELECT id, username, senha_hash, ativo, empresa_id
              FROM brasil_saas.bc_core_usuario
             WHERE username = ? AND deleted_at IS NULL
            """;

    // Mantida sem alteração: authorities dos perfis/permissões já existentes no ERP.
    private static final String SQL_AUTHORITIES = """
            SELECT DISTINCT 'ROLE_' || pf.nome
              FROM brasil_saas.bc_core_usuario_perfil up
              JOIN brasil_saas.bc_core_perfil pf ON pf.id = up.perfil_id
             WHERE up.usuario_id = ?
            UNION
            SELECT DISTINCT pm.codigo
              FROM brasil_saas.bc_core_usuario_perfil up
              JOIN brasil_saas.bc_core_perfil_permissao pp ON pp.perfil_id = up.perfil_id
              JOIN brasil_saas.bc_core_permissao pm ON pm.id = pp.permissao_id
             WHERE up.usuario_id = ?
            """;

    private static final String SQL_EMPRESA_POR_CNPJ = """
            SELECT id FROM brasil_saas.bc_core_empresa WHERE cnpj = ?
            """;

    private final JdbcTemplate jdbc;

    public CustomUserDetailsService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return loadUserByUsername(username, List.of());
    }

    /**
     * Carrega o usuário usando os grupos que vieram do Auth Service e foram
     * preservados no JWT do ERP. O ERP não consulta o AD diretamente para
     * reconstruir a identidade.
     */
    public AuthenticatedUser loadUserByUsername(String username, Collection<String> authServiceGroups)
            throws UsernameNotFoundException {
        List<AuthenticatedUser> users = jdbc.query(SQL_USER, (rs, i) -> {
            String usuario = rs.getString("username");
            Set<String> authorities = new LinkedHashSet<>(jdbc.queryForList(SQL_AUTHORITIES, String.class,
                    rs.getLong("id"), rs.getLong("id")));

            Long empresaId = rs.getObject("empresa_id", Long.class);

            Set<String> grupos = new LinkedHashSet<>();
            if (authServiceGroups != null) {
                authServiceGroups.stream()
                        .filter(g -> g != null && !g.isBlank())
                        .map(g -> g.trim().toLowerCase(Locale.ROOT))
                        .forEach(grupos::add);
            }

            Long empresaDoAd = empresaDoAd(grupos);
            if (empresaDoAd != null) {
                empresaId = empresaDoAd;
            }

            authorities.addAll(rolesDosGrupos(grupos));
            return new AuthenticatedUser(
                    rs.getLong("id"),
                    usuario,
                    rs.getString("senha_hash"),
                    rs.getBoolean("ativo"),
                    empresaId,
                    authorities.stream().map(SimpleGrantedAuthority::new).toList());
        }, username);

        if (users.isEmpty()) {
            throw new UsernameNotFoundException("Usuário não encontrado: " + username);
        }
        return users.get(0);
    }

    /**
     * Resolve a empresa pelo grupo {@code ERP_EMPRESA_<cnpj>}, ou retorna null
     * para manter a empresa que o cadastro do usuário já possui.
     */
    private Long empresaDoAd(Set<String> grupos) {
        for (String grupo : grupos) {
            if (grupo.startsWith("erp_empresa_")) {
                String cnpj = grupo.substring("erp_empresa_".length());
                List<Long> ids = jdbc.queryForList(SQL_EMPRESA_POR_CNPJ, Long.class, cnpj);
                if (!ids.isEmpty()) {
                    return ids.get(0);
                }
                log.warn("Grupo ERP_EMPRESA_{} aponta para um CNPJ que não existe em bc_core_empresa", cnpj);
            }
        }
        return null;
    }

    /**
     * Mapeamento explícito entre grupos do AD e roles reconhecidas pelo ERP.
     * Não há conversão genérica de nomes: somente estes nove grupos concedem
     * roles.
     */
    private static final Map<String, String> GRUPOS_PARA_ROLES = Map.ofEntries(
            Map.entry("grp_diretoria",   "ROLE_DIRETORIA"),
            Map.entry("grp_gerente",     "ROLE_GERENTE"),
            Map.entry("grp_gestor",      "ROLE_GESTOR"),
            Map.entry("grp_financeiro",  "ROLE_FINANCEIRO"),
            Map.entry("grp_estoque",     "ROLE_ESTOQUE"),
            Map.entry("grp_rh",          "ROLE_RH"),
            Map.entry("grp_vendedor",    "ROLE_VENDEDOR"),
            Map.entry("grp_erp_admin",   "ROLE_ADMIN"),
            Map.entry("grp_superuser",   "ROLE_SUPERUSER")
    );

    /** Traduz somente os grupos explicitamente autorizados nas roles do ERP. */
    private static Set<String> rolesDosGrupos(Set<String> grupos) {
        Set<String> roles = new LinkedHashSet<>();
        for (String grupo : grupos) {
            String role = GRUPOS_PARA_ROLES.get(grupo);
            if (role != null) {
                roles.add(role);
            }
        }
        return roles;
    }

    /**
     * O nome da pessoa no AD, a partir do que ela digitou.
     *
     * <p>No Windows a conta aparece das duas formas: {@code euripedes} pelo
     * Windows, {@code SRVCLOUD\\euripedes} em contexto de rede. O
     * {@code sAMAccountName} do AD é só o nome curto, então o prefixo do
     * domínio é removido antes da consulta.
     */
    public static String nomeNoAd(String username) {
        if (username == null) {
            return null;
        }
        int barra = username.indexOf('\\');
        String nome = barra >= 0 ? username.substring(barra + 1) : username;
        int arroba = nome.indexOf('@');
        if (arroba > 0) {
            nome = nome.substring(0, arroba);
        }
        return nome.trim();
    }
}
