package br.com.brasil_saas.fiscal.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "bc_fis_mdfe", schema = "brasil_saas")
@Getter @Setter
public class Mdfe extends TenantEntity {
    private Long numero;

    @Column(length = 10)
    private String serie;

    @Column(name = "chave_acesso", length = 50)
    private String chaveAcesso;

    @Column(name = "data_emissao")
    private LocalDateTime dataEmissao;

    @Column(length = 20, nullable = false)
    private String status;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "uf_inicio", length = 2)
    private String ufInicio;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "uf_fim", length = 2)
    private String ufFim;

    @Column(columnDefinition = "TEXT")
    private String xml;
}
