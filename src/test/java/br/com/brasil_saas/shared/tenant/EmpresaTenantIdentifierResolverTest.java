package br.com.brasil_saas.shared.tenant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import br.com.brasil_saas.shared.security.AuthenticatedUser;

/**
 * A regra de "quem ve o que", testada sem subir banco nem container.
 *
 * <p><b>O que este teste existe para provar.</b> O isolamento por empresa nao
 * pode depender de o filtro "parecer" que funciona. Um filtro de tenant
 * desligado nao da erro: a aplicacao sobe, as consultas rodam, e todo mundo ve
 * tudo. O sintoma aparece como "o diretor ta vendo a empresa dos outros", que se
 * diagnostica como bug de dado.
 *
 * <p>Por isso os testes verificam a <b>decisao</b> — o que o resolver devolve
 * para cada caso — e nao apenas que a classe compila. O "nao aplica filtro" e'
 * represented por {@code null}, e {@code null} tambem e' o que o Hibernate
 * entende como raiz. Essa equivalencia e' o que sustenta o superuser, e e' o
 * primeiro lugar a quebrar se alguem mudar o resolver.
 */
class EmpresaTenantIdentifierResolverTest {

    private final EmpresaTenantIdentifierResolver resolver = new EmpresaTenantIdentifierResolver();

    @BeforeEach
    @AfterEach
    void semContexto() {
        SecurityContextHolder.clearContext();
    }

    private static AuthenticatedUser usuario(Long id, String username, Long empresaId,
                                            String... papeis) {
        // O construtor real exige o enabled: false nao, o usuario esta ativo.
        // Passar false aqui faria o Spring recusar a autenticacao num teste de
        // integracao, e o motivo seria o construtor e nao a regra.
        return new AuthenticatedUser(id, username, "senha-ignorada", true, empresaId,
                List.of(papeis).stream()
                        .map(p -> new SimpleGrantedAuthority("ROLE_" + p))
                        .toList());
    }

