package br.com.brasil_saas.cadastro.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "bc_cad_endereco", schema = "brasil_saas")
@Getter @Setter
public class Endereco extends TenantEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoa pessoa;
    @Column(length = 20, nullable = false)
    private String tipo = "PRINCIPAL";
    @Column(length = 200)
    private String logradouro;
    @Column(length = 20)
    private String numero;
    @Column(length = 100)
    private String complemento;
    @Column(length = 100)
    private String bairro;
    @Column(length = 8)
    private String cep;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "municipio_id")
    private Municipio municipio;
    @Column(length = 2, columnDefinition = "bpchar(2)")
    private String uf;
    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;
    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;
    @Column(nullable = false)
    private Boolean principal = Boolean.FALSE;
}
