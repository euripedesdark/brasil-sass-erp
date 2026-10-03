package br.com.brasil_saas.workflow.model;
import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*; import lombok.*;
import java.time.LocalDateTime;
@Entity @Table(name="bc_wkf_task", schema="brasil_saas") @Getter @Setter
public class WkfTask extends TenantEntity {
    @Column(name="instance_id", nullable=false) private Long instanceId;
    @Column(name="stage_id", nullable=false) private Long stageId;
    @Column(length=200) private String responsavel;
    @Column(nullable=false, length=20) private String status = "PENDENTE";
    @Column(name="decided_at") private LocalDateTime decidedAt;
    @Column(name="decidido_por") private Long decididoPor;
    @Column(columnDefinition="text") private String comentario;
    @Column(name="sla_limite") private LocalDateTime slaLimite;
}
