package br.com.brasil_saas.crm.model;
import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*; import lombok.*;
import java.time.LocalDateTime;
@Entity @Table(name="bc_crm_tarefa", schema="brasil_saas") @Getter @Setter
public class CrmAtividade extends TenantEntity {
    @Column(name="lead_id") private Long leadId;
    @Column(nullable=false, length=20) private String tipo = "TAREFA";
    @Column(nullable=false, length=200) private String assunto;
    @Column(columnDefinition="text") private String descricao;
    @Column(name="data_agendada") private LocalDateTime dataAgendada;
    @Column(nullable=false) private Boolean concluida = false;
    @Column(name="concluida_em") private LocalDateTime concluidaEm;
    @Column(length=200) private String responsavel;
}
