package br.com.brasil_saas.financeiro.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Uma linha do retorno CNAB. Ver migration V90. */
@Entity
@Table(name = "bc_fin_retorno_item", schema = "brasil_saas")
@Getter
@Setter
public class RetornoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "retorno_id")
    private RetornoBancario retorno;

    @Column(name = "titulo_id")
    private Long tituloId;

    @Column(name = "boleto_id")
    private Long boletoId;

    @Column(name = "codigo_registro", length = 4)
    private String codigoRegistro;

    @Column(name = "codigo_ocorrencia", length = 4)
    private String codigoOcorrencia;

    @Column(name = "nosso_numero", length = 50)
    private String nossoNumero;

    @Column(name = "valor_titulo", precision = 15, scale = 2)
    private BigDecimal valorTitulo;

    @Column(name = "valor_recebido", precision = 15, scale = 2)
    private BigDecimal valorRecebido;

    @Column(name = "juros", precision = 15, scale = 2)
    private BigDecimal juros;

    @Column(name = "multa", precision = 15, scale = 2)
    private BigDecimal multa;

    @Column(name = "desconto", precision = 15, scale = 2)
    private BigDecimal desconto;

    @Column(name = "data_credito")
    private LocalDate dataCredito;

    @Column(name = "data_ocorrencia")
    private LocalDate dataOcorrencia;

    @Column(name = "aplicado", nullable = false)
    private Boolean aplicado = false;

    @Column(name = "observacao", columnDefinition = "text")
    private String observacao;

    @Column(name = "criado_em", insertable = false, updatable = false)
    private LocalDateTime criadoEm;
}
