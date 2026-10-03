package br.com.brasil_saas.fiscal.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bc_fis_nfse_item", schema = "brasil_saas")
@Getter @Setter
public class NfseItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nfse_id", nullable = false)
    private Nfse nfse;

    @Column(name = "servico_id")
    private Long servicoId;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(precision = 15, scale = 4, nullable = false)
    private BigDecimal quantidade = BigDecimal.ONE;

    @Column(name = "valor_unitario", precision = 15, scale = 4, nullable = false)
    private BigDecimal valorUnitario;

    @Column(name = "valor_total", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorTotal;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
