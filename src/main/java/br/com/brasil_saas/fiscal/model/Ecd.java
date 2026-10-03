package br.com.brasil_saas.fiscal.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bc_fis_ecd", schema = "brasil_saas")
@Getter @Setter
public class Ecd extends TenantEntity {
    @Column(name = "exercicio", length = 4, nullable = false)
    private String exercicio;

    @Column(name = "arquivo_url", length = 500)
    private String arquivoUrl;

    @Column(length = 20, nullable = false)
    private String status;
}
