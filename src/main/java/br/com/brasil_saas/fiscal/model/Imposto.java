package br.com.brasil_saas.fiscal.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "bc_fis_imposto", schema = "brasil_saas")
@Getter @Setter
public class Imposto extends TenantEntity {
    @Column(length = 10, nullable = false)
    private String sigla; // ICMS, IPI, PIS, COFINS, ISS, CSLL, IRPJ

    @Column(length = 100, nullable = false)
    private String nome;

    @Column(length = 30, nullable = false)
    private String tipo; // FEDERAL, ESTADUAL, MUNICIPAL

    @Column(name = "aliquota_padrao", precision = 7, scale = 4)
    private BigDecimal aliquotaPadrao;

    @Column(nullable = false)
    private Boolean ativo = Boolean.TRUE;
}
