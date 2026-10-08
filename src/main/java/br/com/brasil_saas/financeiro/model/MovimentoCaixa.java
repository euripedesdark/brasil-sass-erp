package br.com.brasil_saas.financeiro.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "bc_fin_caixa_movimento", schema = "brasil_saas")
@Getter
@Setter
public class MovimentoCaixa extends TenantEntity {

    @Column(name = "caixa_id", nullable = false)
    private Long caixaId;

    /** SANGRIA | SUPRIMENTO */
    @Column(nullable = false, length = 20)
    private String tipo;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(name = "saldo_anterior", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoAnterior;

    @Column(name = "saldo_posterior", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoPosterior;

    @Column(length = 500)
    private String observacao;
}
