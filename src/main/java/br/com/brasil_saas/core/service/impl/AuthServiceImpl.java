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
import br.com.brasil_saas.shared.security.PostgresRoleAuthenticationService;
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

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final ModuloAcessoService moduloAcessoService;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;
    private final PostgresRoleAuthenticationService postgresRoleAuthenticationService;
    private final JwtService jwtService;
    private final IdentidadeProperties identidade;
    private final AuthServiceClient authServiceClient;

    @Override
    @Transactional
    public LoginResponse loginBanco(LoginRequest request) {
        String username = request.getUsername().trim();

        Usuario usuario = usuarioRepository.findByUsernameWithAuthorities(username)
                .orElseThrow(() -> new BusinessException("Credenciais inválidas", "INVALID_CREDENTIALS"));

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new BusinessException("Usuário inativo", "USER_INACTIVE");
        }

        if (usuario.getSenhaHash() == null
                || !passwordEncoder.matches(request.getPassword(), usuario.getSenhaHash())) {
            throw new BusinessException("Credenciais inválidas", "INVALID_CREDENTIALS");
        }

        log.info("Login ERP autenticado pela base local: username='{}', provider='POSTGRES'",
                username);

        return buildLoginResponse(usuario, false);
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername().trim();
        String provider = request.getProvider() == null
                ? "AD"
                : request.getProvider().trim().toUpperCase(java.util.Locale.ROOT);

        if ("DB".equals(provider)) {
            // Unmanaged: valida a credencial diretamente na role PostgreSQL.
            if (!postgresRoleAuthenticationService.authenticateSuperuser(username, request.getPassword())) {
                throw new BusinessException("Credenciais do banco inválidas", "INVALID_CREDENTIALS");
            }

            Usuario usuario = ensurePostgresSuperuser(username);
            if (!Boolean.TRUE.equals(usuario.getAtivo())) {
                throw new BusinessException("Usuário inativo", "USER_INACTIVE");
            }

            log.info("Login ERP autenticado diretamente pelo PostgreSQL: username='{}', provider='DB'", username);
            return buildLoginResponse(usuario, true);
        }

        if (!"AD".equals(provider)) {
            throw new BusinessException("Fonte de identidade inválida", "INVALID_AUTH_PROVIDER");
        }

        // Managed: Auth Service/AD continua sendo a autoridade de identidade.
        AuthServiceIdentity identity = authServiceClient.authenticate(username, request.getPassword());
        Usuario usuario = provisionOrUpdateIdentity(identity);

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new BusinessException("Usuário inativo", "USER_INACTIVE");
        }

        log.info("Login ERP autenticado pelo Auth Service/AD: username='{}', provider='{}'",
                identity.username(), identity.provider());

        return buildLoginResponse(usuario, false, identity.groups());
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
        if (usuario.getPerfis().isEmpty() && usuario.getEmpresaId() != null) {
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
        return buildLoginResponse(usuario, false);
    }

    private LoginResponse buildLoginResponse(Usuario usuario, boolean postgresSuperuser) {
        return buildLoginResponse(usuario, postgresSuperuser, java.util.List.of());
    }

    private LoginResponse buildLoginResponse(Usuario usuario, boolean postgresSuperuser, java.util.Collection<String> adGroups) {
        Set<String> authorities = extractAuthorities(usuario);

        if (postgresSuperuser) {
            authorities.add("ROLE_ADMIN");
            authorities.add("ROLE_SUPERADMIN");
        }

        return LoginResponse.builder()
                .accessToken(jwtService.generateToken(usuario.getId(), usuario.getUsername(), usuario.getEmpresaId(), adGroups))
                .refreshToken(jwtService.generateRefreshToken(usuario.getId(), usuario.getUsername(), adGroups))
                .usuarioId(usuario.getId())
                .empresaId(usuario.getEmpresaId())
                .username(usuario.getUsername())
                .authorities(authorities)
                .authSource(postgresSuperuser ? "DB" : "AD")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse refresh(String refreshToken) {
        if (refreshToken == null || !jwtService.validate(refreshToken)) {
            throw new BusinessException("Refresh token inválido", "INVALID_REFRESH_TOKEN");
        }
        String username = jwtService.username(refreshToken);
        Long userId = jwtService.userId(refreshToken);

        Usuario usuario = usuarioRepository.findByUsernameWithAuthorities(username)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado", "USER_NOT_FOUND"));

        if (!usuario.getId().equals(userId)) {
            throw new BusinessException("Refresh token inválido", "INVALID_REFRESH_TOKEN");
        }

        Set<String> authorities = extractAuthorities(usuario);
        java.util.List<String> adGroups = jwtService.adGroups(refreshToken);

        return LoginResponse.builder()
                .accessToken(jwtService.generateToken(usuario.getId(), usuario.getUsername(), usuario.getEmpresaId(), adGroups))
                .refreshToken(jwtService.generateRefreshToken(usuario.getId(), usuario.getUsername(), adGroups))
                .usuarioId(usuario.getId())
                .empresaId(usuario.getEmpresaId())
                .username(usuario.getUsername())
                .authorities(authorities)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse me(Long usuarioId) {
        Usuario usuario = usuarioRepository.findByUsernameWithAuthorities(null)
                .or(() -> usuarioRepository.findById(usuarioId))
                .orElseThrow(() -> new BusinessException("Usuário não encontrado", "USER_NOT_FOUND"));

        if (!usuario.getId().equals(usuarioId)) {
            usuario = usuarioRepository.findByUsernameWithAuthorities(usuario.getUsername())
                    .orElse(usuario);
        }

        Set<String> perfis = usuario.getPerfis().stream()
                .map(Perfil::getNome).collect(Collectors.toSet());

        Set<String> permissoes = resolveEffectivePermissions(usuario);

        String empresaNome = empresaRepository.findById(usuario.getEmpresaId())
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

    /**
     * Provisiona a representação ERP de uma role PostgreSQL SUPERUSER.
     *
     * A senha PostgreSQL não é copiada para senha_hash.
     * O cadastro recebe uma senha BCrypt aleatória apenas para manter
     * a entidade compatível com o modelo ERP. A autenticação posterior
     * continua sendo feita pela role PostgreSQL.
     */
    private Usuario ensurePostgresSuperuser(String username) {
        Usuario existente = usuarioRepository.findByUsernameWithAuthorities(username).orElse(null);

        if (existente != null) {
            ensureAdminProfile(existente);
            return existente;
        }

        // A empresa que nasce com o banco vazio. Estes dados sao os reais da
        // SRVCLOUD CONSULTORIA, e nao um placeholder: a empresa criada aqui
        // recebe o id 1, que e' a empresa dona do certificado A1 e a que o
        // dono administra. Com o placeholder anterior (cnpj 00000000000000,
        // uf RS, fantasia "Brasil SaaS") a instalacao nova nascia com a
        // empresa errada, e o erro so aparecia quando alguem emitia documento
        // fiscal — tarde demais para descobrir.
        //
        // Fonte: dados publicos da Receita Federal, e o proprio certificado
        // ICP-Brasil (CN=...:00000000000191), que concordam com o CNPJ.
        Empresa empresa = empresaRepository.findAll().stream().findFirst().orElseGet(() -> {
            Empresa nova = new Empresa();
            nova.setRazaoSocial("EURIPEDES BATISTA DE PAIVA JUNIOR TECNOLOGIA DA INFORMACAO LTDA");
            nova.setNomeFantasia("SRVCLOUD CONSULTORIA");
            nova.setCnpj("00000000000191");
            nova.setEndereco("RUA PAIS LEME");
            nova.setNumero("215");
            nova.setComplemento("CONJ 1713");
            nova.setBairro("PINHEIROS");
            nova.setCep("05424150");
            nova.setUf("SP");
            nova.setCodigoIbge("3550308");
            nova.setRegimeTributario("SIMPLES");
            nova.setTelefone("4197880145");
            nova.setStatus("ATIVO");
            return empresaRepository.save(nova);
        });

        Perfil admin = findOrCreateProfile(
                empresa, "ADMIN", "Administrador com acesso total", 0);

        Usuario novo = new Usuario();
        novo.setEmpresaId(empresa.getId());
        // O nome e' o proprio nome de usuario. Antes era
        // "PostgreSQL Superuser - " + username, e esse rotulo interno aparecia
        // na tela como se fosse o nome da pessoa. O ERP nao sabe o nome civil de
        // quem entra por este caminho — ele so sabe o nome da conta. Inventar um
        // rotulo e' pior que mostrar o que se sabe.
        novo.setNome(username);
        novo.setUsername(username);
        // Email no formato do UPN do AD (usuario@dominio), nao "@localhost": o
        // dominio vem da configuracao, e o padrao e' o proprio realm do AD.
        novo.setEmail(identidade.emailPara(username));
        novo.setSenhaHash(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
        novo.setAtivo(true);
        novo.setMfaHabilitado(false);
        novo.setTentativasLogin(0);
        novo.getPerfis().add(admin);

        Usuario salvo = usuarioRepository.save(novo);
        log.info("Usuário ERP '{}' provisionado para PostgreSQL SUPERUSER; usuarioId={}",
                username, salvo.getId());
        return salvo;
    }

    private void ensureAdminProfile(Usuario usuario) {
        Empresa empresa = empresaRepository.findById(usuario.getEmpresaId()).orElse(null);
        if (empresa == null) {
            return;
        }

        Perfil admin = findOrCreateProfile(
                empresa, "ADMIN", "Administrador com acesso total", 0);

        if (usuario.getPerfis().stream().noneMatch(p -> admin.getId().equals(p.getId()))) {
            usuario.getPerfis().add(admin);
            usuarioRepository.save(usuario);
        }
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
