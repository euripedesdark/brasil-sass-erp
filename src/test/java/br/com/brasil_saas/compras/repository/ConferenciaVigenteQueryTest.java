package br.com.brasil_saas.compras.repository;

import br.com.brasil_saas.compras.model.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ConferenciaVigenteQueryTest {
    private SessionFactory factory;
    private Session session;
    private String query;
    private Long pedidoId;

    @BeforeEach
    void preparar() throws Exception {
        factory = new Configuration().addAnnotatedClass(PedidoCompra.class)
                .addAnnotatedClass(ItemPedidoCompra.class).addAnnotatedClass(ConferenciaFaturaCompra.class)
                .setProperty("hibernate.connection.driver_class", "org.h2.Driver")
                .setProperty("hibernate.connection.url", "jdbc:h2:mem:" + UUID.randomUUID()
                        + ";INIT=CREATE SCHEMA IF NOT EXISTS brasil_saas")
                .setProperty("hibernate.hbm2ddl.auto", "create-drop")
                .setProperty("hibernate.hbm2ddl.halt_on_error", "true")
                .buildSessionFactory();
        session = factory.withOptions().tenantIdentifier(1L).openSession();
        session.beginTransaction();
        query = ConferenciaFaturaCompraRepository.class
                .getMethod("findVigentesParaTitulo", Long.class, Long.class).getAnnotation(Query.class).value();
        var pedido = new PedidoCompra();
        pedido.setEmpresaId(1L); pedido.setFornecedorId(1L); pedido.setDataEmissao(LocalDate.now());
        pedido.setStatus("RECEBIDO"); pedido.setTituloId(10L);
        session.persist(pedido); pedidoId = pedido.getId();
    }

    @AfterEach
    void fechar() {
        if (session != null) { session.getTransaction().rollback(); session.close(); }
        if (factory != null) factory.close();
    }

    private ConferenciaFaturaCompra gravar(Long documento, String status, Long titulo) {
        var c = new ConferenciaFaturaCompra();
        c.setEmpresaId(1L); c.setPedidoId(pedidoId); c.setRecebimentoId(20L);
        c.setNfeId(documento); c.setTituloId(titulo); c.setStatus(status);
        session.persist(c); session.flush(); return c;
    }

    private List<ConferenciaFaturaCompra> buscar(Long empresa, Long titulo) {
        return session.createQuery(query, ConferenciaFaturaCompra.class)
                .setParameter("empresaId", empresa).setParameter("tituloId", titulo).getResultList();
    }

    @Test
    void reavaliacaoNaoLiberaDivergenciaDeOutroDocumento() {
        gravar(30L, "DIVERGENTE", null);
        var outro = gravar(31L, "DIVERGENTE", null);
        var aprovada = gravar(30L, "APROVADA", null);
        var vigentes = buscar(1L, 10L);
        assertEquals(2, vigentes.size());
        assertTrue(vigentes.contains(outro)); assertTrue(vigentes.contains(aprovada));
    }

    @Test
    void exclusaoLogicaNaoSubstituiConferenciaAnterior() {
        var divergente = gravar(30L, "DIVERGENTE", 10L);
        var excluida = gravar(30L, "APROVADA", 10L);
        excluida.setDeletedAt(LocalDateTime.now()); session.flush();
        assertEquals(List.of(divergente), buscar(1L, 10L));
    }

    @Test
    void consultaRespeitaEmpresaETitulo() {
        gravar(30L, "DIVERGENTE", null);
        assertTrue(buscar(2L, 10L).isEmpty());
        assertTrue(buscar(1L, 99L).isEmpty());
        assertEquals(1, buscar(1L, 10L).size());
    }
}
