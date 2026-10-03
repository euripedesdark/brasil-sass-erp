package br.com.brasil_saas.bi.dto;

import java.time.LocalDateTime;

public record KpiResponse(
    Long id,
    String name,
    String description,
    String kpiType,
    String queryFormula,
    Double targetValue,
    Double currentValue,
    String unit,
    String format,
    String colorGood,
    String colorWarning,
    String colorBad,
    Double thresholdGood,
    Double thresholdWarning,
    Boolean isActive,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
