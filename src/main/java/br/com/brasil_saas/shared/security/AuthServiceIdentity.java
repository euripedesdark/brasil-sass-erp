package br.com.brasil_saas.shared.security;

import java.util.List;

public record AuthServiceIdentity(
        String identityId,
        String username,
        String provider,
        List<String> groups) {
}
