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
    @Column(name="plano_id") private Long planoId;
    @Column(name="nota_id") private Long notaId;
    @Column(name="data_inicio") private LocalDate dataInicio;
    @Column(name="horas_trabalhadas",precision=10,scale=2,nullable=false) private BigDecimal horasTrabalhadas=BigDecimal.ZERO;
    @Column(name="horas_parada",precision=10,scale=2,nullable=false) private BigDecimal horasParada=BigDecimal.ZERO;
    @Column(name="custo_material",precision=15,scale=2,nullable=false) private BigDecimal custoMaterial=BigDecimal.ZERO;
    @Column(name="custo_mao_obra",precision=15,scale=2,nullable=false) private BigDecimal custoMaoObra=BigDecimal.ZERO;
    @Column(name="custo_servico",precision=15,scale=2,nullable=false) private BigDecimal custoServico=BigDecimal.ZERO;
    @Column(length=1000) private String causa;
    @Column(length=2000) private String solucao;
    @Column(length=4000) private String checklist;
    @Column(name="centro_custo_id") private Long centroCustoId;
}