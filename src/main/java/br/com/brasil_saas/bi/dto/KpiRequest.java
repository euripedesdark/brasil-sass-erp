package br.com.brasil_saas.bi.dto;

public record KpiRequest(
    String name,
    String description,
    String kpiType,
    String queryFormula,
    Double targetValue,
    String unit,
    String format,
    String colorGood,
    String colorWarning,
    String colorBad,
    Double thresholdGood,
    Double thresholdWarning,
    Boolean isActive
) {}
