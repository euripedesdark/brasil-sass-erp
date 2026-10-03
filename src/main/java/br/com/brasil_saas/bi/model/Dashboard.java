package br.com.brasil_saas.bi.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "bc_bi_dashboard", schema = "brasil_saas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Dashboard extends TenantEntity {

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "tipo", length = 50)
    private String tipo; // VENDAS, FINANCEIRO, PRODUCAO, ESTOQUE, RH

    @Column(name = "layout", columnDefinition = "TEXT")
    private String layout; // JSON com layout do dashboard

    @Column(name = "filtros", columnDefinition = "TEXT")
    private String filtros; // JSON com filtros padrao

    @Column(name = "ativo")
    private Boolean ativo = true;

    @Column(name = "publico")
    private Boolean publico = false; // Visivel para todos os usuarios

    @Column(name = "data_criacao")
    private LocalDate dataCriacao = LocalDate.now();

    @Column(name = "data_atualizacao")
    private LocalDate dataAtualizacao;

    @Column(name = "criado_por")
    private Long criadoPor;
}
