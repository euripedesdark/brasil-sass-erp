package br.com.brasil_saas.core.service.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private Long usuarioId;
    private Long empresaId;
    private String username;
    private Set<String> authorities;
    private String authSource;
}
