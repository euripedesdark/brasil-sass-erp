package br.com.brasil_saas.cadastro.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Tabela sem uuid/updated_at/deleted_at -> NÃO herda TenantEntity. */
@Entity
@Table(name = "bc_cad_produto_kit", schema = "brasil_saas")
@Getter @Setter
public class ProdutoKit {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "empresa_id", nullable = false, updatable = false)
    private Long empresaId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "kit_id", nullable = false)
    private Produto kit;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Produto item;
    @Column(nullable = false, precision = 15, scale = 3)
    private BigDecimal quantidade;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
