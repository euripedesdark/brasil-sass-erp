package br.com.brasil_saas.crm.model;
import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*; import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
@Entity @Table(name="bc_crm_lead", schema="brasil_saas") @Getter @Setter
public class CrmLead extends TenantEntity {
    @Column(nullable=false, length=200) private String nome;
    @Column(name="empresa_nome", length=200) private String empresaNome;
    @Column(length=150) private String email;
    @Column(length=20) private String telefone;
    @Column(length=60) private String origem;
    @Column(nullable=false, length=30) private String etapa = "PROSPECCAO";
    @Column(nullable=false, length=20) private String status = "ABERTO";
    @Column(name="valor_estimado", precision=15, scale=2, nullable=false) private BigDecimal valorEstimado = BigDecimal.ZERO;
    @Column(nullable=false) private Integer probabilidade = 10;
    @Column(length=200) private String responsavel;
    @Column(name="data_prev_fechamento") private LocalDate dataPrevFechamento;
    @Column(columnDefinition="text") private String observacao;
}
