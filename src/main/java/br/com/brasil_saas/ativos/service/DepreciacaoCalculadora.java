package br.com.brasil_saas.ativos.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

/**
 * Cota mensal de depreciacao, sem acesso a banco.
 *
 * <p>Convencao de mes cheio: o ativo deprecia a partir do mes de inicio da
 * depreciacao (ou da aquisicao). A cota e' sempre limitada ao saldo
 * depreciavel restante, entao reavaliacao e impairment sao distribuidos pela
 * vida util remanescente.
 */
public final class DepreciacaoCalculadora {

    public static final String LINEAR = "LINEAR";
    public static final String SOMA_DIGITOS = "SOMA_DIGITOS";
    public static final String SALDO_DECRESCENTE = "SALDO_DECRESCENTE";

    private DepreciacaoCalculadora() {}

    /** Indice do periodo dentro da vida util: 0 no mes de inicio, negativo antes dele. */
    public static int indiceMes(LocalDate inicio, YearMonth periodo) {
        return (int) ChronoUnit.MONTHS.between(YearMonth.from(inicio), periodo);
    }

    /**
     * @param base            valor depreciavel total (custo + reavaliacao - residual - impairment)
     * @param depreciado      depreciacao ja acumulada
     * @param vidaUtilMeses   vida util total em meses
     * @param taxaAnual       taxa anual (fracao, ex.: 0.20) para saldo decrescente; opcional
     * @param indiceMes       indice do periodo (0 = primeiro mes)
     */
    public static BigDecimal cota(String metodo, BigDecimal base, BigDecimal depreciado,
                                  int vidaUtilMeses, BigDecimal taxaAnual, int indiceMes) {
        if (base == null || vidaUtilMeses <= 0 || indiceMes < 0) return zero();
        BigDecimal acumulado = depreciado == null ? BigDecimal.ZERO : depreciado;
        BigDecimal restante = base.subtract(acumulado);
        if (restante.signum() <= 0) return zero();
        int mesesRestantes = vidaUtilMeses - indiceMes;
        if (mesesRestantes <= 0) return restante.setScale(2, RoundingMode.HALF_UP);

        BigDecimal linearRestante = restante.divide(BigDecimal.valueOf(mesesRestantes), 2, RoundingMode.HALF_UP);
        BigDecimal valor = switch (metodo == null ? LINEAR : metodo) {
            case SOMA_DIGITOS -> somaDigitos(base, vidaUtilMeses, indiceMes);
            case SALDO_DECRESCENTE -> saldoDecrescente(restante, vidaUtilMeses, taxaAnual).max(linearRestante);
            default -> linearRestante;
        };
        return valor.min(restante).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal somaDigitos(BigDecimal base, int vidaUtilMeses, int indiceMes) {
        int anos = (vidaUtilMeses + 11) / 12;
        int ano = indiceMes / 12;
        if (ano >= anos) return base;
        BigDecimal soma = BigDecimal.valueOf((long) anos * (anos + 1) / 2);
        return base.multiply(BigDecimal.valueOf(anos - ano))
                .divide(soma.multiply(BigDecimal.valueOf(12)), 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal saldoDecrescente(BigDecimal restante, int vidaUtilMeses, BigDecimal taxaAnual) {
        BigDecimal taxa = taxaAnual != null && taxaAnual.signum() > 0
                ? taxaAnual
                : BigDecimal.valueOf(2).multiply(BigDecimal.valueOf(12)).divide(BigDecimal.valueOf(vidaUtilMeses), 6, RoundingMode.HALF_UP);
        return restante.multiply(taxa).divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
}
