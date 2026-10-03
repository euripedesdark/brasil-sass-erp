package br.com.brasil_saas.financeiro.model;
import br.com.brasil_saas.shared.model.AuditableEntity;
import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="bc_fin_condicao_pagamento", schema="brasil_saas") @Getter @Setter @NoArgsConstructor
public class CondicaoPagamento extends AuditableEntity {
    @Column(length=100, nullable=false) private String descricao;
    @Column(columnDefinition="text") private String dias; // "0,30,60"
    @Column private Boolean ativo = true;
}
