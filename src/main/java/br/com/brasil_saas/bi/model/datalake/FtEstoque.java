package br.com.brasil_saas.bi.model.datalake;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "ft_estoque", schema = "brasil_saas_dl")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class FtEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "data_id", nullable = false)
    private Long dataId; // FK to dim_tempo

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId; // FK to dim_empresa

    @Column(name = "produto_id", nullable = false)
    private Long produtoId; // FK to dim_produto

    @Column(name = "categoria_id")
    private Long categoriaId; // FK to dim_categoria

    @Column(name = "centro_custo_id")
    private Long centroCustoId; // FK to dim_centro_custo

    @Column(name = "tipo_movimentacao", nullable = false, length = 50)
    private String tipoMovimentacao; // ENTRADA, SAIDA, TRANSFERENCIA, AJUSTE

    @Column(name = "quantidade", nullable = false, precision = 15, scale = 4)
    private BigDecimal quantidade;

    @Column(name = "valor_unitario", precision = 15, scale = 2)
    private BigDecimal valorUnitario;

    @Column(name = "valor_total", precision = 15, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "saldo_anterior", precision = 15, scale = 4)
    private BigDecimal saldoAnterior;

    @Column(name = "saldo_atual", precision = 15, scale = 4)
    private BigDecimal saldoAtual;

    @Column(name = "data_movimentacao", nullable = false)
    private LocalDate dataMovimentacao;

    @Column(name = "documento_origem", length = 100)
    private String documentoOrigem; // PEDIDO_COMPRA, PEDIDO_VENDA, ORDEM_PRODUCAO, etc.

    @Column(name = "documento_id")
    private Long documentoId;

    @Column(name = "data_criacao")
    private LocalDate dataCriacao = LocalDate.now();
}
