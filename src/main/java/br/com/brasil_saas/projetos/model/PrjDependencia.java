package br.com.brasil_saas.projetos.model;
import br.com.brasil_saas.shared.model.TenantEntity; import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="bc_prj_dependencia",schema="brasil_saas") @Getter @Setter
public class PrjDependencia extends TenantEntity {
 @Column(name="projeto_id",nullable=false) private Long projetoId;
 @Column(name="antecessora_id",nullable=false) private Long antecessoraId;
 @Column(name="sucessora_id",nullable=false) private Long sucessoraId;
 @Column(name="defasagem_dias",nullable=false) private Integer defasagemDias=0;
}
