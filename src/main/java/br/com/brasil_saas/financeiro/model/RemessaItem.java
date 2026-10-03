package br.com.brasil_saas.financeiro.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Um titulo dentro de uma remessa. Ver migration V90. */
@Entity
@Table(name = "bc_fin_remessa_item", schema = "brasil_saas")
@Getter
@Setter
public class RemessaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "remessa_id")
    private Remessa remessa;

    @Column(name = "titulo_id")
    private Long tituloId;

    @Column(name = "boleto_id")
    private Long boletoId;

    @Column(name = "nosso_numero", length = 50)
    private String nossoNumero;

    @Column(name = "valor", precision = 15, scale = 2, nullable = false)
    private BigDecimal valor;

    @Column(name = "vencimento")
    private LocalDate vencimento;

    @Column(name = "codigo_ocorrencia", length = 4)
    private String codigoOcorrencia;

    /*
     * Dados do sacado. O CNAB exige todos eles preenchidos: sem documento,
     * nome, endereco, CEP, cidade e UF o BRCobranca rejeita o registro.
     */
    @Column(name = "documento_sacado", length = 20)
    private String documentoSacado;

    @Column(name = "nome_sacado")
    private String nomeSacado;

    @Column(name = "endereco_sacado")
    private String enderecoSacado;

    @Column(name = "bairro_sacado")
    private String bairroSacado;

    @Column(name = "cep_sacado", length = 10)
    private String cepSacado;

    @Column(name = "cidade_sacado")
    private String cidadeSacado;

    @Column(name = "uf_sacado", length = 2)
    private String ufSacado;

    @Column(name = "criado_em", insertable = false, updatable = false)
    private LocalDateTime criadoEm;
}
