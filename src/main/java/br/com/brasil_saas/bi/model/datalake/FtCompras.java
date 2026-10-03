package br.com.brasil_saas.bi.model.datalake;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "ft_compras", schema = "brasil_saas_dl")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class FtCompras {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "data_id", nullable = false)
    private Long dataId; // FK to dim_tempo

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId; // FK to dim_empresa

    @Column(name = "fornecedor_id", nullable = false)
    private Long fornecedorId; // FK to dim_fornecedor

    @Column(name = "produto_id", nullable = false)
    private Long produtoId; // FK to dim_produto

    @Column(name = "categoria_id")
    private Long categoriaId; // FK to dim_categoria

    @Column(name = "pedido_compra_id", nullable = false)
    private Long pedidoCompraId;

    @Column(name = "item_pedido_id")
    private Long itemPedidoId;

    @Column(name = "quantidade", nullable = false, precision = 15, scale = 4)
    private BigDecimal quantidade;

    @Column(name = "valor_unitario", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorUnitario;

    @Column(name = "valor_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "tipo_compra", nullable = false, length = 50)
    private String tipoCompra; // COMPRA_DIRETA, COTACAO, PEDIDO

    @Column(name = "status_pedido", nullable = false, length = 50)
    private String statusPedido; // ABERTO, CONFIRMADO, RECEBIDO, CANCELADO

    @Column(name = "data_pedido", nullable = false)
    private LocalDate dataPedido;

    @Column(name = "data_entrega")
    private LocalDate dataEntrega;

    @Column(name = "data_recebimento")
    private LocalDate dataRecebimento;

    @Column(name = "condicao_pagamento", length = 50)
    private String condicaoPagamento;

    @Column(name = "prazo_entrega")
    private Integer prazoEntrega;

    @Column(name = "frete", precision = 15, scale = 2)
    private BigDecimal frete;

    @Column(name = "impostos", precision = 15, scale = 2)
    private BigDecimal impostos;

    @Column(name = "data_criacao")
    private LocalDate dataCriacao = LocalDate.now();
}
