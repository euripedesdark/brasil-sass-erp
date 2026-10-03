package br.com.brasil_saas.bi.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bc_bi_report", schema = "brasil_saas")
@Getter @Setter
public class Report extends TenantEntity {
    
    @Column(length = 100, nullable = false)
    private String name;
    
    @Column(columnDefinition = "text")
    private String description;
    
    @Column(name = "report_type", length = 50)
    private String reportType; // PDF, EXCEL, CSV, HTML
    
    @Column(name = "query_sql", columnDefinition = "text")
    private String querySql;
    
    @Column(name = "template_path", length = 500)
    private String templatePath;
    
    @Column(name = "output_format", length = 50)
    private String outputFormat;
    
    @Column(name = "is_scheduled")
    private Boolean isScheduled = false;
    
    @Column(name = "schedule_cron", length = 100)
    private String scheduleCron;
    
    @Column(name = "schedule_email", length = 200)
    private String scheduleEmail;
    
    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReportParameter> parameters = new ArrayList<>();
    
    @Column(name = "category", length = 50)
    private String category;
}
