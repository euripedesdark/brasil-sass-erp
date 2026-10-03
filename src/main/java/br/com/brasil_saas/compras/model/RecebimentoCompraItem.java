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

@Entity
@Table(name = "bc_com_recebimento_item", schema = "brasil_saas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecebimentoCompraItem extends TenantEntity {

    @Column(name = "recebimento_id", nullable = false)
    private Long recebimentoId;

    @Column(name = "produto_id", nullable = false)
    private Long produtoId;

    @Column(name = "quantidade_pedida", precision = 15, scale = 4, nullable = false)
    private BigDecimal quantidadePedida = BigDecimal.ZERO;

    @Column(name = "quantidade_recebida", precision = 15, scale = 4, nullable = false)
    private BigDecimal quantidadeRecebida = BigDecimal.ZERO;

    @Column(name = "valor_unitario", precision = 15, scale = 4, nullable = false)
    private BigDecimal valorUnitario = BigDecimal.ZERO;

    @Column(name = "lote_id")
    private Long loteId;
}
