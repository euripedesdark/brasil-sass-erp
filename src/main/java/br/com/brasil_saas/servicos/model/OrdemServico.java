package br.com.brasil_saas.servicos.model;
import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*; import lombok.*;
import java.math.BigDecimal; import java.time.LocalDateTime;
@Entity @Table(name="bc_srv_ordem_servico", schema="brasil_saas") @Getter @Setter
public class OrdemServico extends TenantEntity {
    @Column(nullable=false, length=20) private String numero;
    @Column(name="cliente_id", nullable=false) private Long clienteId;
    @Column(length=200) private String equipamento;
    @Column(columnDefinition="text") private String descricao;
    @Column(length=20, nullable=false) private String status = "ABERTA";
    @Column(name="valor_total", precision=15, scale=2, nullable=false) private BigDecimal valorTotal = BigDecimal.ZERO;
    @Column(name="usuario_id") private Long usuarioId;
    @Column(name="abertura_at", nullable=false) private LocalDateTime aberturaAt;
    @Column(name="previsao_at") private LocalDateTime previsaoAt;
    @Column(name="fechamento_at") private LocalDateTime fechamentoAt;
    @Column(columnDefinition="text") private String laudo;
}
