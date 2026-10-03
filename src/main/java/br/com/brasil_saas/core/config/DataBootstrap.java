package br.com.brasil_saas.core.config;

import br.com.brasil_saas.core.model.Empresa;
import br.com.brasil_saas.core.model.Perfil;
import br.com.brasil_saas.core.model.Usuario;
import br.com.brasil_saas.core.repository.EmpresaRepository;
import br.com.brasil_saas.core.repository.PerfilRepository;
import br.com.brasil_saas.core.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cria empresa + perfil ADMIN + usuário admin/admin123 se não existirem.
 * Executa uma única vez no startup.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataBootstrap implements ApplicationRunner {

    private final EmpresaRepository empresaRepository;
    private final PerfilRepository perfilRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${brasil-saas.bootstrap.admin.enabled:false}")
    private boolean adminBootstrapEnabled;

    @Value("${brasil-saas.bootstrap.admin.username:}")
    private String adminUsername;

    @Value("${brasil-saas.bootstrap.admin.password:}")
    private String adminPassword;

    @Value("${brasil-saas.bootstrap.admin.email:}")
    private String adminEmail;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!adminBootstrapEnabled) {
            log.info("ℹ️  Bootstrap do usuário administrador desabilitado");
            return;
        }

        if (adminUsername == null || adminUsername.isBlank()
                || adminPassword == null || adminPassword.isBlank()
                || adminEmail == null || adminEmail.isBlank()) {
            throw new IllegalStateException(
                    "Bootstrap do administrador habilitado, mas BRASIL_SAAS_BOOTSTRAP_ADMIN_USERNAME, " +
                    "BRASIL_SAAS_BOOTSTRAP_ADMIN_PASSWORD e BRASIL_SAAS_BOOTSTRAP_ADMIN_EMAIL não foram configurados.");
        }

        // 1) Empresa âncora
        Empresa empresa = empresaRepository.findAll().stream().findFirst()
                .orElseGet(() -> {
                    Empresa e = new Empresa();
                    e.setRazaoSocial("SRVCLOUD CONSULTORIA LTDA");
                    e.setNomeFantasia("Brasil SaaS");
                    e.setCnpj("00000000000000");
                    e.setUf("RS");
                    e.setStatus("ATIVO");
                    log.info("🏢 Criando empresa âncora");
                    return empresaRepository.save(e);
                });

        // 2) Perfil ADMIN
        Perfil admin = perfilRepository.findAll().stream()
                .filter(p -> "ADMIN".equals(p.getNome()) && p.getEmpresaId().equals(empresa.getId()))
                .findFirst()
                .orElseGet(() -> {
                    Perfil p = new Perfil();
                    p.setEmpresaId(empresa.getId());
                    p.setNome("ADMIN");
                    p.setDescricao("Administrador com acesso total");
                    log.info("👑 Criando perfil ADMIN");
                    return perfilRepository.save(p);
                });

        // 3) Usuário administrador inicial
        if (usuarioRepository.findByUsernameWithAuthorities(adminUsername).isEmpty()) {
            Usuario u = new Usuario();
            u.setEmpresaId(empresa.getId());
            u.setNome(adminUsername);
            u.setUsername(adminUsername);
            u.setEmail(adminEmail);
            u.setSenhaHash(passwordEncoder.encode(adminPassword));
            u.setAtivo(true);
            u.setMfaHabilitado(false);
            u.setTentativasLogin(0);
            u.getPerfis().add(admin);
            usuarioRepository.save(u);
            log.info("✅ Usuário administrador inicial criado — username: {}", adminUsername);
        } else {
            log.info("ℹ️  Usuário {} já existe — pulando bootstrap", adminUsername);
        }
    }
}
