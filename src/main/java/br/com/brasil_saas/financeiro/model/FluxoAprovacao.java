package br.com.brasil_saas.financeiro.model;
import br.com.brasil_saas.shared.model.AuditableEntity;
import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="bc_fin_fluxo_aprovacao", schema="brasil_saas") @Getter @Setter @NoArgsConstructor
public class FluxoAprovacao extends AuditableEntity {
    @Column(length=100, nullable=false) private String descricao;
    @Column private Boolean ativo = true;
}
