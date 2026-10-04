package br.com.brasil_saas.vendas.devolucao;
import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*; import lombok.*;
import java.math.BigDecimal;
@Entity @Table(name="bc_ven_devolucao_item", schema="brasil_saas") @Getter @Setter
public class VenDevolucaoItem extends TenantEntity {
    @Column(name="devolucao_id", nullable=false) private Long devolucaoId;
    @Column(name="produto_id", nullable=false) private Long produtoId;
    @Column(nullable=false, precision=15, scale=4) private BigDecimal quantidade;
    @Column(name="qtd_recebida", precision=15, scale=3, nullable=false) private BigDecimal qtdRecebida = BigDecimal.ZERO;
}
