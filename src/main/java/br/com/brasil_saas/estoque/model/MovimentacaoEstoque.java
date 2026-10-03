package br.com.brasil_saas.estoque.model;

import br.com.brasil_saas.shared.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bc_est_movimentacao", schema = "brasil_saas")
@Getter @Setter @NoArgsConstructor
public class MovimentacaoEstoque extends AuditableEntity {
    @Column(name = "produto_id", nullable = false) private Long produtoId;
    @Column(name = "deposito_id") private Long depositoId;
    @Column(name = "endereco_id") private Long enderecoId;
    @Column(name = "lote_id") private Long loteId;
    @Column(nullable = false) private String tipo;
    private String origem;
    @Column(name = "origem_id") private Long origemId;
    @Column(nullable = false, precision = 15, scale = 3) private BigDecimal quantidade;
    @Column(name = "saldo_apos", precision = 15, scale = 3) private BigDecimal saldoApos;
    @Column(name = "data_movimento", nullable = false) private LocalDateTime dataMovimento = LocalDateTime.now();
    @Column(columnDefinition = "TEXT") private String observacao;
}