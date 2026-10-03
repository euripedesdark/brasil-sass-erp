package br.com.brasil_saas.core.model;

import br.com.brasil_saas.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bc_core_permissao", schema = "brasil_saas")
@Getter
@Setter
public class Permissao extends BaseEntity {

    @Column(length = 80, nullable = false, unique = true)
    private String codigo;

    @Column(length = 50, nullable = false)
    private String recurso;

    @Column(length = 20, nullable = false)
    private String acao;

    @Column
    private String descricao;
}
