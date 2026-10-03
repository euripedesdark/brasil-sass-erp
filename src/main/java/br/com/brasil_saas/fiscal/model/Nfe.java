package br.com.brasil_saas.fiscal.model;

import br.com.brasil_saas.shared.model.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bc_fis_nfe", schema = "brasil_saas")
@Getter @Setter
public class Nfe extends TenantEntity {
    @Column(name = "pedido_compra_id")
    private Long pedidoCompraId;

    @Column(name = "pedido_venda_id")
    private Long pedidoVendaId;

    @Column(name = "documento_origem_tipo", length = 30)
    private String documentoOrigemTipo;

    @Column(name = "documento_origem_id")
    private Long documentoOrigemId;

    @Column(name = "cliente_id")
    private Long clienteId;

    @Column(name = "pessoa_id")
    private Long pessoaId;

    private Long numero;

    @Column(length = 10)
    private String serie;

    @Column(name = "chave_acesso", length = 50)
    private String chaveAcesso;

    @Column(name = "natureza_operacao", length = 100)
    private String naturezaOperacao;

    @Column(length = 4)
    private String cfop;

    @Column(name = "data_emissao")
    private LocalDateTime dataEmissao;

    @Column(name = "data_saida")
    private LocalDateTime dataSaida;

    @Column(length = 20, nullable = false)
    private String status;

    @Column(length = 50)
    private String protocolo;

    @Column(columnDefinition = "TEXT")
    private String xml;

    @Column(name = "danfe_url", length = 500)
    private String danfeUrl;

    @Column(name = "valor_produtos", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorProdutos;

    @Column(name = "valor_frete", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorFrete;

    @Column(name = "valor_desconto", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorDesconto;

    @Column(name = "valor_icms", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorIcms;

    @Column(name = "valor_ipi", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorIpi;

    @Column(name = "valor_pis", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorPis;

    @Column(name = "valor_cofins", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorCofins;

    @Column(name = "valor_total", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorTotal;
    @Column(name = "tipo_operacao", columnDefinition = "bpchar(1)", nullable = false)
    private String tipoOperacao = "S";
}
