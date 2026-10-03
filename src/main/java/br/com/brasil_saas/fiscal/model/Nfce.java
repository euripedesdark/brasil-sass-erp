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
@Table(name = "bc_fis_nfce", schema = "brasil_saas")
@Getter @Setter
public class Nfce extends TenantEntity {
    @Column(name = "pessoa_id")
    private Long pessoaId;

    private Long numero;

    @Column(length = 10)
    private String serie;

    @Column(name = "chave_acesso", length = 50)
    private String chaveAcesso;

    @Column(name = "data_emissao")
    private LocalDateTime dataEmissao;

    @Column(length = 20, nullable = false)
    private String status;

    @Column(length = 50)
    private String protocolo;

    @Column(name = "valor_total", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorTotal;

    @Column(columnDefinition = "TEXT")
    private String xml;
}
