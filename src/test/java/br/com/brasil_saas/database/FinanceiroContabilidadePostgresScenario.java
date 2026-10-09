package br.com.brasil_saas.database;

import br.com.brasil_saas.contabilidade.repository.*;
import br.com.brasil_saas.contabilidade.service.impl.ContabilidadeServiceImpl;
import br.com.brasil_saas.financeiro.model.PlanoContas;
import br.com.brasil_saas.financeiro.repository.*;
import org.hibernate.Session;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

/** Confere emissao contabil, estorno e fechamento com dados sinteticos e rollback. */
final class FinanceiroContabilidadePostgresScenario {
    static void validar(Session session, Long tituloId) {
        long empresa = 900001L;
        var factory = new JpaRepositoryFactory(session);
        var contas = factory.getRepository(PlanoContasRepository.class);
        var debito = conta("1.01.TESTE", "D"); contas.save(debito);
        var credito = conta("2.01.TESTE", "C"); contas.save(credito);
        var lancamentos = factory.getRepository(CtbLancamentoRepository.class);
        var partidas = factory.getRepository(CtbPartidaRepository.class);
        var fechamentos = factory.getRepository(CtbFechamentoRepository.class);
        var service = new ContabilidadeServiceImpl(lancamentos, partidas, fechamentos, contas,
                factory.getRepository(TituloRepository.class));
        var contabil = service.gerarDeTitulo(empresa, null, tituloId, debito.getId(), credito.getId());
        session.flush();
        assertEquals("LANCADO", contabil.getStatus());
        var valores = service.totais(empresa, contabil.getId());
        igual("100", valores[0]); igual("100", valores[1]);
        assertThrows(ResponseStatusException.class,
                () -> service.gerarDeTitulo(empresa, null, tituloId, debito.getId(), credito.getId()));
        var estorno = service.estornar(empresa, contabil.getId(), "Teste sintetico de integridade");
        session.flush(); session.clear();
        assertEquals("ESTORNADO", lancamentos.findByIdAndEmpresaIdAndDeletedAtIsNull(contabil.getId(), empresa).orElseThrow().getStatus());
        assertEquals("LANCADO", lancamentos.findByIdAndEmpresaIdAndDeletedAtIsNull(estorno.getId(), empresa).orElseThrow().getStatus());
        LocalDate inicio = contabil.getData().withDayOfMonth(1);
        LocalDate fim = LocalDate.now().withDayOfMonth(1).plusMonths(1).minusDays(1);
        var balancete = service.balancete(empresa, inicio, fim);
        assertEquals(2, balancete.size());
        for (var linha : balancete) {
            igual("100", (BigDecimal) linha.get("debito"));
            igual("100", (BigDecimal) linha.get("credito"));
            igual("0", (BigDecimal) linha.get("saldo"));
        }
        var razao = service.razao(empresa, debito.getId(), inicio, fim);
        assertEquals(2, razao.size()); igual("0", (BigDecimal) razao.get(razao.size() - 1).get("saldo"));
        String periodo = String.format("%04d-%02d", LocalDate.now().getYear(), LocalDate.now().getMonthValue());
        var fechamento = service.fechar(empresa, null, periodo);
        assertEquals("FECHADO", fechamento.getStatus());
        assertThrows(ResponseStatusException.class, () -> service.estornar(empresa, estorno.getId(), "Periodo fechado"));
        service.reabrir(empresa, periodo);
        session.flush();
        assertEquals("ABERTO", fechamentos.findByEmpresaIdAndPeriodoAndDeletedAtIsNull(empresa, periodo).orElseThrow().getStatus());
    }
    private static PlanoContas conta(String codigo, String natureza) {
        var conta = new PlanoContas(); conta.setEmpresaId(900001L); conta.setCodigo(codigo);
        conta.setDescricao("Conta sintetica de validacao"); conta.setTipo("A"); conta.setNatureza(natureza);
        return conta;
    }
    private static void igual(String esperado, BigDecimal atual) { assertEquals(0, new BigDecimal(esperado).compareTo(atual)); }
}
