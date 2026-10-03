package br.com.brasil_saas.financeiro.model;
import br.com.brasil_saas.shared.model.AuditableEntity;
import jakarta.persistence.*; import lombok.*;
import java.math.BigDecimal;
@Entity @Table(name="bc_fin_analise_rentabilidade", schema="brasil_saas") @Getter @Setter @NoArgsConstructor
public class AnaliseRentabilidade extends AuditableEntity {
    @Column(name="centro_custo_id") private Long centroCustoId;
    @Column(length=20, nullable=false) private String periodo;
    @Column(precision=15, scale=2) private BigDecimal receita = BigDecimal.ZERO;
    @Column(precision=15, scale=2) private BigDecimal despesa = BigDecimal.ZERO;
    @Column(name="margem_lucro", precision=15, scale=4) private BigDecimal margemLucro = BigDecimal.ZERO;
}
