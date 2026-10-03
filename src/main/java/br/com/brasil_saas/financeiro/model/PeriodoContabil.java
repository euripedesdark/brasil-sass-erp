package br.com.brasil_saas.financeiro.model;
import br.com.brasil_saas.shared.model.AuditableEntity;
import jakarta.persistence.*; import lombok.*;
import java.time.LocalDate;
@Entity @Table(name="bc_fin_periodo_contabil", schema="brasil_saas") @Getter @Setter @NoArgsConstructor
public class PeriodoContabil extends AuditableEntity {
    @Column(name="data_inicio", nullable=false) private LocalDate dataInicio;
    @Column(name="data_fim", nullable=false) private LocalDate dataFim;
    @Column(length=20) private String status = "ABERTO";
}
