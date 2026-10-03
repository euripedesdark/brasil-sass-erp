package br.com.brasil_saas.ia.repository;

import br.com.brasil_saas.ia.model.PromptTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PromptTemplateRepository extends JpaRepository<PromptTemplate, Long> {
    
    List<PromptTemplate> findByEmpresaIdAndIsActiveTrueOrderBySortOrderAsc(Long empresaId);
    
    List<PromptTemplate> findByEmpresaIdAndCategoryOrderBySortOrderAsc(Long empresaId, String category);
    
    Page<PromptTemplate> findByEmpresaId(Long empresaId, Pageable pageable);
}
