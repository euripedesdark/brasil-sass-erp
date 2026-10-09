package br.com.brasil_saas.database;

import br.com.brasil_saas.fiscal.model.Nfse;
import br.com.brasil_saas.fiscal.model.Nfe;
import br.com.brasil_saas.fiscal.repository.NfeRepository;
import br.com.brasil_saas.fiscal.model.Reinf;
import br.com.brasil_saas.fiscal.repository.NfseRepository;
import br.com.brasil_saas.fiscal.repository.ReinfRepository;
import br.com.brasil_saas.fiscal.service.ReinfService;
import br.com.brasil_saas.shared.exception.BusinessException;
import org.hibernate.Session;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/** Datas limítrofes, emissão, geração e fechamento locais com rollback. */
final class FiscalCompetenciasPostgresScenario {
    static void validar(Session session) throws Exception {
        var factory = new JpaRepositoryFactory(session);
        var notas = factory.getRepository(NfseRepository.class);
        var eventos = factory.getRepository(ReinfRepository.class);
        notas.save(nota("2026-03-01T00:00:00", "EMITIDA", "100"));
        notas.save(nota("2026-03-31T23:59:59.999999", "AUTORIZADA", "200"));
        notas.save(nota("2026-04-01T00:00:00", "EMITIDA", "900"));
        notas.save(nota("2026-02-28T23:59:59", "EMITIDA", "800"));
        notas.save(nota("2026-03-15T12:00:00", "FALHA_EMISSAO", "700"));
        session.flush();
        var inicio = LocalDateTime.parse("2026-03-01T00:00:00");
        var fim = inicio.plusMonths(1);
        assertEquals(3, notas.findNoPeriodo(900001L, inicio, fim).size());
        assertTrue(notas.findNoPeriodo(900002L, inicio, fim).isEmpty());
        var nfes = factory.getRepository(NfeRepository.class);
        nfes.save(nfe("2026-03-01T00:00:00", "S"));
        nfes.save(nfe("2026-04-01T00:00:00", "S"));
        nfes.save(nfe("2026-03-15T12:00:00", "E"));
        session.flush();
        assertEquals(1, nfes.findNoPeriodoPorTipo(900001L, "S", inicio, fim).size());
        assertEquals(1, nfes.findNoPeriodoPorTipo(900001L, "E", inicio, fim).size());
        assertTrue(nfes.findNoPeriodoPorTipo(900002L, "S", inicio, fim).isEmpty());
        var service = new ReinfService(eventos, notas);
        var gerados = service.gerarPeriodo(900001L, "03/2026");
        session.flush();
        var prestados = gerados.stream().filter(e -> "R-2020".equals(e.getEvento())).findFirst().orElseThrow();
        assertEquals(2, prestados.getTotalDocs());
        assertEquals(0, new BigDecimal("300").compareTo(prestados.getValorTotal()));
        var json = new com.fasterxml.jackson.databind.ObjectMapper().readTree(prestados.getPayload());
        assertEquals("A\n\tB", json.get("nfs").get(0).get("serieRps").asText());
        assertEquals(prestados.getId(), service.gerarPeriodo(900001L, "03/2026").get(0).getId());
        session.flush();
        assertEquals(2, eventos.findByEmpresaIdAndCompetencia(900001L, "03/2026").size());
        var fechado = service.fechar(900001L, "03/2026");
        session.flush(); session.clear();
        assertEquals(Reinf.FECHADO, fechado.getStatus());
        assertEquals(fechado.getId(), service.fechar(900001L, "03/2026").getId());
        assertThrows(BusinessException.class, () -> service.gerarPeriodo(900001L, "03/2026"));
        assertEquals(3, eventos.findByEmpresaIdAndCompetencia(900001L, "03/2026").size());
        assertTrue(eventos.findByIdAndEmpresaIdAndDeletedAtIsNull(fechado.getId(), 900002L).isEmpty());
    }
    private static Nfe nfe(String data, String tipo) {
        var n = new Nfe(); n.setEmpresaId(900001L); n.setStatus("AUTORIZADA");
        n.setTipoOperacao(tipo); n.setDataEmissao(LocalDateTime.parse(data));
        n.setValorProdutos(BigDecimal.TEN); n.setValorTotal(BigDecimal.TEN);
        n.setValorFrete(BigDecimal.ZERO); n.setValorDesconto(BigDecimal.ZERO);
        n.setValorIcms(BigDecimal.ZERO); n.setValorIpi(BigDecimal.ZERO);
        n.setValorPis(BigDecimal.ZERO); n.setValorCofins(BigDecimal.ZERO);
        return n;
    }
    private static Nfse nota(String data, String status, String valor) {
        var n = new Nfse(); n.setEmpresaId(900001L); n.setStatus(status);
        n.setDataEmissao(LocalDateTime.parse(data)); n.setSerieRps("A\n\tB");
        n.setValorTotal(new BigDecimal(valor)); n.setBaseCalculo(new BigDecimal(valor));
        n.setAliquotaIss(new BigDecimal("5")); n.setValorIss(new BigDecimal(valor).multiply(new BigDecimal("0.05")));
        return n;
    }
}
