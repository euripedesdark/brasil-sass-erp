package br.com.brasil_saas.compras.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Uma linha da conferencia 3-way: o mesmo item visto no pedido, no recebimento
 * e na NF-e de entrada, com o que nao casa.
 *
 * <p>Fica gravada mesmo quando o item esta conforme: e' o registro de que a
 * linha foi comparada e passou.
 */
@Entity
@Table(name = "bc_com_conferencia_fatura_item", schema = "brasil_saas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConferenciaFaturaCompraItem extends TenantEntity {

    @Column(name = "conferencia_id", nullable = false)
    private Long conferenciaId;

    @Column(name = "produto_id")
    private Long produtoId;

    @Column(name = "numero_item")
    private Integer numeroItem;

    @Column(name = "descricao", length = 300)
    private String descricao;

    @Column(name = "quantidade_pedida", precision = 15, scale = 4, nullable = false)
    private BigDecimal quantidadePedida = BigDecimal.ZERO;

    @Column(name = "quantidade_recebida", precision = 15, scale = 4, nullable = false)
    private BigDecimal quantidadeRecebida = BigDecimal.ZERO;

    @Column(name = "quantidade_faturada", precision = 15, scale = 4, nullable = false)
    private BigDecimal quantidadeFaturada = BigDecimal.ZERO;

    @Column(name = "valor_unitario_pedido", precision = 15, scale = 4, nullable = false)
    private BigDecimal valorUnitarioPedido = BigDecimal.ZERO;

    @Column(name = "valor_unitario_recebido", precision = 15, scale = 4, nullable = false)
    private BigDecimal valorUnitarioRecebido = BigDecimal.ZERO;

    @Column(name = "valor_unitario_faturado", precision = 15, scale = 4, nullable = false)
    private BigDecimal valorUnitarioFaturado = BigDecimal.ZERO;

    @Column(name = "valor_total_pedido", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorTotalPedido = BigDecimal.ZERO;

    @Column(name = "valor_total_recebido", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorTotalRecebido = BigDecimal.ZERO;

    @Column(name = "valor_total_faturado", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorTotalFaturado = BigDecimal.ZERO;

    @Column(name = "conforme", nullable = false)
    private Boolean conforme = true;

    @Column(name = "tipo_divergencia", length = 40)
    private String tipoDivergencia;

    @Column(name = "divergencia", length = 500)
    private String divergencia;
}
