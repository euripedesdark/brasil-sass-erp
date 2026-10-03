package br.com.brasil_saas.producao.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name="bc_prod_roteiro",schema="brasil_saas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RoteiroProducao extends TenantEntity {
    @Column(name="produto_id",nullable=false) private Long produtoId;
    @Column(nullable=false,length=60) private String codigo;
    @Column(nullable=false,length=160) private String nome;
    @Column(nullable=false) private Integer versao=1;
    @Column(name="vigencia_inicio") private LocalDate vigenciaInicio;
    @Column(name="vigencia_fim") private LocalDate vigenciaFim;
    @Column(nullable=false) private Boolean ativo=true;
    @Column(columnDefinition="TEXT") private String observacao;
}
