package br.com.brasil_saas.plm.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity @Table(name = "bc_plm_mudanca", schema = "brasil_saas")
@Getter @Setter @NoArgsConstructor
public class PlmMudanca {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "empresa_id", nullable = false) private Long empresaId;
    @Column(nullable = false, length = 80) private String numero;
    @Column(length = 30) private String tipo = "ENGENHARIA";
    @Column(nullable = false, length = 200) private String titulo;
    @Column(columnDefinition = "TEXT") private String descricao;
    @Column(length = 20) private String prioridade = "NORMAL";
    @Column(nullable = false, length = 25) private String status = "ABERTA";
    @Column(name = "solicitante_id") private Long solicitanteId;
    @Column(name = "aprovador_id") private Long aprovadorId;
    @Column(name = "aprovado_em") private LocalDateTime aprovadoEm;
    @Column(name = "implementado_em") private LocalDateTime implementadoEm;
    @Column(columnDefinition = "TEXT") private String observacao;
    @Column(name = "created_at") private LocalDateTime createdAt;
    @Column(name = "updated_at") private LocalDateTime updatedAt;
    @Column(name = "created_by") private Long createdBy;
    @Column(name = "updated_by") private Long updatedBy;
    @Column(name = "deleted_at") private LocalDateTime deletedAt;
}
