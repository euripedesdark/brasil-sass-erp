package br.com.brasil_saas.financeiro.model;
import br.com.brasil_saas.shared.model.AuditableEntity;
import jakarta.persistence.*; import lombok.*;
import java.math.BigDecimal;
@Entity @Table(name="bc_fin_conta_bancaria", schema="brasil_saas") @Getter @Setter @NoArgsConstructor
public class ContaBancaria extends AuditableEntity {
    @Column(length=100, nullable=false) private String banco;
    @Column(length=20, nullable=false) private String agencia;
    @Column(length=50, nullable=false) private String conta;
    @Column(length=5) private String digito;
    @Column(length=50, nullable=false) private String tipo;
    @Column(name="saldo_inicial", precision=15, scale=2, nullable=false) private BigDecimal saldoInicial = BigDecimal.ZERO;
    @Column private Boolean ativa = true;
}
