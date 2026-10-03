package br.com.brasil_saas.fiscal.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bc_fis_servico_lc116", schema = "brasil_saas")
@Getter @Setter
public class ServicoLc116 {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 10, nullable = false, unique = true)
    private String codigo;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String descricao;

    @Column(name = "aliquota_min", precision = 7, scale = 4)
    private BigDecimal aliquotaMin;

    @Column(name = "aliquota_max", precision = 7, scale = 4)
    private BigDecimal aliquotaMax;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
