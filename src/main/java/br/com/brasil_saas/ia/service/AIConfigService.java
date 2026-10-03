package br.com.brasil_saas.ia.service;

import br.com.brasil_saas.ia.dto.AIConfigRequest;
import br.com.brasil_saas.ia.dto.AIConfigResponse;

public interface AIConfigService {
    
    AIConfigResponse saveConfig(AIConfigRequest request, Long empresaId);
    
    AIConfigResponse getConfig(Long empresaId);
    
    Boolean testConnection(Long empresaId);
    
    void resetDailyUsage(Long empresaId);
    
    Long getRemainingTokens(Long empresaId);
}
