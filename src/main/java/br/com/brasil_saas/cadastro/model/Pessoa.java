package br.com.brasil_saas.cadastro.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bc_cad_pessoa", schema = "brasil_saas")
@Getter @Setter
public class Pessoa extends TenantEntity {
    @Column(length = 10, nullable = false)
    private String tipo; // FISICA | JURIDICA
    @Column(length = 200, nullable = false)
    private String nome;
    @Column(length = 14)
    private String documento;
    @Column(length = 150)
    private String email;
    @Column(length = 20)
    private String telefone;
    @Column(length = 20, nullable = false)
    private String status;
    @Column(columnDefinition = "text")
    private String observacao;

    @OneToOne(mappedBy = "pessoa", cascade = CascadeType.ALL, orphanRemoval = true)
    private PessoaFisica fisica;
    @OneToOne(mappedBy = "pessoa", cascade = CascadeType.ALL, orphanRemoval = true)
    private PessoaJuridica juridica;
    @OneToMany(mappedBy = "pessoa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Endereco> enderecos = new ArrayList<>();
    @OneToMany(mappedBy = "pessoa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Contato> contatos = new ArrayList<>();
}
