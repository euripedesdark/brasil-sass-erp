package br.com.brasil_saas.shared.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.TenantId;

/**
 * A base de tudo que pertence a uma empresa.
 *
 * <p><b>O que o {@link TenantId} faz aqui.</b> O Hibernate passa a acrescentar
 * {@code empresa_id = :tenant} em <b>toda</b> query de uma entidade que herda
 * daqui — sem derived query, sem Specification, sem Criteria, sem JSPQL
 * escrito a mao. As 84 entidades que estendem esta classe passam a ser
 * filtradas de uma vez.
 *
 * <p><b>Por que nao foi feito endpoint por endpoint.</b> Foi assim que o
 * sistema chegou aqui. O filtro era manual, em cada controller, e o resultado
 * esta no {@code ProdutoController}: {@code criar} filtra, {@code buscarPorId}
 * nao, {@code atualizar} nao, {@code excluir} nao. Um furo por esquecimento, e o
 * esquecimento nao aparece em nenhum teste — o codigo do controller continua
 * compilando e passando. Com {@code @TenantId} o furo nao depende de ningem
 * lembrar: o esquema e a anotacao.
 *
 * <p><b>Quem escapa do filtro.</b> O superuser, e so ele. O
 * {@link br.com.brasil_saas.shared.tenant.EmpresaTenantIdentifierResolver} devolve
 * {@code null} para o superuser, e no Hibernate 6 {@code null} significa "nao
 * aplicar o filtro" — a query sai sem clausula de empresa.
 *
 * <p><b>E o superuser que grava?</b> Ele ve tudo, mas nao pertence a nenhuma
 * empresa, entao nao tem {@code empresa_id} para gravar. A gravacao de um
 * superuser exige que a empresa destino seja informada, e
 * {@code EmpresaTenantIdentifierResolver.empresaParaGravar()} recusa sem isso.
 * Sem essa recusa, o superuser gravaria linha com {@code empresa_id} nulo, que
 * existe no banco e que nenhuma empresa consegue encontrar.
 *
 * <p><b>O filtro nao protege a leitura crua.</b> Consultas nativas
 * ({@code JdbcTemplate}, {@code @Query(nativeQuery = true)}) nao passam pelo
 * Hibernate e precisam do {@code WHERE empresa_id = ?} escrito a mao. Onde
 * ja existe, esta certo.
 */
@MappedSuperclass
@Getter
@Setter
public abstract class TenantEntity extends BaseEntity {

    @TenantId
    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;
}
