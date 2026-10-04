package br.com.brasil_saas.projetos.model;
import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*; import lombok.*;
import java.time.LocalDate;
@Entity @Table(name="bc_prj_etapa", schema="brasil_saas") @Getter @Setter
public class PrjEtapa extends TenantEntity {
    @Column(name="projeto_id", nullable=false) private Long projetoId;
    @Column(name="pai_id") private Long paiId;
    @Column(name="codigo_wbs", nullable=false, length=60) private String codigoWbs;
    @Column(nullable=false, length=200) private String nome;
    @Column(nullable=false) private Integer ordem = 1;
    @Column(length=200) private String responsavel;
    @Column(name="data_inicio") private LocalDate dataInicio;
    @Column(name="data_fim") private LocalDate dataFim;
    @Column(name="pct_concluido", nullable=false) private Integer pctConcluido = 0;
    @Column(nullable=false, length=20) private String status = "NAO_INICIADA";
}
