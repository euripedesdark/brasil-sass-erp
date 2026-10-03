package br.com.brasil_saas.bi.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bc_bi_kpi", schema = "brasil_saas")
@Getter @Setter
public class Kpi extends TenantEntity {
    
    @Column(length = 100, nullable = false)
    private String name;
    
    @Column(columnDefinition = "text")
    private String description;
    
    @Column(name = "kpi_type", length = 50)
    private String kpiType; // FINANCEIRO, VENDAS, ESTOQUE, RH, SERVICOS
    
    @Column(name = "query_formula", columnDefinition = "text")
    private String queryFormula;
    
    @Column(name = "target_value")
    private Double targetValue;
    
    @Column(name = "current_value")
    private Double currentValue;
    
    @Column(name = "unit", length = 20)
    private String unit = "R$";
    
    @Column(name = "format", length = 20)
    private String format = "NUMBER";
    
    @Column(name = "color_good", length = 20)
    private String colorGood = "#10b981";
    
    @Column(name = "color_warning", length = 20)
    private String colorWarning = "#f59e0b";
    
    @Column(name = "color_bad", length = 20)
    private String colorBad = "#ef4444";
    
    @Column(name = "threshold_good")
    private Double thresholdGood;
    
    @Column(name = "threshold_warning")
    private Double thresholdWarning;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
}
