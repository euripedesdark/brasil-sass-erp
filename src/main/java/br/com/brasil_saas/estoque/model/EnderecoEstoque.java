package br.com.brasil_saas.estoque.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bc_est_endereco", schema = "brasil_saas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class EnderecoEstoque extends TenantEntity {
    @Column(name = "deposito_id", nullable = false) private Long depositoId;
    @Column(nullable = false, length = 50) private String codigo;
    @Column(length = 150) private String descricao;
    @Column(nullable = false, length = 30) private String tipo = "PULMAO";
    @Column(precision = 15, scale = 4) private java.math.BigDecimal capacidade;
    @Column(nullable = false) private Boolean ativo = true;
}