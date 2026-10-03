package br.com.brasil_saas.cadastro.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bc_cad_unidade_medida", schema = "brasil_saas")
@Getter @Setter
public class UnidadeMedida extends TenantEntity {
    @Column(length = 10, nullable = false)
    private String sigla;
    @Column(length = 50, nullable = false)
    private String nome;
    @Column(length = 20, nullable = false)
    private String tipo = "UNIDADE";
}
