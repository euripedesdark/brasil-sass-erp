package br.com.brasil_saas.bi.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bc_bi_indicador", schema = "brasil_saas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Indicador extends TenantEntity {

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "categoria", length = 50)
    private String categoria; // VENDAS, FINANCEIRO, PRODUCAO, ESTOQUE, RH

    @Column(name = "formula", columnDefinition = "TEXT")
    private String formula; // Formula SQL ou expressao matematica

    @Column(name = "valor_atual", precision = 15, scale = 2)
    private BigDecimal valorAtual;

    @Column(name = "valor_anterior", precision = 15, scale = 2)
    private BigDecimal valorAnterior;

    @Column(name = "valor_meta", precision = 15, scale = 2)
    private BigDecimal valorMeta;

    @Column(name = "unidade_medida", length = 20)
    private String unidadeMedida = "R$"; // R$, UN, %, KG, etc.

    @Column(name = "cor_valor_baixo", length = 20)
    private String corValorBaixo = "#dc3545"; // Vermelho

    @Column(name = "cor_valor_medio", length = 20)
    private String corValorMedio = "#ffc107"; // Amarelo

    @Column(name = "cor_valor_alto", length = 20)
    private String corValorAlto = "#28a745"; // Verde

    @Column(name = "ativo")
    private Boolean ativo = true;

    @Column(name = "data_calculo")
    private LocalDate dataCalculo;

    @Column(name = "frequencia_atualizacao", length = 20)
    private String frequenciaAtualizacao; // DIARIO, SEMANAL, MENSAL

    @Column(name = "visivel_dashboard")
    private Boolean visivelDashboard = true;

    @Column(name = "ordem_exibicao")
    private Integer ordemExibicao = 0;
}
