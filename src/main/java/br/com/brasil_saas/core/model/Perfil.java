package br.com.brasil_saas.core.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "bc_core_perfil", schema = "brasil_saas")
@Getter
@Setter
public class Perfil extends TenantEntity {

    @Column(length = 50)
    private String nome;

    @Column
    private String descricao;

    @Column(name = "hierarquia_nivel")
    private Integer hierarquiaNivel;

    /**
     * Perfil pai usado para herança de permissões.
     *
     * Exemplo:
     * ADMIN -> DIRETORIA -> GERENTE
     *
     * As permissões próprias permanecem no perfil e as permissões herdadas
     * são resolvidas pelo serviço de autorização.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_pai_id")
    private Perfil perfilPai;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "bc_core_perfil_permissao",
        schema = "brasil_saas",
        joinColumns = @JoinColumn(name = "perfil_id"),
        inverseJoinColumns = @JoinColumn(name = "permissao_id")
    )
    private Set<Permissao> permissoes = new HashSet<>();
}
