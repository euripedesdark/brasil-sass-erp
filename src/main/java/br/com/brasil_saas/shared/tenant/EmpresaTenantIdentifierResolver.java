package br.com.brasil_saas.shared.tenant;

import java.util.Optional;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import br.com.brasil_saas.shared.security.AuthenticatedUser;

/**
 * De qual empresa e' a requisição que esta rodando.
 *
 * <p><b>O problema que este resolver precisa resolver.</b> A anotacao
 * {@code @TenantId} do Hibernate 6 tem uma exigencia que atrapalha: na subida,
 * ao construir o {@code SessionFactory}, ele exige um identificador de tenant
 * <b>nao nulo</b> e lanca
 *
 * <pre>
 *   HibernateException: SessionFactory configured for multi-tenancy,
 *                       but no tenant identifier specified
 * </pre>
 *
 * e nao consulta o {@link #isRoot} nesse ponto — ele so e' consultado na
 * construcao da query, em {@code QuerySpec} e {@code QueryGroup}. Verificado no
 * bytecode do {@code AbstractSharedSessionContract}: a condicao do erro e'
 * {@code isMultiTenancyEnabled() && getTenantIdentifierValue() == null}.
 *
 * <p>Entao "sem empresa" <b>nao pode ser null</b>, mesmo sendo o estado de quem
 * nao pertence a empresa nenhuma. E' para isso que existe o sentinela
 * {@link #SEM_FILTRO}: um id que nenhuma empresa tem, que o Hibernate aceita na
 * subida, e que o {@link #isRoot} reconhece para nao acrescentar clausula.
 *
 * <p><b>Por que nao um id falso, tipo 0.</b> Funcionaria do mesmo jeito, e
 * seria pior: 0 e' um id que pode existir, e uma empresa com id 0 receberia
 * Writes de todo superuser. {@code -1} nao sai da sequencia, que comeca em 1.
 *
 * <p><b>Por que nao um {@code String} ou um {@code UUID} com o nome
 * "root".</b> Funcionaria tambem, e mudaria o tipo do tenant em todas as 84
 * entidades. {@code Long} casa com {@code empresa_id} e nao obriga conversao.
 *
 * <p><b>Por que a empresa vem do usuario autenticado e nao de um header.</b>
 * Existia um caminho em que o cliente mandava {@code X-Empresa-Id} e o backend
 * acreditava. Isso e' um IDOR de uma linha: trocar o header e' trocar de
 * empresa. Aqui nao ha header, e o {@code empresaId} sai do principal que o
 * {@code JwtAuthenticationFilter} montou a partir do banco — a fonte que nao
 * depende do cliente.
 */
public class EmpresaTenantIdentifierResolver implements CurrentTenantIdentifierResolver<Long> {

    /**
     * O identificador que significa "esta requisicao nao e' de uma empresa".
     *
     * <p>Devolvido em tres casos, todos eles "sem filtro": o superuser, que ve
     * todas as empresas, e a subida da aplicacao, em que nao ha usuario
     * autenticado e o {@code DataBootstrap} precisa rodar sem restricao.
     */
    public static final Long SEM_FILTRO = -1L;

    /**
     * O papel que atravessa empresas.
     *
     * <p>Existe em dois nomes porque as rotas ja aceitavam os dois, e nao ha
     * perfil SUPERADMIN cadastrado — ele nao concede nada a ninguem hoje. Se um
     * dia existir, entra por aqui e nao pelos outros pontos.
     */
    private static final String ROLE_SUPERUSER = "ROLE_SUPERUSER";
    private static final String ROLE_SUPERADMIN = "ROLE_SUPERADMIN";

    @Override
    public Long resolveCurrentTenantIdentifier() {
        return empresaDoUsuarioAutenticado();
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        // A sessao e' STATELESS e o id da empresa esta no banco, entao nao ha
        // sessao de banco para validar entre requisicoes. Confirmar e' o
        // comportamento correto: o que existe para validar ja foi resolvido
        // acima, do principal autenticado.
        return true;
    }

    @Override
    public boolean isRoot(Long tenantIdentifier) {
        // Este e' o metodo que faz o superuser: quando o tenant e' a raiz, o
        // Hibernate nao acrescenta clausula de empresa na query.
        //
        // A sobrescrita nao muda o comportamento — o default do Hibernate trata
        // null como raiz — e existe para a regra ficar no codigo do projeto,
        // onde ela e' testada, em vez de numa implementacao padrao de biblioteca.
        // E' obrigatorio tratar tambem o sentinela, que nao e' null.
        return tenantIdentifier == null || SEM_FILTRO.equals(tenantIdentifier);
    }

