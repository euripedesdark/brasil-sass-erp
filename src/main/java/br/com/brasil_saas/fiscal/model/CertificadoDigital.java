package br.com.brasil_saas.fiscal.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "bc_fis_certificado_digital", schema = "brasil_saas")
@Getter @Setter
public class CertificadoDigital extends TenantEntity {
    @Column(length = 10, nullable = false)
    private String tipo; // A1 | A3

    @Column(name = "arquivo_url", length = 500)
    private String arquivoUrl;

    @Column(name = "cnpj_titular", length = 14)
    private String cnpjTitular;

    @Column(name = "emissao_at")
    private LocalDateTime emissaoAt;

    @Column(name = "validade_at", nullable = false)
    private LocalDateTime validadeAt;

    @Column(nullable = false)
    private Boolean ativo = Boolean.TRUE;
}
