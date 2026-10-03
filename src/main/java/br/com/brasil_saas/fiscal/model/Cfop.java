package br.com.brasil_saas.fiscal.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "bc_fis_cfop", schema = "brasil_saas")
@Getter @Setter
public class Cfop {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 4, nullable = false, unique = true)
    private String codigo;
    @Column(length = 300, nullable = false)
    private String descricao;
    @Column(name = "tipo", length = 20)
    private String tipoOperacao; // ENTRADA, SAIDA
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
