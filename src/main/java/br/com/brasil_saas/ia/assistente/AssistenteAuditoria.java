package br.com.brasil_saas.ia.assistente;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;

/**
 * Uma pergunta ao assistente, e o que ele respondeu com o que usou.
 *
 * <p>Existe porque a arquitetura põe o modelo por último, e essa ordem precisa
 * ser conferível depois. Sem este registro, uma resposta boa e uma resposta
 * inventada pelo modelo são indistinguíveis. Com ele, dá para responder "de onde
 * saiu isso" e "por que o modelo foi chamado quando a busca fiscal já
 * resolvia".
 */
@Entity
@Table(name = "bc_ia_assistente_auditoria")
@Getter
@Setter
public class AssistenteAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Inicializado aqui, e não só no banco. A coluna tem
     * {@code DEFAULT gen_random_uuid()}, mas o Hibernate envia o valor da
     * entidade explicitamente — e sem inicializar aqui ele envia NULL, e a
     * coluna NOT NULL barra. Foi o que aconteceu na primeira gravacao.
     */
    @Column(nullable = false)
    private UUID uuid = UUID.randomUUID();

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "usuario_id")
    private Long usuarioId;

    /**
     * Quem perguntou, pelo nome.
     *
     * <p>Existe porque o id não serve. A primeira versão tentou
     * {@code Long.valueOf(getUsername())}, e o nome nunca é o id — é
     * "euripedes", não 4. E com o roteamento por {@code bc_core_auth_source} um
     * usuário de AD pode nem existir em {@code bc_core_usuario}, então depender
     * de JOIN é depender do esquema de autenticação mudar por baixo.
     */
    @Column(name = "usuario_nome", length = 256)
    private String usuarioNome;

    /**
     * O {@code identityId} do contrato oficial do Auth Service.
     *
     * <p>É a âncora estável, e fica NULL enquanto o {@code IdentityClient} não
     * existir. Username é rótulo: o mesmo nome pode entrar por AD, Postgres ou
     * Linux — é o que a tabela {@code bc_core_auth_source} roteia — e pode mudar
     * de valor. Preencher esta coluna com o username seria reintroduzir a
     * ambiguidade que o {@code usuario_nome} não resolveu.
     */
    @Column(name = "identity_id", length = 128)
    private String identityId;

    @Column(nullable = false, columnDefinition = "text")
    private String pergunta;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> contexto;

    @Column(length = 200)
    private String fontes;

    @Column(length = 120)
    private String modelo;

    /** O texto cru do modelo, separado da resposta final. */
    @Column(name = "resposta_modelo", columnDefinition = "text")
    private String respostaModelo;

    @Column(nullable = false, columnDefinition = "text")
    private String resposta;

    /** {@code ok}, {@code modelo-indisponivel} ou {@code erro}. */
    @Column(nullable = false, length = 32)
    private String status = "ok";

    @Column(columnDefinition = "text")
    private String erro;

    @Column(name = "duracao_ms")
    private Long duracaoMs;

    @Column(name = "tokens_entrada")
    private Integer tokensEntrada;

    @Column(name = "tokens_saida")
    private Integer tokensSaida;

@Column(name = "created_at", nullable = false)
    private java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();
}
