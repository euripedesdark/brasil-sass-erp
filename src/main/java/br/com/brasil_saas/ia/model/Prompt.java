package br.com.brasil_saas.ia.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bc_ia_prompt", schema = "brasil_saas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Prompt extends TenantEntity {

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "conteudo", columnDefinition = "TEXT", nullable = false)
    private String conteudo;

    @Column(name = "categoria", length = 50)
    private String categoria; // VENDAS, FINANCEIRO, PRODUCAO, ESTOQUE, RH, FISCAL, GERAL

    @Column(name = "tipo", length = 20)
    private String tipo = "TEXT"; // TEXT, SYSTEM, USER

    @Column(name = "temperatura")
    private Double temperatura = 0.7;

    @Column(name = "max_tokens")
    private Integer maxTokens = 4096;

    @Column(name = "ativo")
    private Boolean ativo = true;

    @Column(name = "publico")
    private Boolean publico = false; // Visivel para todos os usuarios

    @Column(name = "favorito")
    private Boolean favorito = false;

    @Column(name = "contador_uso")
    private Integer contadorUso = 0;

    @Column(name = "criado_por")
    private Long criadoPor;

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao = LocalDateTime.now();

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;
}
