package br.com.brasil_saas.ia.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bc_ia_config", schema = "brasil_saas")
@Getter @Setter
public class AIConfig extends TenantEntity {
    
    @Column(name = "api_key", columnDefinition = "text")
    private String apiKey;
    
    @Column(name = "api_endpoint", length = 500)
    private String apiEndpoint = "https://api.openai.com/v1";
    
    @Column(name = "default_model", length = 100)
    private String defaultModel = "gpt-4";
    
    @Column(name = "temperature")
    private Double temperature = 0.7;
    
    @Column(name = "max_tokens")
    private Integer maxTokens = 4000;
    
    @Column(name = "timeout_seconds")
    private Integer timeoutSeconds = 60;
    
    @Column(name = "is_enabled")
    private Boolean isEnabled = false;
    
    @Column(name = "max_daily_tokens")
    private Long maxDailyTokens = 100000L;
    
    @Column(name = "daily_token_usage")
    private Long dailyTokenUsage = 0L;
    
    @Column(name = "last_reset_date")
    private java.time.LocalDate lastResetDate;
}
