package br.com.brasil_saas.vendas.model;

import br.com.brasil_saas.shared.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "bc_ven_pedido_item", schema = "brasil_saas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ItemPedidoVenda extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private PedidoVenda pedido;

    // PedidoVenda tem itens de volta: sem o @JsonIgnore o Jackson entra em laco
    // e o pedido volta como 200 com JSON truncado.
    @Column(name = "pedido_id", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonProperty("pedidoId")
    private Long pedidoIdRef;

    @Column(name = "numero_item", nullable = false)
    private Integer numeroItem;

    @Column(name = "produto_id")
    private Long produtoId;

    @Column(name = "servico_id")
    private Long servicoId;

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
}
