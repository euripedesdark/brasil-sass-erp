package br.com.brasil_saas.ia.service.impl;

import br.com.brasil_saas.ia.dto.AIConfigRequest;
import br.com.brasil_saas.ia.dto.AIConfigResponse;
import br.com.brasil_saas.ia.model.AIConfig;
import br.com.brasil_saas.ia.repository.AIConfigRepository;
import br.com.brasil_saas.ia.service.AIConfigService;
import br.com.brasil_saas.shared.exception.BusinessException;
import br.com.brasil_saas.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AIConfigServiceImpl implements AIConfigService {

    private final AIConfigRepository configRepository;

    @Override
    @Transactional
    public AIConfigResponse saveConfig(AIConfigRequest request, Long empresaId) {
        AIConfig config = configRepository.findByEmpresaId(empresaId)
            .orElse(new AIConfig());
        
        config.setEmpresaId(empresaId);
        config.setApiKey(request.apiKey());
        config.setApiEndpoint(request.apiEndpoint());
        config.setDefaultModel(request.defaultModel());
        config.setTemperature(request.temperature());
        config.setMaxTokens(request.maxTokens());
        config.setTimeoutSeconds(request.timeoutSeconds());
        config.setIsEnabled(request.isEnabled());
        config.setMaxDailyTokens(request.maxDailyTokens());
        
        if (config.getLastResetDate() == null) {
            config.setLastResetDate(LocalDate.now());
        }
        
        config = configRepository.save(config);
        
        return mapToResponse(config);
    }

    @Override
    @Transactional(readOnly = true)
    public AIConfigResponse getConfig(Long empresaId) {
        AIConfig config = configRepository.findByEmpresaId(empresaId)
            .orElseThrow(() -> new ResourceNotFoundException("Configuracao de IA nao encontrada"));
        
        return mapToResponse(config);
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean testConnection(Long empresaId) {
        AIConfig config = configRepository.findByEmpresaId(empresaId)
            .orElseThrow(() -> new ResourceNotFoundException("Configuracao de IA nao encontrada"));
        
        if (config.getApiKey() == null || config.getApiKey().isBlank()) {
            throw new BusinessException("API_KEY_REQUIRED", "Chave API nao configurada");
        }
        
        return true;
    }

    @Override
    @Transactional
    public void resetDailyUsage(Long empresaId) {
        AIConfig config = configRepository.findByEmpresaId(empresaId)
            .orElseThrow(() -> new ResourceNotFoundException("Configuracao de IA nao encontrada"));
        
        config.setDailyTokenUsage(0L);
        config.setLastResetDate(LocalDate.now());
        configRepository.save(config);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getRemainingTokens(Long empresaId) {
        AIConfig config = configRepository.findByEmpresaId(empresaId)
            .orElse(new AIConfig());
        
        if (config.getMaxDailyTokens() == null) {
            return Long.MAX_VALUE;
        }
        
        return Math.max(0L, config.getMaxDailyTokens() - config.getDailyTokenUsage());
    }

    private AIConfigResponse mapToResponse(AIConfig config) {
        return new AIConfigResponse(
            config.getId(),
            config.getApiKey(),
            config.getApiEndpoint(),
            config.getDefaultModel(),
            config.getTemperature(),
            config.getMaxTokens(),
            config.getTimeoutSeconds(),
            config.getIsEnabled(),
            config.getMaxDailyTokens(),
            config.getDailyTokenUsage(),
            config.getLastResetDate() != null ? config.getLastResetDate().toString() : null
        );
    }
}
