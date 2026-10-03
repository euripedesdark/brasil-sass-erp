package br.com.brasil_saas.estoque.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bc_est_lote", schema = "brasil_saas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class LoteEstoque extends TenantEntity {
    @Column(name = "produto_id", nullable = false) private Long produtoId;
    @Column(nullable = false, length = 80) private String codigo;
    @Column(name = "data_fabricacao") private LocalDate dataFabricacao;
    @Column(name = "data_validade") private LocalDate dataValidade;
    @Column(nullable = false, precision = 15, scale = 4) private BigDecimal quantidade = BigDecimal.ZERO;
    @Column(name = "deposito_id") private Long depositoId;
    @Column(nullable = false, length = 20) private String status = "ATIVO";
}