    /**
     * A empresa do usuario da requisicao, ou {@link #SEM_FILTRO} quando ele ve
     * todas — ou quando nao ha usuario nenhum.
     *
     * <p>O sentinela cobre tres estados que de outro modo seriam indistinguiveis
     * aqui, e que precisam ser distinguidos la em {@link #empresaDaRequisicao()}:
     * o superuser, o bootstrap e o anonimo. Para o Hibernate os tres sao a mesma
     * coisa — sem filtro — e por isso um so valor resolve.
     */
    private Long empresaDoUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // isAuthenticated() e' true para o AnonymousAuthenticationToken, que o
        // Spring instala quando nao ha token. Sem esta checagem, uma rota
        // liberada por navegador seria tratada como superuser — o filtro
        // desligado e a consulta sem empresa.
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return SEM_FILTRO;
        }

        if (!(auth.getPrincipal() instanceof AuthenticatedUser usuario)) {
            return SEM_FILTRO;
        }

        if (atravessaTodasAsEmpresas(auth)) {
            return SEM_FILTRO;
        }

        return usuario.getEmpresaId() == null ? SEM_FILTRO : usuario.getEmpresaId();
    }

    /**
     * Se o usuario e' superuser.
     *
     * <p>Repete o que {@code UsuarioAdminController.isSuperuser} faz, e e'
     * proposital estar nos dois: a regra do superuser precisa valer em todo
     * lugar, e um segundo ponto de decisao e' um segundo lugar para o filtro ser
     * esquecido. Se um dos dois mudar, os dois mudam.
     */
    private static boolean atravessaTodasAsEmpresas(Authentication auth) {
        return auth.getAuthorities() != null
                && auth.getAuthorities().stream()
                        .anyMatch(a -> ROLE_SUPERUSER.equals(a.getAuthority())
                                || ROLE_SUPERADMIN.equals(a.getAuthority()));
    }

    /**
     * Nome da configuracao do Hibernate que registra este resolver.
     *
     * <p>Sai daqui em vez de repetir a string em varios lugares: o nome da
     * chave e' um dos que mais quebra em silencio quando muda de versao do
     * Hibernate, e um {@code null} silencioso e' o filtro desligado.
     */
    public static String setting() {
        return AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER;
    }

    /**
     * A empresa da requisicao, para o codigo que precisa do id sem passar pelo
     * Hibernate — uma consulta nativa, um insert.
     *
     * <p>Vazio significa "sem empresa definida", que tem tres causas distintas
     * que o chamador pode querer tratar diferente: superuser, usuario sem
     * empresa, e rota publica. Por isso e' {@link Optional} e nao
     * {@code Long}: o sentinela {@code -1} nunca pode escapar daqui, porque o
     * chamador que grave com ele cria linha na empresa "-1".
     */
    public static Optional<Long> empresaDaRequisicao() {
        Long tenant = new EmpresaTenantIdentifierResolver().resolveCurrentTenantIdentifier();
        if (tenant == null || SEM_FILTRO.equals(tenant)) {
            return Optional.empty();
        }
        return Optional.of(tenant);
    }

    /**
     * A empresa para gravar, ou o motivo de nao poder gravar.
     *
     * <p>Um insert sem {@code empresa_id} grava linha que nenhuma empresa ve —
     * dado que existe no banco e que ninguem consegue encontrar, que e' o pior
     * estado possivel para um cadastro. E gravar com o sentinela seria pior:
     * linha na empresa {@code -1}, que nao existe. Por isso, quem grava e'
     * obrigado a dizer de onde vem o id.
     */
    public static Long empresaParaGravar() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthenticatedUser usuario) {
            if (atravessaTodasAsEmpresas(auth)) {
                // Um superuser ve todas as empresas, mas nao pertence a nenhuma.
                // Sem destino declarado, o empresa_id nao tem de quem ser.
                throw new IllegalStateException(
                        "Superuser nao tem empresa propria. Para gravar, informe a empresa "
                                + "destino — um superuser ve todas, mas nao pertence a nenhuma.");
            }
            if (usuario.getEmpresaId() != null) {
                return usuario.getEmpresaId();
            }
            throw new IllegalStateException(
                    "Usuario sem empresa tentando gravar. O fluxo de empresa exige o "
                            + "vinculo antes da primeira gravacao.");
        }
        return null;
    }
}
