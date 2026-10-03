package br.com.brasil_saas.contabilidade.model;
import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*; import lombok.*;
import java.math.BigDecimal;
@Entity @Table(name="bc_ctb_partida", schema="brasil_saas") @Getter @Setter
public class CtbPartida extends TenantEntity {
    @Column(name="lancamento_id", nullable=false) private Long lancamentoId;
    @Column(name="conta_id", nullable=false) private Long contaId;
    @Column(name="centro_custo_id") private Long centroCustoId;
    @Column(nullable=false, precision=15, scale=2) private BigDecimal debito = BigDecimal.ZERO;
    @Column(nullable=false, precision=15, scale=2) private BigDecimal credito = BigDecimal.ZERO;
    @Column(length=500) private String historico;
}
