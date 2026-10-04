package br.com.brasil_saas.producao.model;
import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*; import lombok.*;
import java.math.BigDecimal;
@Entity @Table(name="bc_pcp_mps", schema="brasil_saas") @Getter @Setter
public class MpsItem extends TenantEntity {
    @Column(nullable=false, length=7) private String periodo;
    @Column(name="produto_id", nullable=false) private Long produtoId;
    @Column(name="qtd_demandada", precision=15, scale=3, nullable=false) private BigDecimal qtdDemandada = BigDecimal.ZERO;
    @Column(name="qtd_estoque", precision=15, scale=3, nullable=false) private BigDecimal qtdEstoque = BigDecimal.ZERO;
    @Column(name="qtd_planejada", precision=15, scale=3, nullable=false) private BigDecimal qtdPlanejada = BigDecimal.ZERO;
    @Column(length=20) private String origem = "PEDIDOS";
    @Column(nullable=false, length=20) private String status = "RASCUNHO";
}
