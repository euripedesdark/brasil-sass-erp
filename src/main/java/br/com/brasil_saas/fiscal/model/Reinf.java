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
@Table(name = "bc_fis_reinf", schema = "brasil_saas")
@Getter @Setter
public class Reinf extends TenantEntity {

    public static final String RASCUNHO = "RASCUNHO";
    public static final String GERADO = "GERADO";
    public static final String FECHADO = "FECHADO";
    public static final String TRANSMITIDO = "TRANSMITIDO";

    @Column(name = "competencia", length = 7, nullable = false)
    private String competencia;

    /** R-1000, R-2010, R-2020, R-2099, ... */
    @Column(name = "evento", length = 20)
    private String evento;

    @Column(name = "arquivo_url", length = 500)
    private String arquivoUrl;

    @Column(length = 20, nullable = false)
    private String status = RASCUNHO;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(length = 60)
    private String protocolo;

    @Column(name = "gerado_at")
    private LocalDateTime geradoAt;

    @Column(name = "total_docs")
    private Integer totalDocs = 0;

    @Column(name = "valor_total", precision = 15, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO;
}
