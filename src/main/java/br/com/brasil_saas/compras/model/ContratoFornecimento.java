package br.com.brasil_saas.compras.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Contrato/acordo de fornecimento (SAP Outline Agreement). Tipo QUANTIDADE ou VALOR. */
@Entity
@Table(name = "bc_com_contrato", schema = "brasil_saas")
@Getter @Setter @NoArgsConstructor
public class ContratoFornecimento extends TenantEntity {

    public static final String RASCUNHO = "RASCUNHO", ATIVO = "ATIVO", ENCERRADO = "ENCERRADO", CANCELADO = "CANCELADO";

    @Column(name = "fornecedor_id", nullable = false)
    private Long fornecedorId;
    @Column(nullable = false, length = 30)
    private String numero;
    @Column(nullable = false, length = 20)
    private String tipo = "QUANTIDADE";
    @Column(nullable = false, length = 20)
    private String status = RASCUNHO;
    @Column(name = "vigencia_inicio", nullable = false)
    private LocalDate vigenciaInicio;
    @Column(name = "vigencia_fim", nullable = false)
    private LocalDate vigenciaFim;
    @Column(name = "condicao_pagamento_id")
    private Long condicaoPagamentoId;
    @Column(name = "valor_limite", precision = 15, scale = 2)
    private BigDecimal valorLimite;
    @Column(name = "valor_liberado", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorLiberado = BigDecimal.ZERO;
    @Column(columnDefinition = "TEXT")
    private String observacao;

    @OneToMany(mappedBy = "contrato", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("numeroItem ASC")
    private List<ContratoFornecimentoItem> itens = new ArrayList<>();
}
