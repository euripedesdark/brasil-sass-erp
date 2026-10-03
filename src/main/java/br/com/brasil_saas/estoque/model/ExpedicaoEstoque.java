package br.com.brasil_saas.estoque.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name="bc_est_expedicao", schema="brasil_saas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ExpedicaoEstoque extends TenantEntity {
    @Column(name="pedido_venda_id", nullable=false) private Long pedidoVendaId;
    @Column(name="deposito_id", nullable=false) private Long depositoId;
    @Column(nullable=false, length=20) private String status="ABERTA";
    @Column(name="transportadora_id") private Long transportadoraId;
    @Column(name="codigo_rastreio", length=120) private String codigoRastreio;
    @Column(name="data_abertura", nullable=false) private LocalDateTime dataAbertura=LocalDateTime.now();
    @Column(name="data_separacao") private LocalDateTime dataSeparacao;
    @Column(name="data_embalagem") private LocalDateTime dataEmbalagem;
    @Column(name="data_expedicao") private LocalDateTime dataExpedicao;
    @Column(columnDefinition="TEXT") private String observacoes;
}