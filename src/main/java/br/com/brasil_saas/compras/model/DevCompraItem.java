package br.com.brasil_saas.compras.model;
import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*; import lombok.*;
import java.math.BigDecimal;
@Entity @Table(name="bc_cmp_devolucao_item", schema="brasil_saas") @Getter @Setter
public class DevCompraItem extends TenantEntity {
    @Column(name="devolucao_id", nullable=false) private Long devolucaoId;
    @Column(name="produto_id") private Long produtoId;
    @Column(nullable=false, precision=15, scale=3) private BigDecimal quantidade = BigDecimal.ZERO;
}
