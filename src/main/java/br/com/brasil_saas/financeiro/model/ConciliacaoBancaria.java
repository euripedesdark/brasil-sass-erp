package br.com.brasil_saas.financeiro.model;
import br.com.brasil_saas.shared.model.AuditableEntity;
import jakarta.persistence.*; import lombok.*;
import java.time.LocalDate;
@Entity @Table(name="bc_fin_conciliacao_bancaria", schema="brasil_saas") @Getter @Setter @NoArgsConstructor
public class ConciliacaoBancaria extends AuditableEntity {
    @Column(name="conta_bancaria_id", nullable=false) private Long contaBancariaId;
    @Column(name="data_inicio", nullable=false) private LocalDate dataInicio;
    @Column(name="data_fim", nullable=false) private LocalDate dataFim;
    @Column(length=20) private String status = "EM_ABERTO";
}
