package br.com.brasil_saas.shared.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import br.com.brasil_saas.core.model.Modulo;
import br.com.brasil_saas.core.repository.ModuloRepository;
import br.com.brasil_saas.core.service.ModuloAcessoService;
import jakarta.servlet.FilterChain;

/**
 * A regra do dono, testada na fronteira: "se um usuario e' rh ele so pode
 * acessar coisa de rh daquela empresa, para ele acessar o financeiro ele
 * precisa de permissao de financeiro e rh" e "diretoria e gerente tem que ver
 * todos os modulos da empresa a quem eles pertencem".
 *
 * <p><b>Por que testar o filtro e nao o servico.</b> O servico tem os metodos e
 * a logica. O que decide se a regra vale na pratica e' se o filtro esta no
 * caminho da requisicao e mapeia a URL para o modulo certo. Um servico
 * perfeito e' um filtro ausente.
 *
 * <p><b>Por que o caso do prefixo desconhecido tem teste.</b> E' a falha que
 * nao aparece em nenhum teste de tela: um controller novo em
 * {@code /api/algoQueNaoExiste/} que ninguem mapeou. Com a regra "desconhecido
 * libera", ele passa livre e ninguem ve ate um dado vazar. Aqui desconhecido
 * bloqueia, e a mensagem diz o nome do prefixo.
 */
class ModuloAcessoFilterTest {

    private ModuloRepository moduloRepository;
    private ModuloAcessoService servico;
    private ModuloAcessoFilter filtro;
    private FilterChain chain;

    @BeforeEach
    void preparar() {
        moduloRepository = mock(ModuloRepository.class);
        servico = new ModuloAcessoService(moduloRepository, mock(
                br.com.brasil_saas.core.repository.UsuarioModuloRepository.class),
                mock(br.com.brasil_saas.core.repository.UsuarioRepository.class));
        filtro = new ModuloAcessoFilter(servico);
        chain = mock(FilterChain.class);
        SecurityContextHolder.clearContext();
    }

    private static AuthenticatedUser usuario(Long id, String username, Long empresaId,
                                            String... papeis) {
        return new AuthenticatedUser(id, username, "x", true, empresaId,
                List.of(papeis).stream()
                        .map(p -> (org.springframework.security.core.GrantedAuthority)
                                new SimpleGrantedAuthority("ROLE_" + p))
                        .toList());
    }

