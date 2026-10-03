package br.com.brasil_saas.financeiro.model;
import br.com.brasil_saas.shared.model.AuditableEntity;
import jakarta.persistence.*; import lombok.*;
@Entity @Table(name="bc_fin_tipo_pagamento", schema="brasil_saas") @Getter @Setter @NoArgsConstructor
public class TipoPagamento extends AuditableEntity {
    @Column(length=100, nullable=false) private String descricao;
    @Column(length=20) private String codigo;
    @Column private Boolean ativo = true;
}
