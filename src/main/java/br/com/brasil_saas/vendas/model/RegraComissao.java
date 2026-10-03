package br.com.brasil_saas.vendas.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bc_ven_regra_comissao", schema = "brasil_saas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RegraComissao extends TenantEntity {
    @Column(nullable=false, length=100) private String nome;
    @Column(name="vendedor_id") private Long vendedorId;
    @Column(name="vigencia_inicio") private LocalDate vigenciaInicio;
    @Column(name="vigencia_fim") private LocalDate vigenciaFim;
    @Column(name="meta_valor", precision=15, scale=2) private BigDecimal metaValor;
    @Column(name="faixa_valor_min", precision=15, scale=2, nullable=false) private BigDecimal faixaValorMin = BigDecimal.ZERO;
    @Column(name="faixa_valor_max", precision=15, scale=2) private BigDecimal faixaValorMax;
    @Column(precision=7, scale=4, nullable=false) private BigDecimal percentual;
    @Column(name="base_calculo", length=30, nullable=false) private String baseCalculo = "VALOR_LIQUIDO";
    @Column(nullable=false) private Boolean ativo = true;
}