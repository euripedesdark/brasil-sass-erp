package br.com.brasil_saas.ia.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bc_ia_classificacao", schema = "brasil_saas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Classificacao extends TenantEntity {

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "tipo", length = 50)
    private String tipo; // PRODUTO, CLIENTE, FORNECEDOR, SERVICO, DOCUMENTO

    @Column(name = "categoria", length = 100)
    private String categoria;

    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags; // Tags separadas por virgula

    @Column(name = "confianca")
    private Double confianca; // 0.0 - 1.0 (DOUBLE PRECISION: precision/scale nao se aplicam a tipos float)

    @Column(name = "classificacao_manual", length = 100)
    private String classificacaoManual;

    @Column(name = "classificacao_ia", length = 100)
    private String classificacaoIa;

    @Column(name = "status", length = 20)
    private String status; // PENDENTE, APROVADO, REJEITADO

    @Column(name = "entidade_id")
    private Long entidadeId;

    @Column(name = "aprovado_por")
    private Long aprovadoPor;

    @Column(name = "data_aprovacao")
    private LocalDateTime dataAprovacao;

    @Column(name = "criado_por")
    private Long criadoPor;

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao = LocalDateTime.now();
}
