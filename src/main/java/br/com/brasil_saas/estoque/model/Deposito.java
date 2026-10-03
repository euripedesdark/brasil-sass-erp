package br.com.brasil_saas.estoque.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="bc_est_deposito", schema="brasil_saas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Deposito extends TenantEntity {
    @Column(nullable=false, length=30) private String codigo;
    @Column(nullable=false, length=100) private String nome;
    @Column(nullable=false, length=30) private String tipo = "PADRAO";
    @Column(nullable=false) private Boolean ativo = true;
}