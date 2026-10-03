package br.com.brasil_saas.financeiro.model;
import br.com.brasil_saas.shared.model.AuditableEntity;
import jakarta.persistence.*; import lombok.*;
import java.math.BigDecimal;
@Entity @Table(name="bc_fin_orcamento", schema="brasil_saas") @Getter @Setter @NoArgsConstructor
public class Orcamento extends AuditableEntity {
    @Column(nullable=false) private Integer ano;
    @Column(name="centro_custo_id") private Long centroCustoId;
    @Column(name="plano_contas_id") private Long planoContasId;
    @Column(name="valor_orcado", precision=15, scale=2, nullable=false) private BigDecimal valorOrcado;
}
