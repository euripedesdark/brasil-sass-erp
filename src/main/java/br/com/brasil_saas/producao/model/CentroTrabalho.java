package br.com.brasil_saas.producao.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name="bc_prod_centro_trabalho", schema="brasil_saas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CentroTrabalho extends TenantEntity {
    @Column(nullable=false,length=60) private String codigo;
    @Column(nullable=false,length=160) private String nome;
    @Column(name="capacidade_horas_dia",nullable=false,precision=10,scale=2) private BigDecimal capacidadeHorasDia=BigDecimal.valueOf(8);
    @Column(nullable=false) private Boolean ativo=true;
}
