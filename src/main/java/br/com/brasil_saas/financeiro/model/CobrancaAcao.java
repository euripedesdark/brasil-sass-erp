package br.com.brasil_saas.financeiro.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bc_fin_cobranca_acao", schema = "brasil_saas")
@Getter @Setter
public class CobrancaAcao extends TenantEntity {
    @Column(name = "titulo_id", nullable = false)
    private Long tituloId;
    @Column(name = "pessoa_id")
    private Long pessoaId;
    @Column(nullable = false)
    private Integer nivel = 1;
    @Column(nullable = false, length = 30)
    private String tipo;
    @Column(length = 500)
    private String observacao;
}
