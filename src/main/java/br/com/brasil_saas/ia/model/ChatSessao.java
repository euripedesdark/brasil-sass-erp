package br.com.brasil_saas.ia.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bc_ia_chat_sessao", schema = "brasil_saas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ChatSessao extends TenantEntity {

    @Column(name = "titulo", length = 200)
    private String titulo;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "modelo_ia", length = 50)
    private String modeloIa = "gpt-4"; // gpt-4, gpt-3.5-turbo, etc.

    @Column(name = "temperatura")
    private Double temperatura = 0.7;

    @Column(name = "max_tokens")
    private Integer maxTokens = 4096;

    @Column(name = "ativo")
    private Boolean ativo = true;

    @Column(name = "favorito")
    private Boolean favorito = false;

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao = LocalDateTime.now();

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @OneToMany(mappedBy = "sessao", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ChatMensagem> mensagens = new ArrayList<>();
}
