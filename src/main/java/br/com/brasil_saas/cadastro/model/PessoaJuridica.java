package br.com.brasil_saas.cadastro.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bc_cad_pessoa_juridica", schema = "brasil_saas")
@Getter @Setter
public class PessoaJuridica {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pessoa_id", nullable = false, unique = true)
    private Pessoa pessoa;
    @Column(length = 14)
    private String cnpj;
    @Column(name = "inscricao_estadual", length = 30)
    private String inscricaoEstadual;
    @Column(name = "inscricao_municipal", length = 30)
    private String inscricaoMunicipal;
    @Column(name = "data_abertura")
    private LocalDate dataAbertura;
    @Column(length = 20)
    private String porte;
    @Column(name = "natureza_juridica", length = 100)
    private String naturezaJuridica;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