    private void autenticar(AuthenticatedUser u) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(u, null, u.getAuthorities()));
    }

    private MockHttpServletResponse chamar(String metodo, String uri) throws Exception {
        var req = new MockHttpServletRequest(metodo, uri);
        var res = new MockHttpServletResponse();
        filtro.doFilter(req, res, chain);
        return res;
    }

    // ------------------------------------------------------------------
    // A regra que o dono pediu
    // ------------------------------------------------------------------

    @Test
    @DisplayName("RH sem o modulo financeiro e' barrado no financeiro")
    void rhNaoEntraNoFinanceiro() throws Exception {
        when(moduloRepository.findByChave("rh")).thenReturn(Optional.of(modulo(1L, "rh")));
        when(moduloRepository.findByChave("financeiro")).thenReturn(Optional.of(modulo(2L, "financeiro")));
        when(moduloRepository.usuarioTemModulo(anyLong(), anyLong())).thenAnswer(i ->
                i.getArgument(1, Long.class) == 1L);   // so o modulo 1, o de rh

        autenticar(usuario(50L, "joao.rh", 5L, "RH"));

        var permitido = chamar("GET", "/api/rh/funcionarios");
        assertEquals(200, permitido.getStatus(), "rh tem o modulo rh: passa");

        var barrado = chamar("GET", "/api/financeiro/titulos");
        assertEquals(403, barrado.getStatus(), "rh nao tem financeiro: barrado");
        assertTrue(barrado.getContentAsString().contains("financeiro"),
                "a resposta tem que dizer qual modulo faltou: "
                        + barrado.getContentAsString());
    }

    @Test
    @DisplayName("com os dois modulos, o mesmo RH entra no financeiro")
    void rhComOsDoisModulosEntraNoFinanceiro() throws Exception {
        when(moduloRepository.findByChave(anyString())).thenAnswer(i ->
                Optional.of(modulo(99L, i.getArgument(0))));
        when(moduloRepository.usuarioTemModulo(anyLong(), anyLong())).thenReturn(true);

        autenticar(usuario(50L, "joao.rh", 5L, "RH"));

        assertEquals(200, chamar("GET", "/api/rh/funcionarios").getStatus());
        assertEquals(200, chamar("GET", "/api/financeiro/titulos").getStatus());
    }

    @Test
    @DisplayName("DIRETORIA ve todos os modulos da empresa, sem estar cadastrada neles")
    void diretoriaVeTodosSemEstarCadastrada() throws Exception {
        // Nenhum modulo liberado para ela. Se dependesse da tabela, ela nao
        // veria nada — que e' o oposto do que o dono pediu.
        when(moduloRepository.findByChave(anyString())).thenReturn(Optional.of(modulo(1L, "x")));
        when(moduloRepository.usuarioTemModulo(anyLong(), anyLong())).thenReturn(false);

        autenticar(usuario(51L, "helena", 5L, "DIRETORIA"));

        assertEquals(200, chamar("GET", "/api/financeiro/titulos").getStatus());
        assertEquals(200, chamar("GET", "/api/rh/funcionarios").getStatus());
        assertEquals(200, chamar("GET", "/api/fiscal/nfe").getStatus());
        // e o bypass e' de verdade: nem chegou a consultar a tabela
        verify(moduloRepository, never()).usuarioTemModulo(anyLong(), anyLong());
    }

    @Test
    @DisplayName("GERENTE tambem ve todos os modulos")
    void gerenteVeTodos() throws Exception {
        when(moduloRepository.findByChave(anyString())).thenReturn(Optional.of(modulo(1L, "x")));
        when(moduloRepository.usuarioTemModulo(anyLong(), anyLong())).thenReturn(false);

        autenticar(usuario(52L, "gerente", 5L, "GERENTE"));

        assertEquals(200, chamar("GET", "/api/financeiro/titulos").getStatus());
    }

    @Test
    @DisplayName("CONSULTA nao ve tudo por ser consulta: ela ve o que tem")
    void consultaNaoViraDiretoria() throws Exception {
        when(moduloRepository.findByChave(anyString())).thenReturn(Optional.of(modulo(1L, "x")));
        when(moduloRepository.usuarioTemModulo(anyLong(), anyLong())).thenReturn(false);

        autenticar(usuario(53L, "leitor", 5L, "CONSULTA"));

        assertEquals(403, chamar("GET", "/api/financeiro/titulos").getStatus(),
                "CONSULTA nao esta na lista de quem ve tudo. Se entrasse, o nome "
                        + "'somente leitura' seria a unica distincao do sistema.");
    }

    // ------------------------------------------------------------------
    // Somente leitura
    // ------------------------------------------------------------------

    @Test
    @DisplayName("somente leitura bloqueia escrever e deixa ler")
    void somenteLeituraBloqueiaEscrita() throws Exception {
        when(moduloRepository.findByChave(anyString()))
                .thenReturn(Optional.of(modulo(1L, "financeiro")));
        when(moduloRepository.usuarioTemModulo(anyLong(), anyLong())).thenReturn(true);
        when(moduloRepository.modulosEOmenteLeituraDoUsuario(anyLong()))
                .thenReturn(List.<Object[]>of(new Object[] { "financeiro", true }));

        autenticar(usuario(54L, "contador", 5L, "FINANCEIRO"));

        assertEquals(200, chamar("GET", "/api/financeiro/titulos").getStatus(), "ler e' permitido");
        assertEquals(403, chamar("POST", "/api/financeiro/titulos").getStatus(), "criar nao e'");
        assertEquals(403, chamar("PUT", "/api/financeiro/titulos/1").getStatus(), "alterar nao e'");
        assertEquals(403, chamar("DELETE", "/api/financeiro/titulos/1").getStatus(), "excluir nao e'");
        assertEquals(200, chamar("HEAD", "/api/financeiro/titulos").getStatus(), "HEAD e' leitura");
    }

    // ------------------------------------------------------------------
    // As rotas que nao sao de modulo
    // ------------------------------------------------------------------

    @Test
    @DisplayName("login, superadmin e perfil proprio nao sao modulo")
    void rotasQueNaoSaoModuloPassam() throws Exception {
        // Nenhum modulo liberado. Se alguma dessas fosse tratada como modulo,
        // o login em si seria barrado e ninguem entraria no sistema.
        when(moduloRepository.findByChave(anyString())).thenReturn(Optional.empty());
        when(moduloRepository.usuarioTemModulo(anyLong(), anyLong())).thenReturn(false);

        autenticar(usuario(55L, "comum", 5L, "FINANCEIRO"));

        assertEquals(200, chamar("POST", "/api/auth/login").getStatus());
        assertEquals(200, chamar("GET", "/api/core/perfil").getStatus());
        assertEquals(200, chamar("GET", "/api/superadmin/usuarios").getStatus(),
                "superadmin tem o proprio @PreAuthorize; o filtro nao e' a porta dele");
    }

    @Test
    @DisplayName("municipios e' consulta de apoio e nao exige modulo")
    void municipiosNaoExigeModulo() throws Exception {
        // Sem o modulo cadastro, cadastrar um cliente precisa do IBGE na
        // mesma tela. Se municipios fosse modulo, travaria o cadastro inteiro.
        when(moduloRepository.findByChave(anyString())).thenReturn(Optional.empty());
        when(moduloRepository.usuarioTemModulo(anyLong(), anyLong())).thenReturn(false);

        autenticar(usuario(56L, "so.financeiro", 5L, "FINANCEIRO"));

        assertEquals(200, chamar("GET", "/api/municipios").getStatus());
    }

    @Test
    @DisplayName("sem token, o login nao e' barrado")
    void semTokenNaoEhBarrado() throws Exception {
        SecurityContextHolder.clearContext();

        assertEquals(200, chamar("POST", "/api/auth/login").getStatus());
        verify(chain).doFilter(any(), any());
    }

    @Test
    @DisplayName("o anonimo do Spring nao e' tratado como superuser")
    void anonimoNaoPassa() throws Exception {
        // O Spring instala um AnonymousAuthenticationToken em rota liberada, e
        // ele conta como autenticado. Se ele passasse, uma rota liberada por
        // navegador viraria acesso sem empresa a qualquer modulo.
        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken("k", "anon",
                        AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

        var res = chamar("GET", "/api/financeiro/titulos");
        assertEquals(200, res.getStatus(), "o anonimo passa pelo filtro; quem barra e' o security");
        verify(chain).doFilter(any(), any());
    }

    // ------------------------------------------------------------------
    // O caso que mais escapa
    // ------------------------------------------------------------------

    @Test
    @DisplayName("prefixo que nao e' modulo nenhum e' barrado, nao liberado")
    void prefixoDesconhecidoEBarrado() throws Exception {
        // Um controller novo em /api/algoQueNaoExiste/ nao tem linha em
        // bc_core_modulo. A tentacao e liberar o desconhecido, e ai ele passa
        // livre para qualquer usuario autenticado.
        when(moduloRepository.findByChave(anyString())).thenReturn(Optional.empty());
        when(moduloRepository.usuarioTemModulo(anyLong(), anyLong())).thenReturn(false);

        // Um usuario COMUM. O superuser passa por cima de tudo, e isso e' certo:
        // prefixo sem controller responde 404, e um controller novo sem modulo
        // cadastrado seria dele mesmo para criar o vinculo.
        autenticar(usuario(57L, "comum", 5L, "FINANCEIRO"));
        var res = chamar("GET", "/api/algoQueNaoExiste/lista");
        assertEquals(403, res.getStatus(),
                "prefixo desconhecido bloqueado — e o que impede o furo silencioso");
        assertTrue(res.getContentAsString().contains("algoQueNaoExiste"),
                "a mensagem nomeia o prefixo, que e' a pista do cadastro que falta: "
                        + res.getContentAsString());
    }

    @Test
    @DisplayName("o superuser passa ate em prefixo desconhecido, e isso e' decisao")
    void superuserPassaEmPrefixoDesconhecido() throws Exception {
        // Ele atravessa empresa e modulo, por definicao. Fechar o superuser aqui
        // trancaria o usuario de manutencao do proprio sistema que ele precisa
        // para cadastrar o modulo que faltou.
        when(moduloRepository.findByChave(anyString())).thenReturn(Optional.empty());

        autenticar(usuario(59L, "sysdba", null, "SUPERUSER"));

        assertEquals(200, chamar("GET", "/api/algoQueNaoExiste/lista").getStatus());
    }

    @Test
    @DisplayName("fora de /api nao ha modulo a checar")
    void foraDeApiNaoTemModulo() throws Exception {
        when(moduloRepository.findByChave(anyString())).thenReturn(Optional.empty());
        when(moduloRepository.usuarioTemModulo(anyLong(), anyLong())).thenReturn(false);

        autenticar(usuario(58L, "comum", 5L, "FINANCEIRO"));

        assertEquals(200, chamar("GET", "/actuator/health").getStatus());
        assertEquals(200, chamar("GET", "/").getStatus());
    }

    @Test
    @DisplayName("usuario sem empresa e' barrado no modulo, e nao aprovado")
    void usuarioSemEmpresaNaoEntraNoModulo() throws Exception {
        // Sem empresa ele nao tem o que ver: o tenant e' sentinela e o filtro
        // nao restringe, entao quem barra e' este. A ordem importa — a mensagem
        // tem que ser a do modulo, porque e' a acao que falta.
        when(moduloRepository.findByChave(anyString())).thenReturn(Optional.of(modulo(1L, "rh")));
        when(moduloRepository.usuarioTemModulo(anyLong(), anyLong())).thenReturn(false);

        autenticar(usuario(59L, "sem.empresa", null, "FINANCEIRO"));

        var res = chamar("GET", "/api/rh/funcionarios");
        assertEquals(403, res.getStatus());
        assertFalse(res.getContentAsString().isBlank(), "a resposta tem que explicar");
    }

    private static Modulo modulo(Long id, String chave) {
        Modulo m = new Modulo();
        m.setId(id);
        m.setChave(chave);
        m.setNome(chave);
        return m;
    }
}
