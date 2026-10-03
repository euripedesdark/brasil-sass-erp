package br.com.brasil_saas.bi.dto;

import java.time.LocalDateTime;
import java.util.List;

public record DashboardResponse(
    Long id,
    String name,
    String description,
    String dashboardType,
    String layoutConfig,
    Boolean isPublic,
    Boolean isDefault,
    Integer refreshIntervalMinutes,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<DashboardWidgetResponse> widgets
) {
    public record DashboardWidgetResponse(
        Long id,
        String title,
        String widgetType,
        String dataSource,
        String chartType,
        String config,
        Integer positionX,
        Integer positionY,
        Integer width,
        Integer height,
        Boolean refreshEnabled
    ) {}
}
