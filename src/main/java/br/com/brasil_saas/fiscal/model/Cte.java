package br.com.brasil_saas.fiscal.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bc_fis_cte", schema = "brasil_saas")
@Getter @Setter
public class Cte extends TenantEntity {
    @Column(length = 10)
    private String serie;

    @Column(name = "pessoa_id")
    private Long pessoaId;

    private Long numero;

    @Column(name = "chave_acesso", length = 50)
    private String chaveAcesso;

    @Column(name = "data_emissao")
    private LocalDateTime dataEmissao;

    @Column(length = 20, nullable = false)
    private String status;

    @Column(name = "valor_carga", precision = 15, scale = 2)
    private BigDecimal valorCarga;

    @Column(name = "valor_frete", precision = 15, scale = 2)
    private BigDecimal valorFrete;

    @Column(name = "xml")
    private String xml;
    @Column(name = "tipo_operacao", columnDefinition = "bpchar(1)", nullable = false)
    private String tipoOperacao = "S";
}
