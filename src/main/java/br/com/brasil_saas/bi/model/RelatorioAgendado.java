package br.com.brasil_saas.bi.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bc_bi_relatorio_agendado", schema = "brasil_saas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RelatorioAgendado extends TenantEntity {

    @ManyToOne
    @JoinColumn(name = "relatorio_id")
    private Relatorio relatorio;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "frequencia", length = 20)
    private String frequencia; // DIARIO, SEMANAL, MENSAL, ANUAL, CUSTOM

    @Column(name = "intervalo_dias")
    private Integer intervaloDias; // Para frequencia CUSTOM

    @Column(name = "proxima_execucao")
    private LocalDateTime proximaExecucao;

    @Column(name = "ultima_execucao")
    private LocalDateTime ultimaExecucao;

    @Column(name = "ativo")
    private Boolean ativo = true;

    @Column(name = "email_destinatarios", columnDefinition = "TEXT")
    private String emailDestinatarios;

    @Column(name = "formato", length = 20)
    private String formato = "PDF"; // PDF, EXCEL, CSV

    @Column(name = "parametros", columnDefinition = "TEXT")
    private String parametros; // JSON com parametros

    @Column(name = "criado_por")
    private Long criadoPor;

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao = LocalDateTime.now();
}
