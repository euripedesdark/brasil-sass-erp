package br.com.brasil_saas.core.service.impl;

import br.com.brasil_saas.core.model.Empresa;
import br.com.brasil_saas.core.model.Perfil;
import br.com.brasil_saas.core.model.Modulo;
import br.com.brasil_saas.core.model.Permissao;
import br.com.brasil_saas.core.model.Usuario;
import br.com.brasil_saas.core.config.IdentidadeProperties;
import br.com.brasil_saas.core.repository.EmpresaRepository;
import br.com.brasil_saas.core.repository.PerfilRepository;
import br.com.brasil_saas.core.repository.UsuarioRepository;
import br.com.brasil_saas.core.service.AuthService;
import br.com.brasil_saas.core.service.ModuloAcessoService;
import br.com.brasil_saas.core.service.dto.LoginRequest;
import br.com.brasil_saas.core.service.dto.LoginResponse;
import br.com.brasil_saas.core.service.dto.UserProfileResponse;
import br.com.brasil_saas.shared.exception.BusinessException;
import br.com.brasil_saas.shared.security.AuthServiceClient;
import br.com.brasil_saas.shared.security.AuthServiceIdentity;
import br.com.brasil_saas.shared.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    /**
     * Grupos do AD que concedem SUPERUSER no ERP. Deliberadamente NAO inclui
     * "Domain Users": esse grupo contem todos os usuarios do dominio e
     * transformaria qualquer conta em superuser. "postgres_superuser" segue
     * valendo porque e conta de servico da propria base.
     */
    private static final java.util.Set<String> GRUPOS_ADMINISTRADOR = java.util.Set.of(
            "administrators",
            "domain admins",
            "schema admins",
            "enterprise admins",
            "postgres_superuser");

    private static final String PERFIL_SUPERUSER = "SUPERUSER";

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final ModuloAcessoService moduloAcessoService;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final IdentidadeProperties identidade;
    private final AuthServiceClient authServiceClient;

    @Override
    @Transactional
    public LoginResponse loginBanco(LoginRequest request) {
        return loginComProvider(request, "POSTGRES");
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        String provider = request.getProvider() == null ? "AD"
                : request.getProvider().trim().toUpperCase(java.util.Locale.ROOT);
        if ("DB".equals(provider)) provider = "POSTGRES";
        return loginComProvider(request, provider);
    }

    private LoginResponse loginComProvider(LoginRequest request, String provider) {
        if (!"AD".equals(provider) && !"POSTGRES".equals(provider)) {
            throw new BusinessException("Fonte de identidade inválida", "INVALID_AUTH_PROVIDER");
        }
        AuthServiceIdentity identity = authServiceClient.authenticate(
                request.getUsername().trim(), request.getPassword(), provider);
        Usuario usuario = provisionOrUpdateIdentity(identity);
        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new BusinessException("Usuário inativo", "USER_INACTIVE");
        }
        log.info("Login ERP autenticado pelo IAM: username='{}', provider='{}'",
                identity.username(), identity.provider());
        return buildLoginResponse(usuario, identity.groups(), identity.provider());
    }

    /**
     * Mantém no PostgreSQL apenas a representação ERP da identidade.
     * A senha do AD nunca é armazenada nem comparada pelo ERP.
     */
    private Usuario provisionOrUpdateIdentity(AuthServiceIdentity identity) {
        String username = identity.username().trim();
        Usuario usuario = usuarioRepository.findByUsernameWithAuthorities(username).orElse(null);

        if (usuario == null) {
            usuario = new Usuario();
            usuario.setUsername(username);
            usuario.setNome(username);
            // A coluna permanece NOT NULL por compatibilidade com o modelo
            // legado, mas este hash aleatório NÃO é uma credencial do AD e
            // nunca participa da autenticação. A autoridade de senha é o
            // Auth Service.
            usuario.setSenhaHash(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
            usuario.setAtivo(true);
            usuario.setMfaHabilitado(false);
            usuario.setTentativasLogin(0);
        }

        usuario.setEmail(identidade.emailPara(username));

        Empresa empresa = empresaDaIdentidade(identity.groups(), usuario.getEmpresaId());
        if (empresa != null) {
            usuario.setEmpresaId(empresa.getId());
        }

        // Grupo AD NÃO cria perfil. Perfil é autorização ERP e determina
        // quais módulos o usuário pode abrir. Os grupos ERP_MODULO_* do AD
        // serão usados separadamente para determinar gravação.
        boolean administradorDoDominio = identity.groups() != null && identity.groups().stream()
                .filter(java.util.Objects::nonNull)
                .map(g -> g.trim().toLowerCase(java.util.Locale.ROOT))
                .anyMatch(GRUPOS_ADMINISTRADOR::contains);

        if (administradorDoDominio) {
            // Administrador do AD e SUPERUSER no ERP, resolvido aqui e nao por
            // insercao manual: qualquer conta que o administrador do dominio
            // criar entra com SUPERUSER no primeiro login, sem passo previo.
            // Concessao apenas: a revogacao e manual, na tela de administracao.
            // Remocao automatica nao existe porque o perfil concedido e o
            // mesmo objeto do atribuido manualmente, sem como distinguir.
            Perfil superuser = perfilRepository.findAll().stream()
                    .filter(p -> PERFIL_SUPERUSER.equalsIgnoreCase(p.getNome()))
                    .findFirst()
                    .orElse(null);
            if (superuser != null && usuario.getPerfis().stream()
                    .noneMatch(atual -> superuser.getId().equals(atual.getId()))) {
                usuario.getPerfis().add(superuser);
            }
        } else if (usuario.getPerfis().isEmpty() && usuario.getEmpresaId() != null) {
            Perfil usuarioPerfil = findOrCreateProfile(
                    empresaRepository.findById(usuario.getEmpresaId()).orElseThrow(),
                    "USUARIO",
                    "Usuário autenticado pelo Active Directory",
                    100);
            usuario.getPerfis().add(usuarioPerfil);
        }

        return usuarioRepository.save(usuario);
    }

    private Empresa empresaDaIdentidade(java.util.List<String> grupos, Long empresaAtual) {
        if (grupos != null) {
            for (String grupo : grupos) {
                if (grupo == null) continue;
                String normalizado = grupo.trim().toUpperCase(java.util.Locale.ROOT);
                if (normalizado.startsWith("ERP_EMPRESA_")) {
                    String cnpj = normalizado.substring("ERP_EMPRESA_".length());
                    if (!cnpj.isBlank()) {
                        Empresa empresa = empresaRepository.findByCnpj(cnpj).orElse(null);
                        if (empresa != null) return empresa;
                        log.warn("Grupo AD '{}' aponta para CNPJ '{}' inexistente no ERP", grupo, cnpj);
                    }
                }
            }
        }
        return empresaAtual == null ? null : empresaRepository.findById(empresaAtual).orElse(null);
    }


    /**
     * Entrada por tíquete: a senha não participa, porque a identidade já foi
     * provada no {@code SpnegoService} contra o keytab.
     *
     * <p>Delega para o mesmo {@code buildLoginResponse} do login por senha, de
     * propósito: o token sai igual, o tenant sai igual, e o
     * {@code CustomUserDetailsService} continua somando as permissões do perfil
     * com as do grupo do AD exatamente como nos outros caminhos. Se o SPNEGO
     * montasse a resposta por conta própria, haveria dois jeitos de emitir sessão
     * — e é assim que um atalho vira um segundo sistema de acesso.
     */
    @Override
    @Transactional
    public LoginResponse loginPorPrincipal(String principal) {
        if (principal == null || principal.isBlank()) {
            throw new BusinessException("Identidade não informada", "INVALID_CREDENTIALS");
        }

        String username = principal.trim();
        Usuario usuario = usuarioRepository.findByUsernameWithAuthorities(username).orElse(null);

        if (usuario == null) {
            log.warn("SPNEGO autenticou '{}' no AD, mas não há usuário ERP com esse nome", username);
            throw new BusinessException("Usuário não encontrado: " + username, "USER_NOT_FOUND");
        }

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new BusinessException("Usuário inativo", "USER_INACTIVE");
        }

        log.info("Login ERP autenticado por tíquete SPNEGO: username='{}'", username);
        return buildLoginResponse(usuario, java.util.List.of(), "AD");
    }

    private LoginResponse buildLoginResponse(Usuario usuario,
            java.util.Collection<String> adGroups, String provider) {
        Set<String> authorities = extractAuthorities(usuario);
        authorities.addAll(br.com.brasil_saas.shared.security.CustomUserDetailsService.authoritiesDosGrupos(adGroups));

        return LoginResponse.builder()
                .accessToken(jwtService.generateToken(usuario.getId(), usuario.getUsername(), usuario.getEmpresaId(), adGroups, provider))
                .refreshToken(jwtService.generateRefreshToken(usuario.getId(), usuario.getUsername(), adGroups, provider))
                .usuarioId(usuario.getId())
                .empresaId(usuario.getEmpresaId())
                .username(usuario.getUsername())
                .authorities(authorities)
                .authSource(provider)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse refresh(String refreshToken) {
        if (refreshToken == null || !jwtService.validate(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
            throw new BusinessException("Refresh token inválido", "INVALID_REFRESH_TOKEN");
        }
        String username = jwtService.username(refreshToken);
        Long userId = jwtService.userId(refreshToken);

        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado", "USER_NOT_FOUND"));
        if (!usuario.getUsername().equals(username)) {
            throw new BusinessException("Refresh token inválido", "INVALID_REFRESH_TOKEN");
        }

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new BusinessException("Usuário inativo", "USER_INACTIVE");
        }

        if (!usuario.getId().equals(userId)) {
            throw new BusinessException("Refresh token inválido", "INVALID_REFRESH_TOKEN");
        }

        Set<String> authorities = extractAuthorities(usuario);
        String provider = jwtService.authProvider(refreshToken);
        java.util.List<String> adGroups = jwtService.adGroups(refreshToken);
        authorities.addAll(br.com.brasil_saas.shared.security.CustomUserDetailsService.authoritiesDosGrupos(adGroups));

        return LoginResponse.builder()
                .accessToken(jwtService.generateToken(usuario.getId(), usuario.getUsername(), usuario.getEmpresaId(), adGroups, provider))
                .refreshToken(jwtService.generateRefreshToken(usuario.getId(), usuario.getUsername(), adGroups, provider))
                .usuarioId(usuario.getId())
                .empresaId(usuario.getEmpresaId())
                .username(usuario.getUsername())
                .authorities(authorities)
                .authSource(provider)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse me(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado", "USER_NOT_FOUND"));

        Set<String> perfis = usuario.getPerfis().stream()
                .map(Perfil::getNome).collect(Collectors.toSet());

        Set<String> permissoes = resolveEffectivePermissions(usuario);

        // Usuario sem empresa ainda (primeiro login, cadastro pendente): nao ha
        // o que resolver. findById(null) lanca IllegalArgumentException, que o
        // handler devolveria como 500 — e este e' exatamente o estado que a tela
        // de configuracao precisa enxergar para obrigar o cadastro.
        String empresaNome = usuario.getEmpresaId() == null
                ? null
                : empresaRepository.findById(usuario.getEmpresaId())
                        .map(e -> e.getNomeFantasia() != null ? e.getNomeFantasia() : e.getRazaoSocial())
                        .orElse(null);

        // Modulos liberados: o menu lateral e montado com isso no frontend.
        final Long idUsuario = usuario.getId();
        Set<String> modulos = moduloAcessoService.modulosDoUsuario(idUsuario).stream()
                .map(Modulo::getChave)
                .collect(Collectors.toSet());

        Set<String> somenteLeitura = modulos.stream()
                .filter(chave -> moduloAcessoService.somenteLeitura(idUsuario, chave))
                .collect(Collectors.toSet());

        return UserProfileResponse.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .username(usuario.getUsername())
                .email(usuario.getEmail())
                .empresaId(usuario.getEmpresaId())
                .empresaNome(empresaNome)
                .perfis(perfis)
                .permissoes(permissoes)
                .modulos(modulos)
                .modulosSomenteLeitura(somenteLeitura)
                .build();
    }

    private Perfil findOrCreateProfile(
            Empresa empresa, String nome, String descricao, int hierarquiaNivel) {

        return perfilRepository.findAll().stream()
                .filter(p -> nome.equalsIgnoreCase(p.getNome())
                        && empresa.getId().equals(p.getEmpresaId()))
                .findFirst()
                .orElseGet(() -> {
                    Perfil p = new Perfil();
                    p.setEmpresaId(empresa.getId());
                    p.setNome(nome);
                    p.setDescricao(descricao);
                    p.setHierarquiaNivel(hierarquiaNivel);
                    return perfilRepository.save(p);
                });
    }

    private Set<String> extractAuthorities(Usuario usuario) {
        Set<String> roles = usuario.getPerfis().stream()
                .map(p -> "ROLE_" + p.getNome())
                .collect(Collectors.toSet());

        roles.addAll(resolveEffectivePermissions(usuario));
        return roles;
    }

    private Set<String> resolveEffectivePermissions(Usuario usuario) {
        Set<String> permissions = new java.util.HashSet<>();
        Set<Long> visitedProfiles = new java.util.HashSet<>();

        for (Perfil perfil : usuario.getPerfis()) {
            collectInheritedPermissions(perfil, permissions, visitedProfiles);
        }

        return permissions;
    }

    private void collectInheritedPermissions(
            Perfil perfil,
            Set<String> permissions,
            Set<Long> visitedProfiles) {

        if (perfil == null) {
            return;
        }

        Long perfilId = perfil.getId();
        if (perfilId != null && !visitedProfiles.add(perfilId)) {
            throw new IllegalStateException(
                    "Ciclo detectado na herança de perfis envolvendo o perfil " + perfil.getNome());
        }

        perfil.getPermissoes().stream()
                .map(Permissao::getCodigo)
                .filter(java.util.Objects::nonNull)
                .forEach(permissions::add);

        collectInheritedPermissions(perfil.getPerfilPai(), permissions, visitedProfiles);

        if (perfilId != null) {
            visitedProfiles.remove(perfilId);
        }
    }
}
