package br.com.brasil_saas.estoque.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bc_est_reserva", schema = "brasil_saas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ReservaEstoque extends TenantEntity {
    @Column(name = "deposito_id", nullable = false) private Long depositoId;
    @Column(name = "produto_id", nullable = false) private Long produtoId;
    @Column(name = "pedido_venda_id") private Long pedidoVendaId;
    @Column(name = "lote_id") private Long loteId;
    @Column(name = "endereco_id") private Long enderecoId;
    @Column(nullable = false, precision = 15, scale = 4) private BigDecimal quantidade;
    @Column(nullable = false, length = 20) private String status = "RESERVADA";
    @Column(name = "data_reserva", nullable = false) private LocalDateTime dataReserva = LocalDateTime.now();
    @Column(name = "data_expiracao") private LocalDateTime dataExpiracao;
}