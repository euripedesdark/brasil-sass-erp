package br.com.brasil_saas.core.service.impl;

import br.com.brasil_saas.core.config.IdentidadeProperties;
import br.com.brasil_saas.core.model.Usuario;
import br.com.brasil_saas.core.repository.*;
import br.com.brasil_saas.core.service.ModuloAcessoService;
import br.com.brasil_saas.core.service.dto.LoginRequest;
import br.com.brasil_saas.shared.exception.BusinessException;
import br.com.brasil_saas.shared.security.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceIamTest {
    @Mock UsuarioRepository usuarios;
    @Mock EmpresaRepository empresas;
    @Mock ModuloAcessoService modulos;
    @Mock PerfilRepository perfis;
    @Mock PasswordEncoder encoder;
    @Mock JwtService jwt;
    @Mock IdentidadeProperties identidade;
    @Mock AuthServiceClient iam;
    @InjectMocks AuthServiceImpl service;

    private LoginRequest request(String provider) {
        var r = new LoginRequest(); r.setUsername(" postgres "); r.setPassword("senha-teste"); r.setProvider(provider); return r;
    }
    private Usuario usuario() {
        var u = new Usuario(); u.setId(5L); u.setUsername("postgres"); u.setAtivo(true); return u;
    }
    private void aceito(String provider, List<String> groups) {
        var u = usuario();
        when(iam.authenticate("postgres", "senha-teste", provider)).thenReturn(new AuthServiceIdentity("id", "postgres", provider, groups));
        when(usuarios.findByUsernameWithAuthorities("postgres")).thenReturn(Optional.of(u));
        when(usuarios.save(u)).thenReturn(u);
    }
    @ParameterizedTest @ValueSource(strings={"DB", "POSTGRES", " postgres "})
    void loginBancoSemprePassaPeloIam(String provider) {
        aceito("POSTGRES", List.of("POSTGRES_SUPERUSER"));
        var r = service.login(request(provider));
        assertEquals("POSTGRES", r.getAuthSource());
        assertTrue(r.getAuthorities().contains("ROLE_SUPERUSER"));
        verifyNoInteractions(encoder, perfis);
    }
    @Test void rotaDatabaseNaoSegueProviderInformadoPeloCliente() {
        aceito("POSTGRES", List.of());
        var r = service.loginBanco(request("AD"));
        assertEquals("POSTGRES", r.getAuthSource());
        assertFalse(r.getAuthorities().contains("ROLE_SUPERUSER"));
        verifyNoInteractions(encoder);
    }
    @Test void iamIndisponivelNaoTentaSenhaLocal() {
        when(iam.authenticate(anyString(), anyString(), eq("POSTGRES")))
                .thenThrow(new BusinessException("IAM indisponivel", "AUTH_SERVICE_UNAVAILABLE"));
        assertThrows(BusinessException.class, () -> service.loginBanco(request("DB")));
        verifyNoInteractions(usuarios, encoder, jwt);
    }
    @ParameterizedTest @ValueSource(strings={"Administrators", "Domain Users", "Domain Admins"})
    void gruposDeEntradaDiretaNaoExigemCriacaoDePerfil(String grupo) {
        aceito("AD", List.of(grupo));
        var r = service.login(request("AD"));
        assertEquals("AD", r.getAuthSource());
        assertEquals(!"Domain Users".equals(grupo), r.getAuthorities().contains("ROLE_SUPERUSER"));
        verifyNoInteractions(perfis);
    }
    @Test void refreshRejeitaTokenDeAcesso() {
        when(jwt.validate("access")).thenReturn(true);
        when(jwt.isRefreshToken("access")).thenReturn(false);
        assertThrows(BusinessException.class, () -> service.refresh("access"));
        verifyNoInteractions(usuarios);
    }
    @Test void refreshRejeitaUsuarioInativo() {
        when(jwt.validate("refresh")).thenReturn(true);
        when(jwt.isRefreshToken("refresh")).thenReturn(true);
        when(jwt.username("refresh")).thenReturn("postgres");
        when(jwt.userId("refresh")).thenReturn(5L);
        var u = usuario(); u.setAtivo(false);
        when(usuarios.findByUsernameWithAuthorities("postgres")).thenReturn(Optional.of(u));
        assertThrows(BusinessException.class, () -> service.refresh("refresh"));
        verify(jwt, never()).generateRefreshToken(any(), any(), any(), any());
    }
    @Test void somenteMarcadoresDeModuloESuperuserConhecidosViraramAuthorities() {
        assertEquals(Set.of("erp_modulo_vendas", "ROLE_SUPERUSER"), CustomUserDetailsService.authoritiesDosGrupos(
                List.of("ERP_MODULO_VENDAS", "POSTGRES_SUPERUSER", "QUALQUER_ADMIN", "Domain Users")));
    }
}
