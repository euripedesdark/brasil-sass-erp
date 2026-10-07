package br.com.brasil_saas.shared.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Decorre da diretiva do dono (06/10):
 *
 * <ul>
 *   <li>todo administrador do AD (Administrators ou Domain Admins) e SUPERUSER
 *       e fica <b>acima das roles</b> do sistema;</li>
 *   <li>DIRETORIA, GERENTE e GESTOR gravam em <b>todos os modulos</b> da
 *       propria empresa;</li>
 *   <li>os demais dependem da role/perfil que ja vem do banco.</li>
 * </ul>
 *
 * <p>O Spring Security nao permite sobrescrever {@code hasAuthority}/{@code hasRole}
 * (sao {@code final}) e o {@code getAuthoritySet} e privado, mas a avaliacao de
 * qualquer expressao passa pelas authorities expandidas pelo {@link RoleHierarchy}
 * antes de responder. E por aqui que o acesso extra entra, sem tocar em nenhum
 * dos ~450 {@code @PreAuthorize} do projeto.
 *
 * <p>A lista de autoridades que o codigo exige e <b>descoberta do proprio
 * codigo</b> na subida: varre as beans em busca de {@code @PreAuthorize} e
 * recolhe as roles e os codigos de permissao pedidos. Assim, uma permissao
 * nova que nascer depois ja nasce coberta — quem define e o codigo, nao um
 * cadastro.
 */
@Slf4j
@Component
public class HierarquiaDeAcesso implements RoleHierarchy {

    private static final String ROLE_SUPERUSER = "ROLE_SUPERUSER";
    private static final String ROLE_SUPERADMIN = "ROLE_SUPERADMIN";

    /** Funcoes que gravam em todos os modulos da propria empresa. */
    private static final Set<String> GRAVAM_TODOS_OS_MODULOS =
            Set.of("ROLE_DIRETORIA", "ROLE_GERENTE", "ROLE_GESTOR");

    /** {@code has(Any)?(Authority|Role)('...', ...)} — as chamadas do guard. */
    private static final Pattern CHAMADA =
            Pattern.compile("has(?:Any)?(?:Authority|Role)\\s*\\(([^)]*)\\)");
    private static final Pattern LITERAL =
            Pattern.compile("'([^']*)'|\"([^\"]*)\"");

    /** Tudo que o codigo exige: roles ({@code ROLE_*}) e codigos de modulo. */
    private final Set<String> tudoQueOCodigoExige;
    /** Somente codigos de modulo ({@code modulo:recurso:acao}), p/ diretores. */
    private final Set<String> codigosDeModulo;

    public HierarquiaDeAcesso(ApplicationContext contexto) {
        Set<String> tudo = new LinkedHashSet<>();
        Set<String> modulos = new LinkedHashSet<>();
        for (String nome : contexto.getBeanDefinitionNames()) {
            Class<?> tipo;
            try {
                tipo = contexto.getType(nome);
            } catch (RuntimeException inofensivo) {
                continue;
            }
            if (tipo == null) {
                continue;
            }
            coletar(tipo.getAnnotation(PreAuthorize.class), tudo, modulos);
            for (Method metodo : tipo.getMethods()) {
                coletar(metodo.getAnnotation(PreAuthorize.class), tudo, modulos);
            }
        }
        this.tudoQueOCodigoExige = Set.copyOf(tudo);
        this.codigosDeModulo = Set.copyOf(modulos);
        log.info("HierarquiaDeAcesso: {} autoridades exigidas pelo codigo ({} codigos de modulo)",
                tudo.size(), modulos.size());
    }

    private void coletar(PreAuthorize guarda, Set<String> tudo, Set<String> modulos) {
        if (guarda == null) {
            return;
        }
        Matcher chamada = CHAMADA.matcher(guarda.value());
        while (chamada.find()) {
            boolean pedeRole = chamada.group().contains("Role");
            Matcher valor = LITERAL.matcher(chamada.group(1));
            while (valor.find()) {
                String autoridade = valor.group(1) != null ? valor.group(1) : valor.group(2);
                if (autoridade == null || autoridade.isBlank()) {
                    continue;
                }
                if (pedeRole && !autoridade.startsWith("ROLE_")) {
                    autoridade = "ROLE_" + autoridade;
                }
                tudo.add(autoridade);
                if (autoridade.indexOf(':') >= 0) {
                    modulos.add(autoridade);
                }
            }
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getReachableGrantedAuthorities(
            Collection<? extends GrantedAuthority> authorities) {
        Set<String> acessiveis = new LinkedHashSet<>();
        if (authorities != null) {
            for (GrantedAuthority a : authorities) {
                if (a != null && a.getAuthority() != null) {
                    acessiveis.add(a.getAuthority());
                }
            }
        }
        if (acessiveis.contains(ROLE_SUPERUSER) || acessiveis.contains(ROLE_SUPERADMIN)) {
            acessiveis.addAll(tudoQueOCodigoExige);
        } else if (acessiveis.stream().anyMatch(GRAVAM_TODOS_OS_MODULOS::contains)) {
            acessiveis.addAll(codigosDeModulo);
        }
        return acessiveis.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }
}