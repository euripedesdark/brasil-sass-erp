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
@Table(name = "bc_fis_apuracao", schema = "brasil_saas")
@Getter @Setter
public class Apuracao extends TenantEntity {
    @Column(name = "imposto_id", nullable = false)
    private Long impostoId;

    @Column(name = "competencia", length = 7, nullable = false)
    private String competencia;

    @Column(name = "base_calculo", precision = 15, scale = 2, nullable = false)
    private BigDecimal baseCalculo;

    @Column(name = "valor_devido", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorDevido;

    @Column(name = "valor_credito", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorCredito;

    @Column(name = "valor_pagar", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorPagar;

    @Column(length = 20, nullable = false)
    private String status; // ABERTA | ENCERRADA | TRANSMITIDA

    @Column(name = "apurada_at")
    private LocalDateTime apuradaAt;
}
