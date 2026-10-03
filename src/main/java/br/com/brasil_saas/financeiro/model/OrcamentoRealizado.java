package br.com.brasil_saas.financeiro.model;
import br.com.brasil_saas.shared.model.AuditableEntity;
import jakarta.persistence.*; import lombok.*;
import java.math.BigDecimal;
@Entity @Table(name="bc_fin_orcamento_realizado", schema="brasil_saas") @Getter @Setter @NoArgsConstructor
public class OrcamentoRealizado extends AuditableEntity {
    @Column(name="orcamento_id", nullable=false) private Long orcamentoId;
    @Column(nullable=false) private Integer mes;
    @Column(name="valor_realizado", precision=15, scale=2, nullable=false) private BigDecimal valorRealizado = BigDecimal.ZERO;
}
