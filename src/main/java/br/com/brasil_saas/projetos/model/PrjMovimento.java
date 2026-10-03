package br.com.brasil_saas.projetos.model;
import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*; import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
@Entity @Table(name="bc_prj_movimento", schema="brasil_saas") @Getter @Setter
public class PrjMovimento extends TenantEntity {
    @Column(name="projeto_id", nullable=false) private Long projetoId;
    @Column(name="etapa_id") private Long etapaId;
    @Column(nullable=false, length=10) private String tipo;
    @Column(nullable=false, length=500) private String descricao;
    @Column(nullable=false, precision=15, scale=2) private BigDecimal valor;
    @Column(nullable=false) private LocalDate data;
    @Column(name="origem_tipo", length=60) private String origemTipo;
    @Column(name="origem_id") private Long origemId;
}
