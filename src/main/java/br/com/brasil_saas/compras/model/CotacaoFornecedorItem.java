package br.com.brasil_saas.compras.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity @Table(name="bc_com_cotacao_item",schema="brasil_saas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CotacaoFornecedorItem extends TenantEntity {
    @Column(name="cotacao_fornecedor_id",nullable=false) private Long cotacaoFornecedorId;
    @Column(name="produto_id",nullable=false) private Long produtoId;
    @Column(nullable=false,precision=15,scale=4) private BigDecimal quantidade;
    @Column(name="valor_unitario",nullable=false,precision=15,scale=4) private BigDecimal valorUnitario;
    @Column(name="valor_total",nullable=false,precision=15,scale=2) private BigDecimal valorTotal;
}