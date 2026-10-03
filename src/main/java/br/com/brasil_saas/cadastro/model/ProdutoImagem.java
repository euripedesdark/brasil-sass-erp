package br.com.brasil_saas.cadastro.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/** Tabela sem uuid/updated_at -> NÃO herda TenantEntity. */
@Entity
@Table(name = "bc_cad_produto_imagem", schema = "brasil_saas")
@Getter @Setter
public class ProdutoImagem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "empresa_id", nullable = false, updatable = false)
    private Long empresaId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;
    @Column(length = 500, nullable = false)
    private String url;
    @Column(nullable = false)
    private Integer ordem = 0;
    @Column(nullable = false)
    private Boolean principal = Boolean.FALSE;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
