package br.com.brasil_saas.ativos.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DepreciacaoCalculadoraTest {

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }

    @Test
    void linearDivideOSaldoPelaVidaRestante() {
        assertEquals(bd("100.00"), DepreciacaoCalculadora.cota("LINEAR", bd("12000"), BigDecimal.ZERO, 120, null, 0));
        assertEquals(bd("100.00"), DepreciacaoCalculadora.cota("LINEAR", bd("12000"), bd("6000"), 120, null, 60));
    }

    @Test
    void ultimoMesLevaOResiduoEDepoisNadaMais() {
        assertEquals(bd("100.03"), DepreciacaoCalculadora.cota("LINEAR", bd("12000"), bd("11899.97"), 120, null, 119));
        assertEquals(bd("0.00"), DepreciacaoCalculadora.cota("LINEAR", bd("12000"), bd("12000"), 120, null, 120));
    }

    @Test
    void antesDoInicioNaoDeprecia() {
        assertEquals(bd("0.00"), DepreciacaoCalculadora.cota("LINEAR", bd("12000"), BigDecimal.ZERO, 120, null, -1));
        assertEquals(-1, DepreciacaoCalculadora.indiceMes(LocalDate.of(2026, 5, 20), YearMonth.of(2026, 4)));
        assertEquals(0, DepreciacaoCalculadora.indiceMes(LocalDate.of(2026, 5, 20), YearMonth.of(2026, 5)));
    }

    @Test
    void somaDosDigitosDepreciaMaisNoPrimeiroAno() {
        // 5 anos: soma 15; ano 1 = 5/15 do valor, dividido em 12 meses
        assertEquals(bd("416.67"), DepreciacaoCalculadora.cota("SOMA_DIGITOS", bd("15000"), BigDecimal.ZERO, 60, null, 0));
        assertEquals(bd("83.33"), DepreciacaoCalculadora.cota("SOMA_DIGITOS", bd("15000"), bd("14000"), 60, null, 48));
    }

    @Test
    void somaDosDigitosComUltimoAnoParcialTerminaNaVidaUtil() {
        // 13 meses: ano 1 peso 2, ano 2 (1 mes) peso 1/12 -> denominador 25
        assertEquals(bd("288.00"), DepreciacaoCalculadora.cota("SOMA_DIGITOS", bd("3600"), BigDecimal.ZERO, 13, null, 0));
        assertEquals(bd("144.00"), DepreciacaoCalculadora.cota("SOMA_DIGITOS", bd("3600"), bd("3456"), 13, null, 12));
        assertEquals(bd("0.00"), DepreciacaoCalculadora.cota("SOMA_DIGITOS", bd("3600"), bd("3600"), 13, null, 13));
    }

    @Test
    void saldoDecrescenteUsaTaxaDuplaENuncaFicaAbaixoDoLinear() {
        // vida 60 meses -> taxa 40% a.a. sobre o saldo
        assertEquals(bd("400.00"), DepreciacaoCalculadora.cota("SALDO_DECRESCENTE", bd("12000"), BigDecimal.ZERO, 60, null, 0));
        // perto do fim o linear sobre o restante e' maior
        assertEquals(bd("500.00"), DepreciacaoCalculadora.cota("SALDO_DECRESCENTE", bd("12000"), bd("11000"), 60, null, 58));
    }
}
