package br.com.brasil_saas.cadastro.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "bc_cad_municipio", schema = "brasil_saas")
@Getter @Setter
public class Municipio {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "codigo_ibge", length = 7, nullable = false, unique = true)
    private String codigoIbge;
    @Column(length = 100, nullable = false)
    private String nome;
    @Column(length = 2, nullable = false, columnDefinition = "bpchar(2)")
    private String uf;
    @Column(name = "codigo_siafi", length = 10)
    private String codigoSiafi;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
