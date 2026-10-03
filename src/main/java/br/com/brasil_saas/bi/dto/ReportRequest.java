package br.com.brasil_saas.bi.dto;

import java.util.List;

public record ReportRequest(
    String name,
    String description,
    String reportType,
    String querySql,
    String templatePath,
    String outputFormat,
    Boolean isScheduled,
    String scheduleCron,
    String scheduleEmail,
    String category,
    List<ReportParameterRequest> parameters
) {
    public record ReportParameterRequest(
        String name,
        String label,
        String parameterType,
        String defaultValue,
        Boolean isRequired,
        Integer sortOrder
    ) {}
}
