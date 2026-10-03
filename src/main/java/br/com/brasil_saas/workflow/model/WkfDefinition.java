package br.com.brasil_saas.workflow.model;
import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="bc_wkf_definition", schema="brasil_saas") @Getter @Setter
public class WkfDefinition extends TenantEntity {
    @Column(nullable=false, length=60) private String codigo;
    @Column(nullable=false, length=200) private String nome;
    @Column(columnDefinition="text") private String descricao;
    @Column(name="entidade_alvo", nullable=false, length=60) private String entidadeAlvo;
    @Column(nullable=false) private Boolean ativo = true;
}
