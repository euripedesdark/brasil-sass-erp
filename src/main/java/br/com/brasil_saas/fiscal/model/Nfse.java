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
@Table(name = "bc_fis_nfse", schema = "brasil_saas")
@Getter @Setter
public class Nfse extends TenantEntity {
    @Column(name = "cliente_id")
    private Long clienteId;

    @Column(name = "pessoa_id")
    private Long pessoaId;

    @Column(name = "servico_id")
    private Long servicoId;

    private Long numero;

    @Column(name = "codigo_verificacao", length = 50)
    private String codigoVerificacao;

    @Column(name = "lc116_codigo", length = 10)
    private String lc116Codigo;

    @Column(name = "codigo_tributacao_municipal", length = 20)
    private String codigoTributacaoMunicipal;

    @Column(name = "data_emissao")
    private LocalDateTime dataEmissao;

    /**
     * Chave da nota nacional devolvida pela prefeitura (44 digitos).
     * E o identificador que localiza a nota em qualquer ambiente.
     */
    @Column(name = "chave_nota_nacional", length = 50)
    private String chaveNotaNacional;

    @Column(name = "id_dps", length = 50)
    private String idDps;

    @Column(name = "protocolo_nacional", length = 100)
    private String protocoloNacional;

    /**
     * Serie e numero do RPS que originou a nota.
     *
     * <p>Sem isso nao da para dizer qual RPS gerou a nota quando o numero e
     * reaproveitado depois de um cancelamento — a prefeitura mantem o RPS
     * cancelado e nao o reusa.
     */
    @Column(name = "serie_rps", length = 10)
    private String serieRps;

    @Column(name = "numero_rps", length = 20)
    private String numeroRps;

    @Column(length = 20, nullable = false)
    private String status;

    /**
     * Id do XML assinado no MongoDB. Prazo de guarda: 5 anos.
     *
     * <p>O XML nao fica no Postgres de proposito — o mesmo arquivo em dois
     * lugares cria duas fontes de verdade que divergem.
     */
    @Column(name = "xml_documento_id", length = 50)
    private String xmlDocumentoId;

    /** Id do PDF no MongoDB. Expira em 60 dias. */
    @Column(name = "pdf_documento_id", length = 50)
    private String pdfDocumentoId;

    @Column(columnDefinition = "TEXT")
    private String xml;

    @Column(name = "pdf_url", length = 500)
    private String pdfUrl;

    @Column(name = "base_calculo", precision = 15, scale = 2, nullable = false)
    private BigDecimal baseCalculo;

    @Column(name = "aliquota_iss", precision = 7, scale = 4, nullable = false)
    private BigDecimal aliquotaIss;

    @Column(name = "valor_iss", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorIss;

    @Column(name = "valor_total", precision = 15, scale = 2, nullable = false)
    private BigDecimal valorTotal;
    @Column(name = "tipo_operacao", columnDefinition = "bpchar(1)", nullable = false)
    private String tipoOperacao = "S";
}
