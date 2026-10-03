package br.com.brasil_saas.workflow.model;
import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="bc_wkf_stage", schema="brasil_saas") @Getter @Setter
public class WkfStage extends TenantEntity {
    @Column(name="definition_id", nullable=false) private Long definitionId;
    @Column(nullable=false) private Integer ordem;
    @Column(nullable=false, length=200) private String nome;
    @Column(nullable=false, length=20) private String tipo = "APROVACAO";
    @Column(name="sla_horas", nullable=false) private Integer slaHoras = 48;
    @Column(columnDefinition="text") private String aprovadores;
    @Column(name="exige_todos", nullable=false) private Boolean exigeTodos = false;
}
