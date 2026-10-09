package br.com.brasil_saas.fiscal.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

@Entity
@Table(name = "bc_fis_regra_tributaria", schema = "brasil_saas")
@Getter @Setter
public class RegraTributaria extends TenantEntity {
    @Column(length = 100, nullable = false)
    private String nome;

    @Column(length = 8)
    private String ncm;

    @Column(length = 4)
    private String cfop;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "uf_origem", length = 2)
    private String ufOrigem;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "uf_destino", length = 2)
    private String ufDestino;

    @Column(name = "cst_icms", length = 5)
    private String cstIcms;

    @Column(name = "aliquota_icms", precision = 7, scale = 4)
    private BigDecimal aliquotaIcms;

    @Column(name = "cst_ipi", length = 5)
    private String cstIpi;

    @Column(name = "aliquota_ipi", precision = 7, scale = 4)
    private BigDecimal aliquotaIpi;

    @Column(name = "cst_pis", length = 5)
    private String cstPis;

    @Column(name = "aliquota_pis", precision = 7, scale = 4)
    private BigDecimal aliquotaPis;

    @Column(name = "cst_cofins", length = 5)
    private String cstCofins;

    @Column(name = "aliquota_cofins", precision = 7, scale = 4)
    private BigDecimal aliquotaCofins;

    @Column(name = "aliquota_st", precision = 7, scale = 4)
    private BigDecimal aliquotaSt;

    @Column(precision = 7, scale = 4)
    private BigDecimal mva;

    @Column(name = "aliquota_fcp", precision = 7, scale = 4)
    private BigDecimal aliquotaFcp;

    @Column(name = "aliquota_interna", precision = 7, scale = 4)
    private BigDecimal aliquotaInterna;

    @Column(name = "reducao_base_pct", precision = 7, scale = 4)
    private BigDecimal reducaoBasePct;

    @Column(nullable = false)
    private Boolean ativa = Boolean.TRUE;

    @Column(nullable = false)
    private Integer prioridade = 0;
}
