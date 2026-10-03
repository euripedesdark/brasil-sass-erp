package br.com.brasil_saas.bi.model.datalake;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "dim_produto", schema = "brasil_saas_dl")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DimProduto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "produto_id", nullable = false, unique = true)
    private Long produtoId;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @Column(name = "nome", nullable = false, length = 200)
    private String nome;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "categoria_id")
    private Long categoriaId;

    @Column(name = "categoria_nome", length = 100)
    private String categoriaNome;

    @Column(name = "marca_id")
    private Long marcaId;

    @Column(name = "marca_nome", length = 100)
    private String marcaNome;

    @Column(name = "unidade", nullable = false, length = 10)
    private String unidade; // UN, KG, L, M, etc.

    @Column(name = "tipo_produto", nullable = false, length = 50)
    private String tipoProduto; // PRODUTO_ACABADO, MATERIA_PRIMA, EMBALAGEM, SERVICO

    @Column(name = "familia_produto", length = 100)
    private String familiaProduto;

    @Column(name = "grupo_produto", length = 100)
    private String grupoProduto;

    @Column(name = "linha_produto", length = 100)
    private String linhaProduto;

    @Column(name = "ncm", length = 20)
    private String ncm;

    @Column(name = "cest", length = 20)
    private String cest;

    @Column(name = "cst", length = 20)
    private String cst;

    @Column(name = "peso_bruto", precision = 15, scale = 6)
    private BigDecimal pesoBruto;

    @Column(name = "peso_liquido", precision = 15, scale = 6)
    private BigDecimal pesoLiquido;

    @Column(name = "volume", precision = 15, scale = 6)
    private BigDecimal volume;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @Column(name = "data_criacao")
    private LocalDate dataCriacao = LocalDate.now();
}
