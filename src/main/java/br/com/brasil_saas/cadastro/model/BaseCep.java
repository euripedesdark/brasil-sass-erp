package br.com.brasil_saas.cadastro.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "bc_cad_base_cep", schema = "brasil_saas")
@Getter @Setter
public class BaseCep {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 8, nullable = false)
    private String cep;
    @Column(length = 200)
    private String logradouro;
    @Column(length = 100)
    private String bairro;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "municipio_id")
    private Municipio municipio;
    @Column(length = 2, columnDefinition = "bpchar(2)")
    private String uf;
    @Column(length = 30)
    private String tipo;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
