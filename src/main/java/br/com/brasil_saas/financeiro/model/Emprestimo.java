package br.com.brasil_saas.financeiro.model;
import br.com.brasil_saas.shared.model.AuditableEntity;
import jakarta.persistence.*; import lombok.*;
import java.math.BigDecimal; import java.time.LocalDate;
@Entity @Table(name="bc_fin_emprestimo", schema="brasil_saas") @Getter @Setter @NoArgsConstructor
public class Emprestimo extends AuditableEntity {
    @Column(length=100, nullable=false) private String instituicao;
    @Column(name="valor_total", precision=15, scale=2, nullable=false) private BigDecimal valorTotal;
    @Column(name="taxa_juros", precision=10, scale=4) private BigDecimal taxaJuros;
    @Column(name="data_contratacao", nullable=false) private LocalDate dataContratacao;
    @Column(name="data_quitacao") private LocalDate dataQuitacao;
    @Column(length=20) private String status = "ATIVO";
}
