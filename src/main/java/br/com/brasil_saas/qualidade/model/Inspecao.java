package br.com.brasil_saas.qualidade.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "bc_qual_inspecao", schema = "brasil_saas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Inspecao extends TenantEntity {
    @Column(name="plano_id") private Long planoId;
    @Column(name="referencia_tipo", nullable=false, length=30) private String referenciaTipo;
    @Column(name="referencia_id") private Long referenciaId;
    @Column(nullable=false, length=50) private String numero;
    @Column(name="data_inspecao", nullable=false) private LocalDate dataInspecao;
    @Column(nullable=false, length=30) private String status = "ABERTA";
    @Column(length=30) private String resultado;
    @Column(length=1000) private String observacao;
}
