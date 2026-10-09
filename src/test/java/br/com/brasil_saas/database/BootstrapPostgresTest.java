package br.com.brasil_saas.database;

import jakarta.persistence.Entity;
import br.com.brasil_saas.compras.model.ContratoFornecimento;
import br.com.brasil_saas.contratosvenda.model.ContratoVenda;
import org.flywaydb.core.Flyway;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import java.sql.DriverManager;
import java.util.Map;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

/** Executar somente contra um banco PostgreSQL descartavel e vazio. */
@EnabledIfEnvironmentVariable(named = "ERP_BOOTSTRAP_TEST_URL", matches = "jdbc:postgresql:.*")
class BootstrapPostgresTest {
    @Test
    void instalaValidaEntidadesENaoReaplicaMigracoes() throws Exception {
        String url = System.getenv("ERP_BOOTSTRAP_TEST_URL");
        String user = System.getenv().getOrDefault("ERP_BOOTSTRAP_TEST_USER", "postgres");
        String password = System.getenv().getOrDefault("ERP_BOOTSTRAP_TEST_PASSWORD", "");
        try (var connection = DriverManager.getConnection(url, user, password);
             var statement = connection.createStatement();
             var rows = statement.executeQuery("select count(*) from information_schema.tables where table_schema in ('brasil_saas','dl')")) {
            rows.next();
            assertEquals(0, rows.getInt(1), "O teste exige banco vazio e nao altera bancos existentes");
        }
        Flyway flyway = Flyway.configure().dataSource(url, user, password)
                .schemas("brasil_saas").baselineOnMigrate(false)
                .configuration(Map.of("flyway.postgresql.transactional.lock", "false"))
                .locations("classpath:db/migration").load();
        assertTrue(flyway.migrate().migrationsExecuted >= 2);
        flyway.validate();
        assertEquals(0, flyway.migrate().migrationsExecuted);

        var registry = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.connection.url", url)
                .applySetting("hibernate.connection.username", user)
                .applySetting("hibernate.connection.password", password)
                .applySetting("hibernate.default_schema", "brasil_saas")
                // Spring Data abre EntityManagers adicionais para validar suas queries.
                // Todos pertencem exclusivamente a empresa sintetica deste teste.
                .applySetting("hibernate.tenant_identifier_resolver", new org.hibernate.context.spi.CurrentTenantIdentifierResolver<Long>() {
                    @Override public Long resolveCurrentTenantIdentifier() { return 900001L; }
                    @Override public boolean validateExistingCurrentSessions() { return true; }
                })
                .applySetting("hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy")
                .applySetting("hibernate.hbm2ddl.auto", "validate").build();
        try {
            var scanner = new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
            var sources = new MetadataSources(registry);
            var entities = scanner.findCandidateComponents("br.com.brasil_saas");
            assertTrue(entities.size() >= 227, "A validacao deve incluir todas as entidades atuais");
            for (var entity : entities) sources.addAnnotatedClass(Class.forName(entity.getBeanClassName()));
            try (var factory = sources.buildMetadata().buildSessionFactory()) {
                assertTrue(factory.isOpen());
                try (var session = factory.withOptions().tenantIdentifier(900001L).openSession()) {
                    var transaction = session.beginTransaction();
                    try {
                        session.doWork(connection -> {
                            try (var statement = connection.createStatement()) {
                                statement.executeUpdate("insert into brasil_saas.bc_core_empresa(id,razao_social,cnpj) values(900001,'Empresa sintetica de teste','00000000000000')");
                                statement.executeUpdate("insert into brasil_saas.bc_cad_pessoa(id,empresa_id,tipo,nome) values(900001,900001,'JURIDICA','Pessoa sintetica de teste')");
                                statement.executeUpdate("insert into brasil_saas.bc_cad_fornecedor(id,empresa_id,pessoa_id) values(900001,900001,900001)");
                                statement.executeUpdate("insert into brasil_saas.bc_cad_cliente(id,empresa_id,pessoa_id) values(900001,900001,900001)");
                            }
                        });
                        var compra = new ContratoFornecimento();
                        compra.setEmpresaId(900001L);
                        compra.setFornecedorId(900001L);
                        compra.setNumero("CT-TESTE");
                        compra.setVigenciaInicio(LocalDate.of(2026, 10, 9));
                        compra.setValorLiberado(new BigDecimal("25.00"));
                        session.persist(compra);
                        var venda = new ContratoVenda();
                        venda.setEmpresaId(900001L);
                        venda.setClienteId(900001L);
                        venda.setNumero("CV-TESTE");
                        venda.setTitulo("Contrato sintetico");
                        venda.setInicio(LocalDate.of(2026, 10, 9));
                        venda.setRenovacaoAuto(true);
                        venda.setValor(new BigDecimal("100.00"));
                        session.persist(venda);
                        session.flush();
                        session.clear();
                        var compraLida = session.get(ContratoFornecimento.class, compra.getId());
                        var vendaLida = session.get(ContratoVenda.class, venda.getId());
                        assertEquals(compra.getVigenciaInicio(), compraLida.getVigenciaInicio());
                        assertEquals(new BigDecimal("25.00"), compraLida.getValorLiberado());
                        assertNull(compraLida.getVigenciaFim());
                        assertEquals("Contrato sintetico", vendaLida.getTitulo());
                        assertTrue(vendaLida.getRenovacaoAuto());
                        assertEquals(new BigDecimal("100.00"), vendaLida.getValor());
                        VendasReservasPostgresScenario.validar(session);
                    } finally {
                        transaction.rollback();
                    }
                }
            }
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
