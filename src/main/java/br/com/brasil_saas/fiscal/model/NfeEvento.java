package br.com.brasil_saas.fiscal.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "bc_fis_nfe_evento", schema = "brasil_saas")
@Getter @Setter
public class NfeEvento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nfe_id", nullable = false)
    private Nfe nfe;

    @Column(length = 30, nullable = false)
    private String tipo;

    @Column(nullable = false)
    private Integer sequencia = 1;

    @Column(length = 50)
    private String protocolo;

    @Column(name = "data_evento")
    private LocalDateTime dataEvento;

    @Column(columnDefinition = "TEXT")
    private String xml;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;
}
