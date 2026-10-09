package br.com.brasil_saas.metas.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "bc_ven_meta", schema = "brasil_saas")
@Getter @Setter
public class MetaComercial extends TenantEntity {
    @Column(nullable = false)
    private Integer ano;
    @Column(nullable = false)
    private Integer mes;
    @Column(name = "vendedor_id")
    private Long vendedorId;
    @Column(length = 40)
    private String canal;
    @Column(name = "valor_meta", precision = 18, scale = 2, nullable = false)
    private BigDecimal valorMeta = BigDecimal.ZERO;
    @Column(name = "valor_realizado", precision = 18, scale = 2, nullable = false)
    private BigDecimal valorRealizado = BigDecimal.ZERO;
    @Column(columnDefinition = "text")
    private String observacao;
}
