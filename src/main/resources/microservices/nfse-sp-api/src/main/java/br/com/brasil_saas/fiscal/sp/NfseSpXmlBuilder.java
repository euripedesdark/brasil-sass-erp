package br.com.brasil_saas.fiscal.sp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Monta o XML das mensagens da Prefeitura de Sao Paulo.
 *
 * <p>Tres regras da prefeitura que custam nota se ignoradas:
 * <ol>
 *   <li>o atributo {@code Versao} do cabecalho segue o leiaute
 *       ({@code brasil-saas.fiscal.nfse-sp.xsd-version});</li>
 *   <li>campo opcional em branco <b>nao</b> pode ser emitido — a prefeitura
 *       valida o conteudo contra o XSD e
 *       {@code <InscricaoMunicipalTomador></...>} vazio e recusado;</li>
 *   <li>a ordem dos elementos importa: o XSD e um {@code xs:sequence}, e o
 *       {@code NumeroEncapsulamento} tem de vir antes de
 *       {@code ValorTotalRecebido} e {@code RetencaoPisCofins}.</li>
 * </ol>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NfseSpXmlBuilder {

    private static final String NS = "http://www.prefeitura.sp.gov.br/nfe";

    private final NfseSpProperties properties;

    // ------------------------------------------------------------------
    // Consulta de CNPJ
    // ------------------------------------------------------------------

    /** Mensagem (sem envelope): assine antes de chamar {@link #envolverConsultaCnpj}. */
    public String consultaCnpj(String cnpjRemetente, String cnpjContribuinte) {
        return "<PedidoConsultaCNPJ xmlns=\"" + NS + "\">"
                + cabecalho(cnpjRemetente)
                + "<CNPJContribuinte xmlns=\"\"><CNPJ>" + xml(cnpjContribuinte) + "</CNPJ></CNPJContribuinte>"
                + "</PedidoConsultaCNPJ>";
    }

    // ------------------------------------------------------------------
    // Envio de RPS
    // ------------------------------------------------------------------

    /**
     * Monta o {@code PedidoEnvioRPS}.
     *
     * <p>A assinatura chega pronta: ela sai da cadeia de 86 posicoes, que
     * depende so dos campos do RPS e nao do XML. Calcular antes e injetar aqui
     * evita montar o XML duas vezes.
     *
     * <p>Campos do leiaute 2 ({@code IBSCBS}, {@code NCM}, {@code NBS},
     * {@code ValorMulta}...) sao emitidos <b>somente</b> com o leiaute 2, e
     * {@code ValorServicos} somente no leiaute 1 — ele e obrigatorio no XSD v01
     * e nao existe no v2. Mandar o errado nos dois sentidos faz a prefeitura
     * recusar.
     */
    public String envioRps(Rps rps, String assinatura) {
        boolean v2 = properties.getXsdVersion() >= 2;

        StringBuilder s = new StringBuilder();
        // xmlns="" de proposito: o RPS e os campos internos nao ficam no
        // namespace da prefeitura. Sem isso a resposta e
        // "has invalid child element 'RPS' in namespace
        //  'http://www.prefeitura.sp.gov.br/nfe'" — que parece elemento errado
        // e e namespace.
        s.append("<RPS xmlns=\"\">");

        s.append(tag("Assinatura", assinatura));
        s.append(tag("ChaveRPS",
                tag("InscricaoPrestador", rps.imPrestador())
                        + tag("SerieRPS", rps.serieRps())
                        + tag("NumeroRPS", rps.numeroRps())));

        s.append(tag("TipoRPS", rps.tipoRps()));
        s.append(tag("DataEmissao", rps.dataEmissao()));
        s.append(tag("StatusRPS", rps.statusRps()));
        s.append(tag("TributacaoRPS", rps.tributacaoRps()));

        if (!v2) {
            // obrigatorio no XSD v01 (minOccurs=1)
            s.append(tag("ValorServicos", rps.valorServicos()));
        }
        s.append(tag("ValorDeducoes", rps.valorDeducoes()));
        s.append(tagOptional("ValorPIS", rps.valorPis()));
        s.append(tagOptional("ValorCOFINS", rps.valorCofins()));
        s.append(tagOptional("ValorINSS", rps.valorInss()));
        s.append(tagOptional("ValorIR", rps.valorIr()));
        s.append(tagOptional("ValorCSLL", rps.valorCsll()));

        s.append(tag("CodigoServico", rps.codigoServico()));
        s.append(tag("AliquotaServicos", rps.aliquotaServicos()));
        // XSD tipa ISSRetido como xs:boolean: "true"/"false", nao S/N
        s.append(tag("ISSRetido", Boolean.toString(rps.issRetido())));

        s.append(cpfcnpjTomador(rps));
        s.append(tagOptional("InscricaoMunicipalTomador", rps.inscricaoMunicipalTomador()));
        s.append(tagOptional("InscricaoEstadualTomador", rps.inscricaoEstadualTomador()));
        s.append(tagOptional("RazaoSocialTomador", rps.razaoSocialTomador()));
        s.append(enderecoTomador(rps));
        s.append(tagOptional("EmailTomador", rps.emailTomador()));

        s.append(cpfcnpjIntermediario(rps));
        s.append(tagOptional("InscricaoMunicipalIntermediario", rps.inscricaoMunicipalIntermediario()));
        s.append(tagOptional("ISSRetidoIntermediario", rps.issRetidoIntermediario()));
        s.append(tagOptional("EmailIntermediario", rps.emailIntermediario()));

        s.append(tag("Discriminacao", rps.discriminacao()));
        s.append(tagOptional("ValorCargaTributaria", rps.valorCargaTributaria()));
        s.append(tagOptional("PercentualCargaTributaria", rps.percentualCargaTributaria()));
        s.append(tagOptional("FonteCargaTributaria", rps.fonteCargaTributaria()));
        s.append(tagOptional("CodigoCEI", rps.codigoCei()));
        s.append(tagOptional("MatriculaObra", rps.matriculaObra()));
        s.append(tagOptional("MunicipioPrestacao", rps.municipioPrestacao()));

        // ordem exigida pelo xs:sequence
        s.append(tagOptional("NumeroEncapsulamento", rps.numeroEncapsulamento()));
        s.append(tagOptional("ValorTotalRecebido", rps.valorTotalRecebido()));
        s.append(tagOptional("RetencaoPisCofins", rps.retencaoPisCofins()));

        if (v2) {
            // ValorInicialCobrado foi descontinuado (erro 640): use o Final
            s.append(tagOptional("ValorFinalCobrado", rps.valorFinalCobrado()));
            s.append(tagOptional("ValorMulta", rps.valorMulta()));
            s.append(tagOptional("ValorJuros", rps.valorJuros()));
            s.append(tagOptional("ValorIPI", rps.valorIpi()));
            s.append(tagOptional("ExigibilidadeSuspensa", rps.exigibilidadeSuspensa()));
            s.append(tagOptional("NCM", rps.ncm()));
            s.append(tagOptional("NBS", rps.nbs()));
            if (rps.cLocPrestacao() != null && !rps.cLocPrestacao().isBlank()) {
                s.append(tag("cLocPrestacao", rps.cLocPrestacao()));
            } else {
                s.append(tagOptional("cPaisPrestacao", rps.cPaisPrestacao()));
            }
            s.append(ibsCbs(rps.ibsCbs()));
        }

        s.append("</RPS>");

        return "<PedidoEnvioRPS xmlns=\"" + NS + "\">"
                + cabecalho(rps.cnpjRemetente())
                + s
                + "</PedidoEnvioRPS>";
    }

    // ------------------------------------------------------------------
    // Cancelamento
    // ------------------------------------------------------------------

    public String cancelamento(String cnpjRemetente, List<DetalheCancelamento> detalhes) {
        StringBuilder s = new StringBuilder();
        s.append("<PedidoCancelamentoNFe xmlns=\"").append(NS).append("\">");

        // <transacao> e FILHO de <Cabecalho>, com a inicial minuscula, e vem
        // depois de <CPFCNPJRemetente>. Fora do cabecalho a prefeitura
        // responde "Cabecalho has incomplete content, expected 'transacao'".
        s.append("<Cabecalho xmlns=\"\" Versao=\"").append(properties.getXsdVersion()).append("\">")
                .append("<CPFCNPJRemetente><CNPJ>").append(xml(cnpjRemetente))
                .append("</CNPJ></CPFCNPJRemetente>")
                .append("<transacao>true</transacao>")
                .append("</Cabecalho>");

        for (DetalheCancelamento d : detalhes) {
            StringBuilder chave = new StringBuilder();
            chave.append(tag("InscricaoPrestador", d.inscricaoPrestador()));
            chave.append(tag("NumeroNFe", d.numeroNfe()));
            if (d.codigoVerificacao() != null && !d.codigoVerificacao().isBlank()) {
                chave.append(tag("CodigoVerificacao", d.codigoVerificacao()));
            }
            if (d.chaveNotaNacional() != null && !d.chaveNotaNacional().isBlank()) {
                chave.append(tag("ChaveNotaNacional", d.chaveNotaNacional()));
            }
            // <Detalhe> fora do namespace da prefeitura (xmlns=""), como o
            // <RPS> do envio. Sem isso: "has invalid child element 'Detalhe'
            // in namespace 'http://www.prefeitura.sp.gov.br/nfe'".
            s.append("<Detalhe xmlns=\"\">")
                    .append("<ChaveNFe>").append(chave).append("</ChaveNFe>")
                    .append("<AssinaturaCancelamento>")
                    .append(xml(d.assinaturaCancelamento()))
                    .append("</AssinaturaCancelamento>")
                    .append("</Detalhe>");
        }
        s.append("</PedidoCancelamentoNFe>");

        return s.toString();
    }

    /**
     * Cadeia do {@code AssinaturaCancelamento}: IM com 8 + número da NF-e com
     * 12, ambos com zeros <b>à esquerda</b>.
     *
     * <p>Zerar à direita (o que {@code %-8s} faria) gera {@code 21300330} em
     * vez de {@code 02130033}, e a prefeitura responde
     * {@code 1305 "Assinatura de cancelamento incorreta"} sem dizer qual das
     * duas versões ela calculou.
     */
    public String cadeiaCancelamento(String inscricaoPrestador, String numeroNfe) {
        return zerosAEsquerda(digitos(inscricaoPrestador), 8)
                + zerosAEsquerda(digitos(numeroNfe), 12);
    }

    private String zerosAEsquerda(String valor, int tamanho) {
        if (valor.length() >= tamanho) {
            return valor.substring(valor.length() - tamanho);
        }
        return "0".repeat(tamanho - valor.length()) + valor;
    }

    // ------------------------------------------------------------------
    // SOAP
    // ------------------------------------------------------------------

    /**
     * Embrulha a mensagem <b>ja assinada</b> no envelope SOAP.
     *
     * <p>Duas decisões que vieram do WSDL e que nao se adivinham:
     * <p>Protocolo: <b>SOAP 1.2</b> no envelope, com o cabecalho
     * {@code SOAPAction} mesmo assim. O WSDL publica os dois bindings, mas o
     * 1.1 responde {@code "Server did not recognize the value of HTTP Header
     * SOAPAction"} e o 1.2 sem o cabecalho responde
     * {@code "Unable to handle request without a valid action parameter"} —
     * ou seja, so a combinacao 1.2 + cabecalho funciona.
     *
     * <p>E o elemento do corpo leva o sufixo {@code Request}
     * ({@code ConsultaCNPJRequest}), que e o nome global do WSDL — sem ele a
     * prefeitura responde {@code 1102 "Mensagem XML de Pedido do servico
     * sem conteudo"}.
     *
     * <p>Alem disso a assinatura vai DENTRO do {@code MensagemXML}: o XSD
     * declara {@code Signature} como filho da raiz da mensagem (item P3 de
     * PedidoEnvioRPS.xsd), entao assinar o SOAP por fora e recusado.
     */
    public String envelopar(NfseSpOperacoes.Operacao operacao, String mensagemAssinada) {
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">"
                + "<soap:Body>"
                + "<" + operacao.elemento() + " xmlns=\"" + NS + "\">"
                + "<VersaoSchema>" + properties.getXsdVersion() + "</VersaoSchema>"
                + "<MensagemXML><![CDATA[" + mensagemAssinada.replace("\n", "") + "]]></MensagemXML>"
                + "</" + operacao.elemento() + ">"
                + "</soap:Body>"
                + "</soap:Envelope>";
    }

    /** Nome da operacao no envelope, para cada mensagem. */
    public String enveloparConsultaCnpj(String mensagemAssinada) {
        return envelopar(NfseSpOperacoes.CONSULTA_CNPJ, mensagemAssinada);
    }

    public String enveloparEnvioRps(String mensagemAssinada) {
        return envelopar(NfseSpOperacoes.ENVIO_RPS, mensagemAssinada);
    }

    public String enveloparCancelamento(String mensagemAssinada) {
        return envelopar(NfseSpOperacoes.CANCELAMENTO_NFE, mensagemAssinada);
    }

    private String cabecalho(String cnpjRemetente) {
        return "<Cabecalho xmlns=\"\" Versao=\"" + properties.getXsdVersion() + "\">"
                + "<CPFCNPJRemetente><CNPJ>" + xml(cnpjRemetente) + "</CNPJ></CPFCNPJRemetente>"
                + "</Cabecalho>";
    }

    /** tpCPFCNPJNIF: o XSD quer exatamente um dos quatro. */
    private String cpfcnpjTomador(Rps rps) {
        if (emBranco(rps.cpfTomador())) {
            if (!emBranco(rps.cnpjTomador())) {
                return "<CPFCNPJTomador xmlns=\"\"><CNPJ>" + xml(rps.cnpjTomador()) + "</CNPJ></CPFCNPJTomador>";
            }
            if (!emBranco(rps.nifTomador())) {
                return "<CPFCNPJTomador xmlns=\"\"><NIF>" + xml(rps.nifTomador()) + "</NIF></CPFCNPJTomador>";
            }
            return "<CPFCNPJTomador xmlns=\"\"><NaoNIF>false</NaoNIF></CPFCNPJTomador>";
        }
        return "<CPFCNPJTomador xmlns=\"\"><CPF>" + xml(rps.cpfTomador()) + "</CPF></CPFCNPJTomador>";
    }

    private String cpfcnpjIntermediario(Rps rps) {
        if (emBranco(rps.cpfIntermediario()) && emBranco(rps.cnpjIntermediario())) {
            return "";
        }
        if (!emBranco(rps.cpfIntermediario())) {
            return "<CPFCNPJIntermediario xmlns=\"\"><CPF>" + xml(rps.cpfIntermediario()) + "</CPF></CPFCNPJIntermediario>";
        }
        return "<CPFCNPJIntermediario xmlns=\"\"><CNPJ>" + xml(rps.cnpjIntermediario()) + "</CNPJ></CPFCNPJIntermediario>";
    }

    private String enderecoTomador(Rps rps) {
        if (rps.enderecoTomador() == null) {
            return "";
        }
        Endereco e = rps.enderecoTomador();
        return tag("EnderecoTomador",
                tagOptional("TipoLogradouro", e.tipoLogradouro())
                        + tagOptional("Logradouro", e.logradouro())
                        + tagOptional("Numero", e.numero())
                        + tagOptional("Complemento", e.complemento())
                        + tagOptional("Bairro", e.bairro())
                        + tagOptional("Cidade", e.cidade())
                        + tagOptional("UF", e.uf())
                        + tagOptional("CEP", e.cep()));
    }

    private String ibsCbs(IbsCbs b) {
        if (b == null) {
            return "";
        }
        return tag("IBSCBS",
                tagOptional("finNFSe", b.finNfse())
                        + tagOptional("indFinal", b.indFinal())
                        + tagOptional("cIndOp", b.cIndOp())
                        + tagOptional("indDest", b.indDest())
                        + valoresIbsCbs(b));
    }

    private String valoresIbsCbs(IbsCbs b) {
        if (b.cClassTrib() == null || b.cClassTrib().isBlank()) {
            return "";
        }
        return tag("valores", tag("trib", tag("gIBSCBS",
                tag("cClassTrib", b.cClassTrib()))));
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    /**
     * Emite a tag so quando o valor tem conteudo.
     *
     * <p>Campo opcional vazio e pior que campo ausente: a prefeitura valida o
     * conteudo e {@code <InscricaoMunicipalTomador></...>} gera
     * "The value '' is invalid according to its datatype".
     */
    private String tagOptional(String nome, String valor) {
        if (emBranco(valor)) {
            return "";
        }
        return tag(nome, valor);
    }

    private String tag(String nome, String conteudo) {
        return "<" + nome + ">" + conteudo + "</" + nome + ">";
    }

    /** Escapa o minimo para o conteudo nao quebrar o XML. */
    private String xml(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private boolean emBranco(String v) {
        return v == null || v.isBlank();
    }

    private String digitos(String v) {
        return v == null ? "" : v.replaceAll("\\D", "");
    }

    // ------------------------------------------------------------------
    // DTOs
    // ------------------------------------------------------------------

    public record Endereco(String tipoLogradouro, String logradouro, String numero,
                           String complemento, String bairro, String cidade,
                           String uf, String cep) {
    }

    public record IbsCbs(String finNfse, String indFinal, String cIndOp,
                         String indDest, String cClassTrib) {
    }

    public record DetalheCancelamento(String inscricaoPrestador, String numeroNfe,
                                      String codigoVerificacao, String chaveNotaNacional,
                                      String assinaturaCancelamento) {
    }

    /** Um RPS a emitir. */
    public record Rps(
            String cnpjRemetente,
            String imPrestador, String serieRps, String numeroRps,
            String tipoRps, String dataEmissao, String statusRps, String tributacaoRps,
            String valorServicos, String valorDeducoes,
            String valorPis, String valorCofins, String valorInss, String valorIr, String valorCsll,
            String codigoServico, String aliquotaServicos, boolean issRetido,
            String cpfTomador, String cnpjTomador, String nifTomador,
            String inscricaoMunicipalTomador, String inscricaoEstadualTomador,
            String razaoSocialTomador, Endereco enderecoTomador, String emailTomador,
            String cpfIntermediario, String cnpjIntermediario,
            String inscricaoMunicipalIntermediario, String issRetidoIntermediario,
            String emailIntermediario,
            String discriminacao,
            String valorCargaTributaria, String percentualCargaTributaria,
            String fonteCargaTributaria, String codigoCei, String matriculaObra,
            String municipioPrestacao,
            String numeroEncapsulamento, String valorTotalRecebido, String retencaoPisCofins,
            // leiaute 2
            String valorFinalCobrado, String valorMulta, String valorJuros, String valorIpi,
            String exigibilidadeSuspensa, String ncm, String nbs,
            String cLocPrestacao, String cPaisPrestacao, IbsCbs ibsCbs) {

        /** RPS minimo do leiaute 1, o caso da SrvCloud (Simples Nacional). */
        public static Rps minimo(String cnpjRemetente, String im, String serie, String numero,
                                 String dataEmissao, String codigoServico, String aliquota,
                                 String valorServicos, String deducoes, String cpfOuCnpjTomador,
                                 boolean tomadorEhCpf, String razaoSocial, String email,
                                 String discriminacao, String tributacao) {
            return new Rps(cnpjRemetente, im, serie, numero, "RPS", dataEmissao, "N", tributacao,
                    valorServicos, deducoes, "0.00", "0.00", "0.00", "0.00", "0.00",
                    codigoServico, aliquota, false,
                    tomadorEhCpf ? cpfOuCnpjTomador : null, tomadorEhCpf ? null : cpfOuCnpjTomador, null,
                    null, null, razaoSocial, null, email,
                    null, null, null, null, null,
                    discriminacao, null, null, null, null, null, null,
                    null, null, null,
                    null, null, null, null, null, null, null, null, null, null);
        }
    }
}
