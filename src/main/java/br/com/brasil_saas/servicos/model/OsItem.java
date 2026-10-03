package br.com.brasil_saas.servicos.model;
import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*; import lombok.*; import java.math.BigDecimal;
@Entity @Table(name="bc_srv_os_item", schema="brasil_saas") @Getter @Setter
public class OsItem extends TenantEntity {
    @Column(name="os_id", nullable=false) private Long osId;
    @Column(name="produto_id") private Long produtoId;
    @Column(name="servico_id") private Long servicoId;
    @Column(nullable=false, precision=15, scale=3) private BigDecimal quantidade;
    @Column(name="valor_unitario", nullable=false, precision=15, scale=4) private BigDecimal valorUnitario;
    @Column(name="valor_total", nullable=false, precision=15, scale=2) private BigDecimal valorTotal;
}
