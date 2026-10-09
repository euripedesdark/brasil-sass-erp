package br.com.brasil_saas.enterprise.service;

import br.com.brasil_saas.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PlanejadorCargaTest {
    private static PlanejadorCarga.Entrega e(long id, String destino, String peso, String vol) {
        return new PlanejadorCarga.Entrega("PEDIDO_VENDA", id, destino, new BigDecimal(peso), new BigDecimal(vol));
    }
    private static final BigDecimal CAP_P = new BigDecimal("1000"), CAP_V = new BigDecimal("10");

    @Test void agrupaNoMesmoDestinoRespeitandoPeso() {
        var cargas = PlanejadorCarga.planejar(List.of(e(1, "Cuiabá", "600", "2"), e(2, "Cuiabá", "300", "2"), e(3, "Cuiabá", "500", "2")), CAP_P, CAP_V);
        assertEquals(2, cargas.size());
        cargas.forEach(c -> assertTrue(c.peso().compareTo(CAP_P) <= 0));
    }
    @Test void destinosDiferentesNaoMisturam() {
        var cargas = PlanejadorCarga.planejar(List.of(e(1, "Cuiabá", "100", "1"), e(2, "Sinop", "100", "1")), CAP_P, CAP_V);
        assertEquals(2, cargas.size());
    }
    @Test void volumeTambemLimita() {
        var cargas = PlanejadorCarga.planejar(List.of(e(1, "Cuiabá", "10", "6"), e(2, "Cuiabá", "10", "6")), CAP_P, CAP_V);
        assertEquals(2, cargas.size());
    }
    @Test void entregaMaiorQueOVeiculoERecusada() {
        assertThrows(BusinessException.class, () -> PlanejadorCarga.planejar(List.of(e(1, "Cuiabá", "1500", "1")), CAP_P, CAP_V));
    }
    @Test void capacidadeInvalidaERecusada() {
        assertThrows(BusinessException.class, () -> PlanejadorCarga.planejar(List.of(e(1, "X", "1", "1")), BigDecimal.ZERO, CAP_V));
    }
}
