package br.com.brasil_saas.financeiro.model;
import br.com.brasil_saas.shared.model.AuditableEntity;
import jakarta.persistence.*; import lombok.*;
import java.math.BigDecimal; import java.time.LocalDate;
@Entity @Table(name="bc_fin_titulo_parcela", schema="brasil_saas") @Getter @Setter @NoArgsConstructor
public class TituloParcela extends AuditableEntity {
    @Column(name="titulo_id", nullable=false) private Long tituloId;
    @Column(name="numero_parcela", nullable=false) private Integer numeroParcela;
    @Column(name="valor_parcela", precision=15, scale=2, nullable=false) private BigDecimal valorParcela;
    @Column(name="valor_saldo", precision=15, scale=2, nullable=false) private BigDecimal valorSaldo;
    @Column(name="data_vencimento", nullable=false) private LocalDate dataVencimento;
    @Column(length=20) private String status = "ABERTO";
}
