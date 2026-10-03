package br.com.brasil_saas.financeiro.model;
import br.com.brasil_saas.shared.model.AuditableEntity;
import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="bc_fin_integracao_bancaria", schema="brasil_saas") @Getter @Setter @NoArgsConstructor
public class IntegracaoBancaria extends AuditableEntity {
    @Column(name="conta_bancaria_id", nullable=false) private Long contaBancariaId;
    @Column(length=50, nullable=false) private String provedor;
    @Column(name="configuracao_json", columnDefinition="text") private String configuracaoJson;
    @Column private Boolean ativo = true;
}
