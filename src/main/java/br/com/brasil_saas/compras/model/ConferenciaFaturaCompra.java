package br.com.brasil_saas.compras.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "bc_com_conferencia_fatura", schema = "brasil_saas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ConferenciaFaturaCompra extends TenantEntity {
    @Column(name="pedido_id", nullable=false) private Long pedidoId;
    @Column(name="recebimento_id") private Long recebimentoId;
    @Column(name="titulo_id") private Long tituloId;
    @Column(name="nfe_id") private Long nfeId;
    @Column(name="valor_pedido", precision=15, scale=2, nullable=false) private BigDecimal valorPedido = BigDecimal.ZERO;
    @Column(name="valor_recebido", precision=15, scale=2, nullable=false) private BigDecimal valorRecebido = BigDecimal.ZERO;
    @Column(name="valor_fatura", precision=15, scale=2, nullable=false) private BigDecimal valorFatura = BigDecimal.ZERO;
    @Column(name="tolerancia", precision=7, scale=4, nullable=false) private BigDecimal tolerancia = BigDecimal.ZERO;
    @Column(name="status", length=20, nullable=false) private String status = "PENDENTE";
    @Column(name="divergencia", columnDefinition="TEXT") private String divergencia;
}