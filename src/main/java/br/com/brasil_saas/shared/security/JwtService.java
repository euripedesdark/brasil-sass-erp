package br.com.brasil_saas.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Emissão e validação de JWT (jjwt 0.12.x).
 */
@Service
public class JwtService {

    @Value("${brasil-saas.jwt.secret}")
    private String secret;

    @Value("${brasil-saas.jwt.expiration-ms:3600000}")
    private long expirationMs;

    @Value("${brasil-saas.jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationMs;

    public String generateToken(Long userId, String username, Long empresaId) {
        return generateToken(userId, username, empresaId, List.of());
    }

    public String generateToken(Long userId, String username, Long empresaId, Collection<String> adGroups) {
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("empresaId", empresaId)
                .claim("adGroups", adGroups == null ? List.of() : List.copyOf(adGroups))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getKey())
                .compact();
    }

    public String generateRefreshToken(Long userId, String username) {
        return generateRefreshToken(userId, username, List.of());
    }

    public String generateRefreshToken(Long userId, String username, Collection<String> adGroups) {
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("type", "refresh")
                .claim("adGroups", adGroups == null ? List.of() : List.copyOf(adGroups))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                .signWith(getKey())
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validate(String token) {
        try {
            parse(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            return "refresh".equals(parse(token).get("type", String.class));
        } catch (Exception e) {
            return false;
        }
    }

    public String username(String token) {
        return parse(token).getSubject();
    }

    public Long userId(String token) {
        return parse(token).get("userId", Long.class);
    }

    /** Grupos devolvidos pelo Auth Service e preservados na sessão do ERP. */
    public List<String> adGroups(String token) {
        Object value = parse(token).get("adGroups");
        if (value instanceof Collection<?> collection) {
            return collection.stream()
                    .filter(Objects::nonNull)
                    .map(String::valueOf)
                    .toList();
        }
        return List.of();
    }

    public Long empresaId(String token) {
        return parse(token).get("empresaId", Long.class);
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
