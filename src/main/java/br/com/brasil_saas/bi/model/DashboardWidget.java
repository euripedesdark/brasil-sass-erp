package br.com.brasil_saas.bi.model;

import br.com.brasil_saas.shared.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bc_bi_dashboard_widget", schema = "brasil_saas")
@Getter @Setter
public class DashboardWidget extends BaseEntity {
    
    @ManyToOne
    @JoinColumn(name = "dashboard_id", nullable = false)
    private Dashboard dashboard;
    
    @Column(length = 100, nullable = false)
    private String title;
    
    @Column(name = "widget_type", length = 50, nullable = false)
    private String widgetType; // CHART, METRIC, TABLE, GAUGE
    
    @Column(name = "data_source", length = 200)
    private String dataSource;
    
    @Column(name = "chart_type", length = 50)
    private String chartType; // BAR, LINE, PIE, DOUGHNUT, AREA
    
    @Column(columnDefinition = "jsonb")
    private String config;
    
    @Column(name = "position_x")
    private Integer positionX = 0;
    
    @Column(name = "position_y")
    private Integer positionY = 0;
    
    @Column(name = "width")
    private Integer width = 4;
    
    @Column(name = "height")
    private Integer height = 3;
    
    @Column(name = "refresh_enabled")
    private Boolean refreshEnabled = true;
}
