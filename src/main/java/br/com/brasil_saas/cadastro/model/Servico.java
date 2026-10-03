package br.com.brasil_saas.cadastro.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "bc_cad_servico", schema = "brasil_saas")
@Getter @Setter
public class Servico extends TenantEntity {
    @Column(length = 30, nullable = false)
    private String codigo;
    @Column(length = 200, nullable = false)
    private String nome;
    @Column(columnDefinition = "text")
    private String descricao;
    @Column(name = "lc116_codigo", length = 10)
    private String lc116Codigo;

    /**
     * Codigo de servico da Tabela de Servicos da Prefeitura de Sao Paulo,
     * com 4 digitos. Ex.: {@code 2919} = suporte tecnico em informatica
     * (LC 116 {@code 01.07}).
     *
     * <p>É este codigo, e nao o {@code lc116Codigo}, que vai no RPS e na
     * assinatura. Sem ele a prefeitura recusa a emissao (erro 306).
     */
    @Column(name = "codigo_tributacao_municipal", length = 10)
    private String codigoTributacaoMunicipal;
    @Column(length = 12)
    private String nbs;
    @Column(name = "aliquota_iss", precision = 7, scale = 4, nullable = false)
    private BigDecimal aliquotaIss = BigDecimal.ZERO;
    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal preco = BigDecimal.ZERO;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidade_medida_id")
    private UnidadeMedida unidadeMedida;
    @Column(nullable = false)
    private Boolean ativo = Boolean.TRUE;
}
