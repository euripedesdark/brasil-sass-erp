package br.com.brasil_saas.fiscal.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "bc_fis_sped_fiscal", schema = "brasil_saas")
@Getter @Setter
public class SpedFiscal extends TenantEntity {
    @Column(name = "competencia", length = 7, nullable = false)
    private String competencia;

    @Column(name = "arquivo_url", length = 500)
    private String arquivoUrl;

    @Column(length = 20, nullable = false)
    private String status;

    @Column(name = "gerado_at")
    private LocalDateTime geradoAt;
}
