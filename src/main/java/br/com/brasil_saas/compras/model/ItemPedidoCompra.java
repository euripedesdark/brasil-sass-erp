package br.com.brasil_saas.compras.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "bc_com_pedido_item", schema = "brasil_saas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ItemPedidoCompra extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private PedidoCompra pedido;

    // Id lido da propria coluna, para nao passar pelo proxy lazy.
    @Column(name = "pedido_id", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonProperty("pedidoId")
    private Long pedidoRef;

    @Column(name = "numero_item", nullable = false)
    private Integer numeroItem;

    @Column(name = "produto_id")
    private Long produtoId;

    @Column(name = "descricao", length = 300)
    private String descricao;

    @Column(name = "quantidade", precision = 15, scale = 3, nullable = false)
    private BigDecimal quantidade;

    @Column(name = "unidade", length = 10)
    private String unidade;

    @Column(name = "valor_unitario", precision = 15, scale = 4, nullable = false)
    private BigDecimal valorUnitario;

    @Column(name = "valor_desconto", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorDesconto = BigDecimal.ZERO;

    @Column(name = "valor_total", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorTotal;

    @Column(name = "criado_estoque", nullable = false)
    private Boolean criadoEstoque = false;

    @Column(name = "quantidade_recebida", precision = 15, scale = 3, nullable = false)
    private BigDecimal quantidadeRecebida = BigDecimal.ZERO;
}
