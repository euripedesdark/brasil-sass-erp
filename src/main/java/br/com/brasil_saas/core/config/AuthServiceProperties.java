package br.com.brasil_saas.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "brasil-saas.auth-service")
@Getter
@Setter
public class AuthServiceProperties {
    private boolean enabled = true;
    private String baseUrl = "http://localhost:8181";
    private String authenticatePath = "/api/v1/identity/authenticate";
}
