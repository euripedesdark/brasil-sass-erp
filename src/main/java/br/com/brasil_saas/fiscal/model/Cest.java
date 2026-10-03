package br.com.brasil_saas.fiscal.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "bc_fis_cest", schema = "brasil_saas")
@Getter @Setter
public class Cest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 7, nullable = false)
    private String codigo;
    @Column(length = 300, nullable = false)
    private String descricao;
    @Column(length = 8)
    private String ncm;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
