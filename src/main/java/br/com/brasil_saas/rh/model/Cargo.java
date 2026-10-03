package br.com.brasil_saas.rh.model;
import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
@Entity @Table(name="bc_rh_cargo", schema="brasil_saas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Cargo extends TenantEntity {
    @Column(length=100, nullable=false) private String nome;
    @Column(precision=15, scale=2) private BigDecimal salarioBase = BigDecimal.ZERO;
    @Column(nullable=false) private Boolean ativo = true;
}
