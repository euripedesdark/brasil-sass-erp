package br.com.brasil_saas.financeiro.model;
import br.com.brasil_saas.shared.model.AuditableEntity;
import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="bc_fin_conciliacao_item", schema="brasil_saas") @Getter @Setter @NoArgsConstructor
public class ConciliacaoItem extends AuditableEntity {
    @Column(name="conciliacao_id", nullable=false) private Long conciliacaoId;
    @Column(name="extrato_id", nullable=false) private Long extratoId;
    @Column(name="baixa_id") private Long baixaId;
    @Column(length=20) private String status = "PENDENTE";
}
