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
import java.time.LocalDate;

@Entity
@Table(name = "bc_com_recebimento", schema = "brasil_saas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecebimentoCompra extends TenantEntity {

    @Column(name = "pedido_id")
    private Long pedidoId;

    @Column(name = "numero", nullable = false, length = 30)
    private String numero;

    @Column(name = "data_recebimento", nullable = false)
    private LocalDate dataRecebimento;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "CONFERENCIA";

    @Column(name = "documento_fornecedor", length = 50)
    private String documentoFornecedor;

    @Column(name = "valor_total", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Column(name = "observacao", columnDefinition = "TEXT")
    private String observacao;
}
