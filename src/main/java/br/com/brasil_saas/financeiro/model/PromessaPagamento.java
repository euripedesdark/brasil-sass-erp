package br.com.brasil_saas.financeiro.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bc_fin_promessa_pagamento", schema = "brasil_saas")
@Getter @Setter
public class PromessaPagamento extends TenantEntity {
    @Column(name = "titulo_id", nullable = false)
    private Long tituloId;
    @Column(name = "pessoa_id")
    private Long pessoaId;
    @Column(name = "valor_prometido", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorPrometido;
    @Column(name = "data_prometida", nullable = false)
    private LocalDate dataPrometida;
    @Column(nullable = false, length = 20)
    private String status = "ABERTA";
    @Column(length = 500)
    private String observacao;
}
