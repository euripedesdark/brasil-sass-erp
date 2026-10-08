package br.com.brasil_saas.compras.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "bc_com_conferencia_fatura_item", schema = "brasil_saas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ConferenciaFaturaCompraItem extends TenantEntity {
    @Column(name = "conferencia_id", nullable = false)
    private Long conferenciaId;

    @Column(name = "pedido_item_id")
    private Long pedidoItemId;

    @Column(name = "recebimento_item_id")
    private Long recebimentoItemId;

    @Column(name = "nfe_item_id")
    private Long nfeItemId;

    @Column(name = "produto_id")
    private Long produtoId;

    @Column(name = "numero_item")
    private Integer numeroItem;

    @Column(name = "descricao", length = 300)
    private String descricao;

    @Column(name = "valor_total_pedido", precision = 15, scale = 2)
    private BigDecimal valorTotalPedido = BigDecimal.ZERO;

    @Column(name = "valor_total_recebido", precision = 15, scale = 2)
    private BigDecimal valorTotalRecebido = BigDecimal.ZERO;

    @Column(name = "valor_total_faturado", precision = 15, scale = 2)
    private BigDecimal valorTotalFaturado = BigDecimal.ZERO;

    @Column(name = "conforme", nullable = false)
    private Boolean conforme = true;

    @Column(name = "tipo_divergencia", length = 40)
    private String tipoDivergencia;

    @Column(name = "quantidade_pedida", precision = 18, scale = 4, nullable = false)
    private BigDecimal quantidadePedida = BigDecimal.ZERO;

    @Column(name = "quantidade_recebida", precision = 18, scale = 4, nullable = false)
    private BigDecimal quantidadeRecebida = BigDecimal.ZERO;

    @Column(name = "quantidade_faturada", precision = 18, scale = 4, nullable = false)
    private BigDecimal quantidadeFaturada = BigDecimal.ZERO;

    @Column(name = "valor_unitario_pedido", precision = 18, scale = 4, nullable = false)
    private BigDecimal valorUnitarioPedido = BigDecimal.ZERO;

    @Column(name = "valor_unitario_recebido", precision = 18, scale = 4, nullable = false)
    private BigDecimal valorUnitarioRecebido = BigDecimal.ZERO;

    @Column(name = "valor_unitario_faturado", precision = 18, scale = 4, nullable = false)
    private BigDecimal valorUnitarioFaturado = BigDecimal.ZERO;

    @Column(name = "tolerancia", precision = 18, scale = 4, nullable = false)
    private BigDecimal tolerancia = BigDecimal.ZERO;

    @Column(name = "status", length = 20, nullable = false)
    private String status = "DIVERGENTE";

    @Column(name = "divergencia", columnDefinition = "TEXT")
    private String divergencia;
}
