package br.com.brasil_saas.financeiro.model;
import br.com.brasil_saas.shared.model.AuditableEntity;
import jakarta.persistence.*; import lombok.*;
import java.math.BigDecimal;
@Entity @Table(name="bc_fin_lancamento_partida", schema="brasil_saas") @Getter @Setter @NoArgsConstructor
public class LancamentoPartida extends AuditableEntity {
    @Column(name="lancamento_id", nullable=false) private Long lancamentoId;
    @Column(name="plano_contas_id", nullable=false) private Long planoContasId;
    @Column(name="centro_custo_id") private Long centroCustoId;
    @Column(length=1, nullable=false, columnDefinition="bpchar(1)") private String tipo; // D|C
    @Column(precision=15, scale=2, nullable=false) private BigDecimal valor;
}
