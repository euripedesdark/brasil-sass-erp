package br.com.brasil_saas.core.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "bc_core_documento_fluxo", schema = "brasil_saas")
@Getter @Setter
public class DocumentoFluxo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "origem_tipo", nullable = false, length = 40)
    private String origemTipo;

    @Column(name = "origem_id", nullable = false)
    private Long origemId;

    @Column(name = "origem_numero", length = 60)
    private String origemNumero;

    @Column(name = "destino_tipo", nullable = false, length = 40)
    private String destinoTipo;

    @Column(name = "destino_id", nullable = false)
    private Long destinoId;

    @Column(name = "destino_numero", length = 60)
    private String destinoNumero;

    /** GERA, CONVERTE, FATURA, BAIXA, REFERENCIA */
    @Column(nullable = false, length = 40)
    private String relacao = "GERA";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "created_by")
    private Long createdBy;
}
