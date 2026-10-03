package br.com.brasil_saas.cadastro.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "bc_cad_produto_ecommerce", schema = "brasil_saas")
@Getter @Setter
public class ProdutoEcommerce extends TenantEntity {
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false, unique = true)
    private Produto produto;
    @Column(nullable = false)
    private Boolean publicado = Boolean.FALSE;
    @Column(length = 50)
    private String marketplace;
    @Column(name = "url_externa", length = 500)
    private String urlExterna;
    @Column(name = "preco_marketplace", precision = 15, scale = 2)
    private BigDecimal precoMarketplace;
}
