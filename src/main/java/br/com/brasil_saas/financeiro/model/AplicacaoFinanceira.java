package br.com.brasil_saas.financeiro.model;
import br.com.brasil_saas.shared.model.AuditableEntity;
import jakarta.persistence.*; import lombok.*;
import java.math.BigDecimal; import java.time.LocalDate;
@Entity @Table(name="bc_fin_aplicacao_financeira", schema="brasil_saas") @Getter @Setter @NoArgsConstructor
public class AplicacaoFinanceira extends AuditableEntity {
    @Column(name="conta_bancaria_id") private Long contaBancariaId;
    @Column(length=100, nullable=false) private String descricao;
    @Column(name="valor_aplicado", precision=15, scale=2, nullable=false) private BigDecimal valorAplicado;
    @Column(name="taxa_juros", precision=10, scale=4) private BigDecimal taxaJuros;
    @Column(name="data_aplicacao", nullable=false) private LocalDate dataAplicacao;
    @Column(name="data_resgate") private LocalDate dataResgate;
}
