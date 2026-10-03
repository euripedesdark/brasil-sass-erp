package br.com.brasil_saas.ativos.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name="bc_ativo_manutencao", schema="brasil_saas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Manutencao extends TenantEntity {
    @Column(name="ativo_id",nullable=false) private Long ativoId;
    @Column(nullable=false,length=50) private String numero;
    @Column(nullable=false,length=30) private String tipo="CORRETIVA";
    @Column(nullable=false,length=30) private String status="ABERTA";
    @Column(nullable=false,length=20) private String prioridade="MEDIA";
    @Column(nullable=false,length=2000) private String descricao;
    @Column(name="data_programada") private LocalDate dataProgramada;
    @Column(name="data_conclusao") private LocalDate dataConclusao;
    @Column(precision=15,scale=2,nullable=false) private BigDecimal custo=BigDecimal.ZERO;
    @Column(name="responsavel_id") private Long responsavelId;
}