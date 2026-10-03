package br.com.brasil_saas.ativos.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name="bc_ativo_imobilizado", schema="brasil_saas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AtivoImobilizado extends TenantEntity {
    @Column(nullable=false,length=50) private String codigo;
    @Column(nullable=false,length=255) private String descricao;
    @Column(length=100) private String classe;
    @Column(name="numero_serie",length=100) private String numeroSerie;
    @Column(length=255) private String localizacao;
    @Column(name="responsavel_id") private Long responsavelId;
    @Column(name="data_aquisicao") private LocalDate dataAquisicao;
    @Column(name="valor_aquisicao",precision=15,scale=2,nullable=false) private BigDecimal valorAquisicao=BigDecimal.ZERO;
    @Column(name="valor_residual",precision=15,scale=2,nullable=false) private BigDecimal valorResidual=BigDecimal.ZERO;
    @Column(name="valor_depreciado",precision=15,scale=2,nullable=false) private BigDecimal valorDepreciado=BigDecimal.ZERO;
    @Column(nullable=false,length=30) private String status="ATIVO";
    @Column(name="vida_util_meses") private Integer vidaUtilMeses;
}