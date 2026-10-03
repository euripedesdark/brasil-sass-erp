package br.com.brasil_saas.bi.model.datalake;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "dim_cliente", schema = "brasil_saas_dl")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DimCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "cliente_id", nullable = false, unique = true)
    private Long clienteId;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "cpf_cnpj", nullable = false, length = 20)
    private String cpfCnpj;

    @Column(name = "tipo_pessoa", nullable = false, length = 20)
    private String tipoPessoa; // FISICA, JURIDICA

    @Column(name = "segmento", length = 100)
    private String segmento;

    @Column(name = "regiao", length = 100)
    private String regiao;

    @Column(name = "estado", length = 2)
    private String estado;

    @Column(name = "cidade", length = 100)
    private String cidade;

    @Column(name = "grupo_cliente", length = 100)
    private String grupoCliente;

    @Column(name = "classificacao", length = 20)
    private String classificacao; // A, B, C (Curva ABC)

    @Column(name = "data_primeira_compra")
    private LocalDate dataPrimeiraCompra;

    @Column(name = "data_ultima_compra")
    private LocalDate dataUltimaCompra;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @Column(name = "data_criacao")
    private LocalDate dataCriacao = LocalDate.now();
}
