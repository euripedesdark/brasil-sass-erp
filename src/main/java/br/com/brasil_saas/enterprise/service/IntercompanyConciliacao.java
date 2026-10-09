package br.com.brasil_saas.enterprise.service;

import java.math.BigDecimal;

/** Regra pura de conciliação entre os dois lados de um lançamento intercompany. */
public final class IntercompanyConciliacao {
    private IntercompanyConciliacao() {}

    public record Resultado(String status, BigDecimal diferenca) {
        public boolean reconciliado() { return "RECONCILIADO".equals(status); }
    }

    /** @param tolerancia diferença absoluta aceita (ex.: 0.01 para arredondamento). */
    public static Resultado comparar(BigDecimal valorLocal, String moedaLocal,
                                     BigDecimal valorParceiro, String moedaParceira, BigDecimal tolerancia) {
        if (valorParceiro == null) return new Resultado("SEM_CONTRAPARTIDA", null);
        if (moedaLocal != null && moedaParceira != null && !moedaLocal.trim().equalsIgnoreCase(moedaParceira.trim()))
            return new Resultado("DIVERGENTE_MOEDA", null);
        BigDecimal dif = valorLocal.subtract(valorParceiro);
        BigDecimal tol = tolerancia == null ? BigDecimal.ZERO : tolerancia.abs();
        return dif.abs().compareTo(tol) <= 0 ? new Resultado("RECONCILIADO", dif) : new Resultado("DIVERGENTE", dif);
    }
}
