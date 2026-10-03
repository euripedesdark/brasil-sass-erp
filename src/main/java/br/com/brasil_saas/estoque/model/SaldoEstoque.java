package br.com.brasil_saas.estoque.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bc_est_saldo", schema = "brasil_saas")
@Getter @Setter @NoArgsConstructor
public class SaldoEstoque extends TenantEntity {
    @Column(name = "deposito_id", nullable = false) private Long depositoId;
    @Column(name = "produto_id", nullable = false) private Long produtoId;
    @Column(nullable = false, precision = 15, scale = 3) private BigDecimal quantidade = BigDecimal.ZERO;
    @Column(name = "atualizado_em", nullable = false) private LocalDateTime atualizadoEm = LocalDateTime.now();
}
