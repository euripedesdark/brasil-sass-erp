package br.com.brasil_saas.compras.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

/** Tolerância cadastral de conferência: VALOR fixo ou PERCENTUAL sobre o pedido. */
@Entity
@Table(name = "bc_com_tolerancia", schema = "brasil_saas")
@Getter @Setter @NoArgsConstructor
public class ToleranciaConferencia extends TenantEntity {

    @Column(nullable = false, length = 12)
    private String tipo = "VALOR";
    @Column(precision = 15, scale = 4, nullable = false)
    private BigDecimal limite;
    @Column(name = "produto_id")
    private Long produtoId;
    @Column(nullable = false)
    private Boolean ativo = true;
}
