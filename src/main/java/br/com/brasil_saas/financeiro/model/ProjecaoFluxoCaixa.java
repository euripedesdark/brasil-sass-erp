package br.com.brasil_saas.financeiro.model;
import br.com.brasil_saas.shared.model.AuditableEntity;
import jakarta.persistence.*; import lombok.*;
import java.math.BigDecimal; import java.time.LocalDate;
@Entity @Table(name="bc_fin_projecao_fluxo_caixa", schema="brasil_saas") @Getter @Setter @NoArgsConstructor
public class ProjecaoFluxoCaixa extends AuditableEntity {
    @Column(name="data_projecao", nullable=false) private LocalDate dataProjecao;
    @Column(name="valor_previsto_entrada", precision=15, scale=2) private BigDecimal valorPrevistoEntrada = BigDecimal.ZERO;
    @Column(name="valor_previsto_saida", precision=15, scale=2) private BigDecimal valorPrevistoSaida = BigDecimal.ZERO;
}
