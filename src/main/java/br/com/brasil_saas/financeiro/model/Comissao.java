package br.com.brasil_saas.financeiro.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bc_fin_comissao", schema = "brasil_saas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Comissao extends TenantEntity {

    @Column(name = "funcionario_id", nullable = false)
    private Long funcionarioId;

    @Column(name = "pedido_id", nullable = false)
    private Long pedidoId;

    @Column(name = "valor_venda", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorVenda;

    @Column(name = "percentual_comissao", precision = 5, scale = 2, nullable = false)
    private BigDecimal percentual;

    @Column(name = "valor_comissao", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorComissao;

    @Column(name = "status", length = 20, nullable = false)
    private String status; // PENDENTE, PAGO

    @Column(name = "data_pagamento")
    private LocalDateTime dataPagamento;
}
