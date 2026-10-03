package br.com.brasil_saas.compras.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity @Table(name="bc_com_cotacao_fornecedor",schema="brasil_saas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CotacaoFornecedor extends TenantEntity {
    @Column(name="cotacao_id",nullable=false) private Long cotacaoId;
    @Column(name="fornecedor_id",nullable=false) private Long fornecedorId;
    @Column(nullable=false,length=20) private String status="PENDENTE";
    @Column(name="prazo_entrega") private Integer prazoEntrega;
    @Column(name="condicao_pagamento_id") private Long condicaoPagamentoId;
    @Column(precision=15,scale=2) private BigDecimal frete=BigDecimal.ZERO;
    @Column(precision=15,scale=2) private BigDecimal desconto=BigDecimal.ZERO;
    @Column(name="valor_total",precision=15,scale=2) private BigDecimal valorTotal=BigDecimal.ZERO;
    @Column(columnDefinition="TEXT") private String observacao;
}