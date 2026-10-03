package br.com.brasil_saas.cadastro.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "bc_cad_produto_variacao", schema = "brasil_saas")
@Getter @Setter
public class ProdutoVariacao extends TenantEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;
    @Column(length = 100, nullable = false)
    private String nome;
    @Column(length = 50)
    private String sku;
    @Column(name = "codigo_barras", length = 30)
    private String codigoBarras;
    @Column(precision = 15, scale = 2)
    private BigDecimal preco;
    @Column(nullable = false)
    private Boolean ativo = Boolean.TRUE;
}
