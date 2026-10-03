package br.com.brasil_saas.financeiro.model;
import br.com.brasil_saas.shared.model.AuditableEntity;
import jakarta.persistence.*; import lombok.*;
import java.math.BigDecimal; import java.time.LocalDate;
@Entity @Table(name="bc_fin_baixa", schema="brasil_saas") @Getter @Setter @NoArgsConstructor
public class Baixa extends AuditableEntity {
    @Column(name="titulo_id", nullable=false) private Long tituloId;
    @Column(name="parcela_id") private Long parcelaId;
    @Column(name="conta_bancaria_id") private Long contaBancariaId;
    @Column(name="tipo_pagamento_id") private Long tipoPagamentoId;
    @Column(name="data_baixa", nullable=false) private LocalDate dataBaixa;
    @Column(name="valor_baixa", precision=15, scale=2, nullable=false) private BigDecimal valorBaixa;
    @Column(name="valor_desconto", precision=15, scale=2) private BigDecimal valorDesconto = BigDecimal.ZERO;
    @Column(name="valor_juro", precision=15, scale=2) private BigDecimal valorJuro = BigDecimal.ZERO;
    @Column(name="valor_multa", precision=15, scale=2) private BigDecimal valorMulta = BigDecimal.ZERO;
    @Column(columnDefinition="text") private String observacao;
}
