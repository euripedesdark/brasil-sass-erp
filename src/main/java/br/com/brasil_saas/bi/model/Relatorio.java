package br.com.brasil_saas.bi.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bc_bi_relatorio", schema = "brasil_saas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Relatorio extends TenantEntity {

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "tipo", length = 50)
    private String tipo; // SQL, JASPER, CUSTOM

    @Column(name = "categoria", length = 50)
    private String categoria; // VENDAS, FINANCEIRO, PRODUCAO, ESTOQUE, RH, FISCAL

    @Column(name = "sql_query", columnDefinition = "TEXT")
    private String sqlQuery;

    @Column(name = "parametros", columnDefinition = "TEXT")
    private String parametros; // JSON com parametros do relatorio

    @Column(name = "ativo")
    private Boolean ativo = true;

    @Column(name = "agendado")
    private Boolean agendado = false;

    @Column(name = "frequencia", length = 20)
    private String frequencia; // DIARIO, SEMANAL, MENSAL, ANUAL

    @Column(name = "ultima_execucao")
    private LocalDateTime ultimaExecucao;

    @Column(name = "proxima_execucao")
    private LocalDateTime proximaExecucao;

    @Column(name = "email_destinatarios", columnDefinition = "TEXT")
    private String emailDestinatarios; // E-mails separados por virgula

    @Column(name = "formato_exportacao", length = 20)
    private String formatoExportacao = "PDF"; // PDF, EXCEL, CSV

    @Column(name = "criado_por")
    private Long criadoPor;

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao = LocalDateTime.now();
}
