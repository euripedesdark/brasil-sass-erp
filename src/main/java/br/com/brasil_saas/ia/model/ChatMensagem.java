package br.com.brasil_saas.ia.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bc_ia_chat_mensagem", schema = "brasil_saas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ChatMensagem extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sessao_id", nullable = false)
    private ChatSessao sessao;

    @Column(name = "conteudo", columnDefinition = "TEXT", nullable = false)
    private String conteudo;

    @Column(name = "tipo", length = 20, nullable = false)
    private String tipo; // USER, ASSISTANT, SYSTEM

    @Column(name = "tokens")
    private Integer tokens;

    @Column(name = "tempo_resposta_ms")
    private Long tempoRespostaMs;

    @Column(name = "modelo_ia", length = 50)
    private String modeloIa;

    @Column(name = "data_envio")
    private LocalDateTime dataEnvio = LocalDateTime.now();

    @Column(name = "classificacao")
    private Integer classificacao; // 1-5 estrelas

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;
}
