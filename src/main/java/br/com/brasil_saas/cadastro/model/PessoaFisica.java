package br.com.brasil_saas.cadastro.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bc_cad_pessoa_fisica", schema = "brasil_saas")
@Getter @Setter
public class PessoaFisica {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pessoa_id", nullable = false, unique = true)
    private Pessoa pessoa;
    @Column(length = 11)
    private String cpf;
    @Column(length = 20)
    private String rg;
    @Column(name = "orgao_expedidor", length = 20)
    private String orgaoExpedidor;
    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;
    @Column(length = 1, columnDefinition = "bpchar(1)")
    private String sexo;
    @Column(name = "estado_civil", length = 20)
    private String estadoCivil;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
