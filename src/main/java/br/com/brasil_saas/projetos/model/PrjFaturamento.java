package br.com.brasil_saas.projetos.model;
import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*; import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
@Entity @Table(name="bc_prj_faturamento", schema="brasil_saas") @Getter @Setter
public class PrjFaturamento extends TenantEntity {
    @Column(name="projeto_id", nullable=false) private Long projetoId;
    @Column(nullable=false, length=500) private String descricao;
    @Column(nullable=false, precision=15, scale=2) private BigDecimal valor;
    @Column(name="data_prevista") private LocalDate dataPrevista;
    @Column(name="data_faturado") private LocalDate dataFaturado;
    @Column(nullable=false, length=20) private String status = "PREVISTO";
    @Column(name="titulo_id") private Long tituloId;
}