    private void autenticar(AuthenticatedUser usuario) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities()));
    }

    @Test
    @DisplayName("um diretor com empresa filtra pela empresa DELA")
    void diretorComEmpresaVeSoAPropria() {
        autenticar(usuario(50L, "diretor.acme", 7L, "DIRETORIA"));

        assertEquals(7L, resolver.resolveCurrentTenantIdentifier(),
                "um diretor tem de ver os cadastros da empresa a que foi associado");
        assertFalse(resolver.isRoot(7L),
                "com empresa, nao e' raiz — o filtro tem de entrar na query");
        assertEquals(7L, EmpresaTenantIdentifierResolver.empresaDaRequisicao().orElseThrow(),
                "e o id que ele grava e' o da empresa dele");
    }

    @Test
    @DisplayName("o superuser ve todas: o sentinela, que e' raiz")
    void superuserVeTodas() {
        autenticar(usuario(4L, "sysdba", 1L, "SUPERUSER"));

        Long tenant = resolver.resolveCurrentTenantIdentifier();
        // Nao e' null: o Hibernate lanca na subida se o tenant vier nulo, e nao
        // consulta o isRoot nesse ponto. E' o sentinela que resolve os dois.
        assertEquals(EmpresaTenantIdentifierResolver.SEM_FILTRO, tenant,
                "o superuser nao e' de empresa nenhuma");
        assertTrue(resolver.isRoot(tenant),
                "e' isso que faz a query sair sem clausula de empresa");
        assertTrue(EmpresaTenantIdentifierResolver.empresaDaRequisicao().isEmpty(),
                "o sentinela NUNCA pode escapar como id de empresa: gravar com ele "
                        + "criaria linha na empresa -1");
    }

    @Test
    @DisplayName("o sentinela nunca vaza como empresa, em nenhum estado sem empresa")
    void sentinelaNuncaVaza() {
        // Os tres estados que o Hibernate trata igual precisam continuar
        // distinguiveis para quem grava.
        autenticar(usuario(4L, "sysdba", 1L, "SUPERUSER"));
        assertTrue(EmpresaTenantIdentifierResolver.empresaDaRequisicao().isEmpty(), "superuser");

        autenticar(usuario(60L, "sem.empresa", null, "VENDEDOR"));
        assertTrue(EmpresaTenantIdentifierResolver.empresaDaRequisicao().isEmpty(), "sem empresa");

        SecurityContextHolder.clearContext();
        assertTrue(EmpresaTenantIdentifierResolver.empresaDaRequisicao().isEmpty(), "sem autenticacao");
    }

    @Test
    @DisplayName("SUPERUSER sem empresa tambem ve todas")
    void superuserSemEmpresaTambemVeTodas() {
        // O dono definiu que o superuser pode estar associado a uma ou mais
        // empresas, ou a nenhuma. Nos dois casos ele ve tudo, entao a empresa
        // dele — quando existe — nao pode estreitar o filtro.
        autenticar(usuario(4L, "sysdba", null, "SUPERUSER"));

        assertEquals(EmpresaTenantIdentifierResolver.SEM_FILTRO,
                resolver.resolveCurrentTenantIdentifier());
        assertTrue(resolver.isRoot(resolver.resolveCurrentTenantIdentifier()));
    }

    @Test
    @DisplayName("SUPERADMIN tambem atravessa, porque as rotas ja o aceitavam")
    void superadminTambemAtravessa() {
        // Nao ha perfil SUPERADMIN cadastrado, entao isto nao concede acesso a
        // ninguem hoje. O teste existe para que, se o perfil um dia existir, ele
        // entre pelo mesmo caminho do SUPERUSER e nao por um segundo lugar.
        autenticar(usuario(5L, "superadmin.futuro", 3L, "SUPERADMIN"));

        assertEquals(EmpresaTenantIdentifierResolver.SEM_FILTRO,
                resolver.resolveCurrentTenantIdentifier());
        assertTrue(resolver.isRoot(resolver.resolveCurrentTenantIdentifier()));
    }

    @Test
    @DisplayName("ADMIN puro ja nao existe, e se voltasse nao atravessaria")
    void adminPuroNaoAtravessa() {
        // O perfil ADMIN foi eliminado na V104. Este teste fixa a fronteira:
        // o filtro so e' desligado por SUPERUSER, e nao por "papel de
        // administracao". Se alguem reintroduzir o ADMIN, este teste falha.
        autenticar(usuario(11L, "antigo.admin", 2L, "ADMIN"));

        assertEquals(2L, resolver.resolveCurrentTenantIdentifier(),
                "ADMIN — ou qualquer papel que nao seja SUPERUSER — fica preso a empresa");
    }

    @Test
    @DisplayName("usuario sem empresa: nao ve nada, e nao quebra")
    void usuarioSemEmpresaNaoQuebra() {
        // O fluxo de empresa exige o vinculo antes da primeira gravacao. Enquanto
        // ele nao existe, /configurar-empresa e /perfil continuam liberadas sem
        // empresa — mas os dados das outras empresas ficam atras de um filtro que
        // nao casa com nenhuma linha. "Faz login e nao ve nada" e' o que
        // EmpresaDoUsuarioService documenta, e e' o estado que obriga o cadastro.
        autenticar(usuario(60L, "sem.empresa", null, "VENDEDOR"));

        Long tenant = resolver.resolveCurrentTenantIdentifier();
        assertEquals(EmpresaTenantIdentifierResolver.SEM_EMPRESA, tenant,
                "sem empresa: um tenant que nenhuma linha tem. O Hibernate exige um "
                        + "valor nao nulo na subida, mas nao pode ser um que exista");
        assertFalse(resolver.isRoot(tenant),
                "nao e' raiz: raiz sairia sem WHERE empresa_id, e um usuario novo "
                        + "do dominio leria as empresas dos outros antes de cadastrar a dele");
    }

    @Test
    @DisplayName("sem autenticacao: sentinela, e nunca 'aconteceu de passar'")
    void semAutenticacaoNaoVaza() {
        assertEquals(EmpresaTenantIdentifierResolver.SEM_FILTRO,
                resolver.resolveCurrentTenantIdentifier());

        // O AnonymousAuthenticationToken conta como autenticado para o Spring
        // (ele instala um em qualquer rota liberada). Sem este teste, uma rota
        // liberada por navegador treated como superuser — sem filtro, sem
        // empresa, e com resposta 200.
        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken("key", "anonymous",
                        AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

        assertEquals(EmpresaTenantIdentifierResolver.SEM_FILTRO,
                resolver.resolveCurrentTenantIdentifier());
    }

    @Test
    @DisplayName("gravar sem empresa e' recusado com o motivo")
    void gravarSemEmpresaERecusado() {
        autenticar(usuario(60L, "sem.empresa", null, "VENDEDOR"));

        IllegalStateException erro = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class,
                () -> EmpresaTenantIdentifierResolver.empresaParaGravar());

        assertTrue(erro.getMessage().contains("sem empresa"),
                "a mensagem tem que dizer o que falta: " + erro.getMessage());
    }

    @Test
    @DisplayName("o superuser nao grava em nome de ninguem por padrao")
    void superuserGravarExigeEmpresaDestino() {
        // Ele ve todas as empresas, mas nao pertence a nenhuma. Sem destino
        // declarado, o empresa_id sairia nulo — linha que existe e que nenhuma
        // empresa consegue encontrar.
        autenticar(usuario(4L, "sysdba", 1L, "SUPERUSER"));

        IllegalStateException erro = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class,
                () -> EmpresaTenantIdentifierResolver.empresaParaGravar());

        assertTrue(erro.getMessage().contains("empresa"),
                "a mensagem tem que apontar a empresa destino: " + erro.getMessage());
    }

    @Test
    @DisplayName("o superuser e' definido pelo papel, nao pelo nome do usuario")
    void naoEPorNome() {
        // Um diretor chamado "sysdba" nao atravessa, e um superuser chamado
        // "diretor" atravessa. A regra e' do papel; se algum dia for pelo nome,
        // trocar o nome do usuario muda o alcance dos dados.
        autenticar(usuario(1L, "sysdba", 9L, "DIRETORIA"));
        assertEquals(9L, resolver.resolveCurrentTenantIdentifier());

        autenticar(usuario(2L, "diretor.comum", 4L, "SUPERUSER"));
        assertEquals(EmpresaTenantIdentifierResolver.SEM_FILTRO,
                resolver.resolveCurrentTenantIdentifier());
    }
}
