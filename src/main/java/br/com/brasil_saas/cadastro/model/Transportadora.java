package br.com.brasil_saas.cadastro.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bc_cad_transportadora", schema = "brasil_saas")
@Getter @Setter
public class Transportadora extends TenantEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoa pessoa;
    @Column(length = 30)
    private String codigo;
    @Column(name = "registro_antt", length = 30)
    private String registroAntt;
    @Column(nullable = false)
    private Boolean ativo = Boolean.TRUE;
}
