package br.com.brasil_saas.bi.model.datalake;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "dim_empresa", schema = "brasil_saas_dl")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DimEmpresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "empresa_id", nullable = false, unique = true)
    private Long empresaId;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "cnpj", length = 20)
    private String cnpj;

    @Column(name = "razao_social", length = 200)
    private String razaoSocial;

    @Column(name = "grupo_economico", length = 100)
    private String grupoEconomico;

    @Column(name = "segmento", length = 100)
    private String segmento;

    @Column(name = "porte", length = 20)
    private String porte; // MICRO, PEQUENA, MEDIA, GRANDE

    @Column(name = "data_fundacao")
    private LocalDate dataFundacao;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @Column(name = "data_criacao")
    private LocalDate dataCriacao = LocalDate.now();
}
