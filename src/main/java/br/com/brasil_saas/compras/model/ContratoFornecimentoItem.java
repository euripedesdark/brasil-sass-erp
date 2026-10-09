package br.com.brasil_saas.compras.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "bc_com_contrato_item", schema = "brasil_saas")
@Getter @Setter @NoArgsConstructor
public class ContratoFornecimentoItem extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrato_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private ContratoFornecimento contrato;

    @Column(name = "numero_item", nullable = false)
    private Integer numeroItem;
    @Column(name = "produto_id")
    private Long produtoId;
    @Column(length = 300)
    private String descricao;
    @Column(length = 10)
    private String unidade;
    @Column(name = "quantidade_contratada", precision = 15, scale = 3, nullable = false)
    private BigDecimal quantidadeContratada;
    @Column(name = "quantidade_liberada", precision = 15, scale = 3, nullable = false)
    private BigDecimal quantidadeLiberada = BigDecimal.ZERO;
    @Column(name = "valor_unitario", precision = 15, scale = 4, nullable = false)
    private BigDecimal valorUnitario;

    public BigDecimal saldo() {
        return quantidadeContratada.subtract(quantidadeLiberada);
    }
}
