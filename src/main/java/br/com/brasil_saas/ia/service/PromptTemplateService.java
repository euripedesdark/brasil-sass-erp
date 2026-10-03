package br.com.brasil_saas.ia.service;

import br.com.brasil_saas.ia.model.PromptTemplate;
import br.com.brasil_saas.shared.web.PageResponse;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface PromptTemplateService {
    
    PromptTemplate save(PromptTemplate template);
    
    List<PromptTemplate> listAll(Long empresaId);
    
    List<PromptTemplate> listByCategory(String category, Long empresaId);
    
    PageResponse<PromptTemplate> listPaginated(Long empresaId, Pageable pageable);
    
    void delete(Long id, Long empresaId);
    
    PromptTemplate getById(Long id, Long empresaId);
}
