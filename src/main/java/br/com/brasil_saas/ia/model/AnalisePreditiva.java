package br.com.brasil_saas.ia.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bc_ia_analise_preditiva", schema = "brasil_saas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AnalisePreditiva extends TenantEntity {

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "tipo", length = 50)
    private String tipo; // VENDAS, ESTOQUE, FINANCEIRO, PRODUCAO

    @Column(name = "entidade", length = 50)
    private String entidade; // PRODUTO, CLIENTE, FORNECEDOR, etc.

    @Column(name = "entidade_id")
    private Long entidadeId;

    @Column(name = "periodo", length = 20)
    private String periodo; // DIARIO, SEMANAL, MENSAL, ANUAL

    @Column(name = "dias_previsao")
    private Integer diasPrevisao = 30;

    @Column(name = "valor_atual", precision = 15, scale = 2)
    private BigDecimal valorAtual;

    @Column(name = "valor_previsto", precision = 15, scale = 2)
    private BigDecimal valorPrevisto;

    @Column(name = "valor_minimo", precision = 15, scale = 2)
    private BigDecimal valorMinimo;

    @Column(name = "valor_maximo", precision = 15, scale = 2)
    private BigDecimal valorMaximo;

    @Column(name = "confianca", precision = 5, scale = 2)
    private BigDecimal confianca; // 0.0 - 1.0

    @Column(name = "status", length = 20)
    private String status; // PENDENTE, COMPLETO, ERRO

    @Column(name = "data_analise")
    private LocalDate dataAnalise;

    @Column(name = "data_proxima_analise")
    private LocalDate dataProximaAnalise;

    @Column(name = "criado_por")
    private Long criadoPor;

    @Column(name = "data_criacao")
    private LocalDate dataCriacao = LocalDate.now();
}
