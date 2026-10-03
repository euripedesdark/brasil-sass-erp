package br.com.brasil_saas.shared.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * O grupo do AD traduzido em authority.
 *
 * <p>O que estes testes travam: o grupo do AD passa a valer como perfil, e nada
 * mais. O grupo universal do dominio nao pode virar authority — {@code srvcloud}
 * chega para toda conta, e se virar {@code ROLE_SRVCLOUD} seria um acesso que
 * ninguem concedeu.
 *
 * <p>A traducao e um metodo estatico e privado, entao o teste o invoca direto por
 * reflexao. Nao ha mock de {@code JdbcTemplate} aqui de proposito: o caminho do
 * grupo nao passa pelo banco, e o Mockito nao instrumenta o {@code JdbcTemplate}
 * neste JDK.
 */
class CustomUserDetailsServiceTest {

    /** Invoca o metodo privado e estatico que faz a traducao. */
    @SuppressWarnings("unchecked")
    private Set<String> rolesDe(Collection<String> grupos) throws Exception {
        Method m = CustomUserDetailsService.class.getDeclaredMethod("rolesDosGrupos", Set.class);
        m.setAccessible(true);
        return (Set<String>) m.invoke(null, normalizados(grupos));
    }

    /** Os grupos chegam do Auth Service ja normalizados, como no codigo real. */
    private Set<String> normalizados(Collection<String> grupos) {
        Set<String> out = new LinkedHashSet<>();
        for (String g : grupos) {
            if (g != null && !g.isBlank()) {
                out.add(g.trim().toLowerCase(Locale.ROOT));
            }
        }
        return out;
    }

    @Test
    @DisplayName("GRP_DIRETORIA vira ROLE_DIRETORIA")
    void diretoriaViraRole() throws Exception {
        Set<String> roles = rolesDe(List.of("GRP_DIRETORIA"));

        assertEquals(Set.of("ROLE_DIRETORIA"), roles);
    }

    @Test
    @DisplayName("os nove grupos do contrato viram os nove ROLE_")
    void contratoInteiro() throws Exception {
        Set<String> roles = rolesDe(List.of(
                "GRP_DIRETORIA",
                "GRP_GERENTE",
                "GRP_GESTOR",
                "GRP_FINANCEIRO",
                "GRP_ESTOQUE",
                "GRP_RH",
                "GRP_VENDEDOR",
                "GRP_ERP_ADMIN",
                "GRP_SUPERUSER"));

        assertEquals(Set.of(
                "ROLE_DIRETORIA",
                "ROLE_GERENTE",
                "ROLE_GESTOR",
                "ROLE_FINANCEIRO",
                "ROLE_ESTOQUE",
                "ROLE_RH",
                "ROLE_VENDEDOR",
                "ROLE_ADMIN",
                "ROLE_SUPERUSER"), roles);
    }

    @Test
    @DisplayName("grupo fora do contrato nao vira authority nenhuma")
    void grupoForaDoContratoNaoViraRole() throws Exception {
        Set<String> roles = rolesDe(List.of(
                "srvcloud",
                "Domain Admins",
                "ERP_EMPRESA_00000000000191",
                "ERP_MODULO_FISCAL",
                "GRP_ERP_ACESSO",
                "GRP_ASTRAL_ADMIN"));

        assertTrue(roles.isEmpty(), "nao devia gerar authority, gerou: " + new TreeSet<>(roles));
    }

    @Test
    @DisplayName("o grupo de empresa nao polui com ROLE_")
    void grupoDeEmpresaNaoViraRole() throws Exception {
        Set<String> roles = rolesDe(List.of(
                "GRP_DIRETORIA",
                "ERP_EMPRESA_00000000000191"));

        // o diretor entra, para o acesso total continuar valendo...
        assertTrue(roles.contains("ROLE_DIRETORIA"), "perdeu o ROLE_DIRETORIA");
        // ...e o tenant nao vira authority, porque empresa nao e perfil.
        assertEquals(1, roles.size(), "o grupo de empresa virou authority: " + new TreeSet<>(roles));
        assertTrue(roles.stream().noneMatch(r -> r.contains("EMPRESA")),
                "grupo de empresa virou authority: " + new TreeSet<>(roles));
    }
}
