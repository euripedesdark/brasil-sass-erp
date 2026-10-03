package br.com.brasil_saas.fiscal.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bc_fis_esocial", schema = "brasil_saas")
@Getter @Setter
public class Esocial extends TenantEntity {
    @Column(name = "competencia", length = 7, nullable = false)
    private String competencia;

    @Column(name = "evento", length = 20)
    private String evento; // S-1000 | S-1200 | S-1299 ...

    @Column(name = "arquivo_url", length = 500)
    private String arquivoUrl;

    @Column(length = 20, nullable = false)
    private String status;
}
