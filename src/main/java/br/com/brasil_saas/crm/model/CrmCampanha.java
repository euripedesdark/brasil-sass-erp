package br.com.brasil_saas.crm.model;
import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*; import lombok.*;
import java.math.BigDecimal; import java.time.LocalDate;
@Entity @Table(name="bc_crm_campanha",schema="brasil_saas") @Getter @Setter
public class CrmCampanha extends TenantEntity {
 @Column(nullable=false,length=200) private String nome;
 @Column(columnDefinition="text") private String descricao;
 @Column(nullable=false,length=30) private String canal="OUTRO";
 @Column(nullable=false,length=20) private String status="RASCUNHO";
 @Column(name="data_inicio") private LocalDate dataInicio;
 @Column(name="data_fim") private LocalDate dataFim;
 @Column(nullable=false,precision=15,scale=2) private BigDecimal orcamento=BigDecimal.ZERO;
 @Column(name="custo_realizado",nullable=false,precision=15,scale=2) private BigDecimal custoRealizado=BigDecimal.ZERO;
}
