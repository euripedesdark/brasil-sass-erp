package br.com.brasil_saas.compras.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

/** Alçada de aprovação de pedido de compra por empresa. */
@Entity
@Table(name = "bc_com_alcada", schema = "brasil_saas")
@Getter @Setter @NoArgsConstructor
public class AlcadaAprovacao extends TenantEntity {

    @Column(name = "valor_limite", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorLimite;
    @Column(nullable = false)
    private Boolean ativo = true;
}
