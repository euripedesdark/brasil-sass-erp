package br.com.brasil_saas.bi.model.datalake;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "ft_financeiro", schema = "brasil_saas_dl")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class FtFinanceiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "data_id", nullable = false)
    private Long dataId; // FK to dim_tempo

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId; // FK to dim_empresa

    @Column(name = "tipo_lancamento", nullable = false, length = 50)
    private String tipoLancamento; // RECEITA, DESPESA, TRANSFERENCIA

    @Column(name = "categoria_lancamento", nullable = false, length = 100)
    private String categoriaLancamento;

    @Column(name = "centro_custo_id")
    private Long centroCustoId; // FK to dim_centro_custo

    @Column(name = "cliente_fornecedor_id")
    private Long clienteFornecedorId; // Pode ser cliente ou fornecedor

    @Column(name = "cliente_fornecedor_tipo", length = 20)
    private String clienteFornecedorTipo; // CLIENTE, FORNECEDOR

    @Column(name = "titulo_id")
    private Long tituloId;

    @Column(name = "valor", nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(name = "valor_liquido", precision = 15, scale = 2)
    private BigDecimal valorLiquido;

    @Column(name = "data_vencimento")
    private LocalDate dataVencimento;

    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;

    @Column(name = "status", nullable = false, length = 50)
    private String status; // PENDENTE, PAGO, CANCELADO, ATRASADO

    @Column(name = "forma_pagamento", length = 50)
    private String formaPagamento;

    @Column(name = "data_criacao")
    private LocalDate dataCriacao = LocalDate.now();
}
