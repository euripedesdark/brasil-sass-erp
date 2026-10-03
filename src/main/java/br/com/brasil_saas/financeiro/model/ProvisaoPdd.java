package br.com.brasil_saas.financeiro.model;
import br.com.brasil_saas.shared.model.AuditableEntity;
import jakarta.persistence.*; import lombok.*;
import java.math.BigDecimal; import java.time.LocalDate;
@Entity @Table(name="bc_fin_provisao_pdd", schema="brasil_saas") @Getter @Setter @NoArgsConstructor
public class ProvisaoPdd extends AuditableEntity {
    @Column(name="data_provisao", nullable=false) private LocalDate dataProvisao;
    @Column(name="valor_provisao", precision=15, scale=2, nullable=false) private BigDecimal valorProvisao;
    @Column(columnDefinition="text") private String observacao;
}
