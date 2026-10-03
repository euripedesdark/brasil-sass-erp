package br.com.brasil_saas.ia.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bc_ia_embedding", schema = "brasil_saas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Embedding extends TenantEntity {

    @Column(name = "entidade_tipo", length = 50, nullable = false)
    private String entidadeTipo; // PRODUTO, CLIENTE, DOCUMENTO, EMAIL, etc.

    @Column(name = "entidade_id", nullable = false)
    private Long entidadeId;

    @Column(name = "texto", columnDefinition = "TEXT", nullable = false)
    private String texto;

    @Column(name = "embedding_vector", columnDefinition = "float8[]")
    private float[] embeddingVector; // Vetor de embeddings (ex: 1536 dimensoes para text-embedding-3-small)

    @Column(name = "dimensoes")
    private Integer dimensoes;

    @Column(name = "modelo", length = 50)
    private String modelo = "text-embedding-3-small";

    @Column(name = "tamanho_texto")
    private Integer tamanhoTexto;

    @Column(name = "tokens")
    private Integer tokens;

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao = LocalDateTime.now();

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;
}
