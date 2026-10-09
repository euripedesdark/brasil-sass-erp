package br.com.brasil_saas.shared.security;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import jakarta.servlet.FilterChain;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtIamSessionTest {
    private JwtService jwt() {
        var service = new JwtService();
        ReflectionTestUtils.setField(service, "secret", "chave-so-para-teste-0123456789012345678901234567890123456789");
        ReflectionTestUtils.setField(service, "expirationMs", 60000L);
        ReflectionTestUtils.setField(service, "refreshExpirationMs", 60000L);
        return service;
    }
    @Test void tokenPreservaProviderEPrivilegioConfirmadoPeloIam() {
        var service = jwt();
        var groups = List.of("POSTGRES_SUPERUSER");
        String access = service.generateToken(1L, "postgres", 2L, groups, "POSTGRES");
        String refresh = service.generateRefreshToken(1L, "postgres", groups, "POSTGRES");
        assertEquals("POSTGRES", service.authProvider(access));
        assertEquals("POSTGRES", service.authProvider(refresh));
        assertEquals(groups, service.adGroups(refresh));
        assertFalse(service.isRefreshToken(access));
        assertTrue(service.isRefreshToken(refresh));
    }
    @Test void tokenAnteriorContinuaCompativel() {
        var service = jwt();
        assertEquals("AD", service.authProvider(service.generateToken(1L, "usuario", 2L)));
    }
    @Test void usuarioInativadoNaoContinuaAutenticadoComJwtAntigo() throws Exception {
        var service = jwt();
        var users = mock(CustomUserDetailsService.class);
        when(users.loadUserByUsername("usuario", List.of())).thenReturn(new AuthenticatedUser(
                1L, "usuario", "hash", false, 2L, List.of(new SimpleGrantedAuthority("ROLE_SUPERUSER"))));
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + service.generateToken(1L, "usuario", 2L));
        var chain = mock(FilterChain.class);
        SecurityContextHolder.clearContext();
        try {
            new JwtAuthenticationFilter(service, users).doFilter(request, new MockHttpServletResponse(), chain);
            assertNull(SecurityContextHolder.getContext().getAuthentication());
            verify(chain).doFilter(eq(request), any());
        } finally { SecurityContextHolder.clearContext(); }
    }
}
