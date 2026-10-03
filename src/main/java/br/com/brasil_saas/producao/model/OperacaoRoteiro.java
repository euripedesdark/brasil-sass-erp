package br.com.brasil_saas.producao.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name="bc_prod_roteiro_operacao",schema="brasil_saas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class OperacaoRoteiro extends TenantEntity {
    @Column(name="roteiro_id",nullable=false) private Long roteiroId;
    @Column(nullable=false) private Integer sequencia;
    @Column(nullable=false,length=60) private String codigo;
    @Column(nullable=false,length=160) private String nome;
    @Column(name="centro_trabalho_id") private Long centroTrabalhoId;
    @Column(name="setup_minutos",nullable=false,precision=12,scale=3) private BigDecimal setupMinutos=BigDecimal.ZERO;
    @Column(name="maquina_minutos",nullable=false,precision=12,scale=3) private BigDecimal maquinaMinutos=BigDecimal.ZERO;
    @Column(name="homem_minutos",nullable=false,precision=12,scale=3) private BigDecimal homemMinutos=BigDecimal.ZERO;
    @Column(columnDefinition="TEXT") private String instrucoes;
    @Column(nullable=false) private Boolean ativo=true;
}
