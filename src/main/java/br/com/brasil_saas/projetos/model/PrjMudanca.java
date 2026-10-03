package br.com.brasil_saas.projetos.model;
import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*; import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity @Table(name="bc_prj_mudanca", schema="brasil_saas") @Getter @Setter
public class PrjMudanca extends TenantEntity {
    @Column(name="projeto_id", nullable=false) private Long projetoId;
    @Column(nullable=false, length=500) private String descricao;
    @Column(nullable=false, length=20) private String status = "SOLICITADA";
    @Column(name="impacto_valor", precision=15, scale=2, nullable=false) private BigDecimal impactoValor = BigDecimal.ZERO;
    @Column(name="decidida_por") private Long decididaPor;
    @Column(name="decidida_em") private LocalDateTime decididaEm;
}
