package br.com.brasil_saas.projetos.model;
import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="bc_prj_risco", schema="brasil_saas") @Getter @Setter
public class PrjRisco extends TenantEntity {
    @Column(name="projeto_id", nullable=false) private Long projetoId;
    @Column(nullable=false, length=500) private String descricao;
    @Column(nullable=false) private Integer probabilidade = 50;
    @Column(nullable=false, length=20) private String impacto = "MEDIO";
    @Column(nullable=false, length=20) private String status = "ABERTO";
    @Column(columnDefinition="text") private String mitigacao;
}
