package br.com.brasil_saas.workflow.model;
import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*; import lombok.*;
import java.time.LocalDateTime;
@Entity @Table(name="bc_wkf_instance", schema="brasil_saas") @Getter @Setter
public class WkfInstance extends TenantEntity {
    @Column(name="definition_id", nullable=false) private Long definitionId;
    @Column(name="entidade_tipo", nullable=false, length=60) private String entidadeTipo;
    @Column(name="entidade_id", nullable=false) private Long entidadeId;
    @Column(nullable=false, length=20) private String status = "EM_ANDAMENTO";
    @Column(name="etapa_atual", nullable=false) private Integer etapaAtual = 1;
    @Column(name="solicitado_por") private Long solicitadoPor;
    @Column(columnDefinition="text") private String observacao;
    @Column(name="concluded_at") private LocalDateTime concludedAt;
}
