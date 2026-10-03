package br.com.brasil_saas.fiscal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bc_fis_cte_item", schema = "brasil_saas")
@Getter @Setter
public class CteItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cte_id", nullable = false)
    private Cte cte;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(precision = 15, scale = 4)
    private BigDecimal quantidade;

    @Column(name = "valor", precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
