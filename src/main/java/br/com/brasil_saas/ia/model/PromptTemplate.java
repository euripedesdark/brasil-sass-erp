package br.com.brasil_saas.ia.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bc_ia_prompt_template", schema = "brasil_saas")
@Getter @Setter
public class PromptTemplate extends TenantEntity {
    
    @Column(length = 100, nullable = false)
    private String name;
    
    @Column(name = "category", length = 50)
    private String category;
    
    @Column(columnDefinition = "text", nullable = false)
    private String content;
    
    @Column(columnDefinition = "text")
    private String description;
    
    @Column(name = "variables", columnDefinition = "jsonb")
    private String variables;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
}
