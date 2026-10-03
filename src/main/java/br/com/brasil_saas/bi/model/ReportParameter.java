package br.com.brasil_saas.bi.model;

import br.com.brasil_saas.shared.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bc_bi_report_parameter", schema = "brasil_saas")
@Getter @Setter
public class ReportParameter extends BaseEntity {
    
    @ManyToOne
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;
    
    @Column(length = 50, nullable = false)
    private String name;
    
    @Column(length = 100)
    private String label;
    
    @Column(name = "parameter_type", length = 20)
    private String parameterType; // STRING, NUMBER, DATE, BOOLEAN
    
    @Column(name = "default_value", length = 200)
    private String defaultValue;
    
    @Column(name = "is_required")
    private Boolean isRequired = false;
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
}
