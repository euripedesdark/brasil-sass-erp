package br.com.brasil_saas.rh.model;
import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*; import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
@Entity @Table(name="bc_rh_ferias", schema="brasil_saas") @Getter @Setter
public class Ferias extends TenantEntity {
    @Column(name="funcionario_id", nullable=false) private Long funcionarioId;
    @Column(name="data_inicio", nullable=false) private LocalDate dataInicio;
    @Column(nullable=false) private Integer dias = 30;
    @Column(name="data_fim", nullable=false) private LocalDate dataFim;
    @Column(nullable=false, length=20) private String status = "PROGRAMADA";
    @Column(name="valor_ferias", precision=15, scale=2, nullable=false) private BigDecimal valorFerias = BigDecimal.ZERO;
    @Column(name="valor_terco", precision=15, scale=2, nullable=false) private BigDecimal valorTerco = BigDecimal.ZERO;
    @Column(name="folha_id") private Long folhaId;
    @Column(length=500) private String observacao;
}
