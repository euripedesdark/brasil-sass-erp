package br.com.brasil_saas.crm.model;
import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*; import lombok.*; import java.time.LocalDateTime;
@Entity @Table(name="bc_crm_campanha_contato",schema="brasil_saas") @Getter @Setter
public class CrmCampanhaContato extends TenantEntity {
 @Column(name="campanha_id",nullable=false) private Long campanhaId;
 @Column(name="lead_id",nullable=false) private Long leadId;
 @Column(nullable=false,length=20) private String status="PENDENTE";
 @Column(name="data_ultimo_contato") private LocalDateTime dataUltimoContato;
 @Column(columnDefinition="text") private String observacao;
}
