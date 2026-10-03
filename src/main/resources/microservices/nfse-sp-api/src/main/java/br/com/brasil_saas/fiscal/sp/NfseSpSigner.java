package br.com.brasil_saas.fiscal.sp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

/**
 * Assinatura digital exigida pela Prefeitura de Sao Paulo.
 *
 * <p> Sao duas assinaturas distintas e confundem-las custa a nota:
 * <ol>
 *   <li><b>assinatura do XML</b> ({@link #assinarXml}) — XMLDSig padrao, envelopada
 *       na raiz da mensagem. E o que a prefeitura exige em todas as mensagens.</li>
 *   <li><b>assinatura do RPS</b> ({@link #assinarRps}) — SHA1 de uma cadeia de
 *       caracteres de 86 posicoes com os dados do RPS, assinado em RSA. Vai no
 *       campo {@code Assinatura}.</li>
 * </ol>
 *
 * <p>A geometria da cadeia depende do leiaute (item 4.3.2 do manual), e errar
 * nela devolve o erro <b>1206</b>, que ecoa a string verificada e por isso e
 * facil de diagnosticar.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NfseSpSigner {

    private static final String NS_XMLDSIG = "http://www.w3.org/2000/09/xmldsig#";
    private static final String ALGO_C14N_EXCLUSIVA = "http://www.w3.org/2001/10/xml-exc-c14n#";
    private static final String ALGO_RSA_SHA1 = "http://www.w3.org/2000/09/xmldsig#rsa-sha1";
    private static final String ALGO_SHA1 = "http://www.w3.org/2000/09/xmldsig#sha1";

    private final NfseSpCertificadoService certificadoService;

    // ------------------------------------------------------------------
    // 1) assinatura do RPS: SHA1 da cadeia de 86 posicoes
    // ------------------------------------------------------------------

    /**
     * Assina uma cadeia de caracteres no padrao da prefeitura.
     *
     * <p>Algoritmo: SHA1 do texto, assinado em RSA, devolvido em Base64.
     */
    public String assinarTexto(String texto, PrivateKey chave) {
        try {
            Signature signature = Signature.getInstance("SHA1withRSA");
            signature.initSign(chave);
            signature.update(texto.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (Exception e) {
            throw new NfseSpException("Falha ao assinar a cadeia do RPS: " + e.getMessage(), e);
        }
    }

    /**
     * Monta a cadeia de 86 posicoes do RPS e assina.
     *
     * <p>Posicoes (item 4.3.2 do manual da Web Service):
     * <ol start="0">
     *   <li>IM do prestador — <b>8 digitos no leiaute 1</b>, 12 no leiaute 2</li>
     *   <li>Serie do RPS — 5, alinhado a esquerda, preenchido com espaco</li>
     *   <li>Numero do RPS — 12 digitos</li>
     *   <li>Data de emissao — AAAAMMDD</li>
     *   <li>Tributacao — 1</li>
     *   <li>Status — 1</li>
     *   <li>ISS retido — S ou N</li>
     *   <li>Valor — <b>Valor dos Servicos no leiaute 1</b>,
     *       Inicial/Final Cobrado no leiaute 2. 15 digitos.</li>
     *   <li>Valor das deducoes — 15</li>
     *   <li>Codigo do servico — 5</li>
     *   <li>Indicador CPF/CNPJ do tomador — 1</li>
     *   <li>CPF/CNPJ do tomador — 14</li>
     *   <li>Indicador do intermediario — 1, <b>somente se houver</b></li>
     *   <li>CPF/CNPJ do intermediario — 14, somente se houver</li>
     *   <li>ISS retido pelo intermediario — 1, somente se houver</li>
     * </ol>
     *
     * <p>Sem intermediario a cadeia tem <b>86 posicoes</b> (itens 1 a 12), que e
     * o que o manual especifica. Com intermediario, 102.
     *
     * <p>Usar a largura errada na posicao 1 ou o valor errado na posicao 8
     * invalida a assinatura inteira. Foi exatamente o que travou a emissao
     * inicial, com o erro 1206.
     */
    public String montarAssinaturaRps(DadosAssinaturaRps dados) {
        StringBuilder s = new StringBuilder();

        s.append(daEsquerda(dados.imPrestador(), '0', dados.leiaute2() ? 12 : 8));
        s.append(daDireita(dados.serieRps(), ' ', 5));
        s.append(daEsquerda(dados.numeroRps(), '0', 12));
        s.append(dados.dataEmissao().replaceAll("\\D", ""));
        s.append(umCaractere(dados.tributacaoRps()));
        s.append(umCaractere(dados.statusRps()));
        s.append(dados.issRetido() ? 'S' : 'N');

        // posicao 8: o campo muda de nome conforme o leiaute
        String valorPosicao8 = dados.leiaute2() ? dados.valorFinalCobrado() : dados.valorServicos();
        s.append(daEsquerda(semCentavos(valorPosicao8), '0', 15));
        s.append(daEsquerda(semCentavos(dados.valorDeducoes()), '0', 15));
        s.append(daEsquerda(dados.codigoServico(), '0', 5));

        // tomador: indicador 1=CPF, 2=CNPJ, 3=nao informado, 4=NIF
        if (dados.cpfOuCnpjTomador() == null || dados.cpfOuCnpjTomador().isBlank()) {
            s.append('3').append(daEsquerda("", '0', 14));
        } else {
            s.append(dados.tomadorEhCpf() ? '1' : '2');
            s.append(daEsquerda(somenteDigitos(dados.cpfOuCnpjTomador()), '0', 14));
        }

        // Posicoes 13 a 15: intermediario.
        //
        // O manual define a cadeia em 86 posicoes, que sao exatamente as itens 1
        // a 12. As posicoes do intermediario so entram quando existe um — sem
        // intermediario a cadeia para em 86. Acessar sempre as tres produz uma
        // assinatura que a prefeitura recusa.
        if (dados.cpfOuCnpjIntermediario() == null || dados.cpfOuCnpjIntermediario().isBlank()) {
            return s.toString();
        }
        s.append(dados.intermediarioEhCpf() ? '1' : '2');
        s.append(daEsquerda(somenteDigitos(dados.cpfOuCnpjIntermediario()), '0', 14));
        s.append(dados.issRetidoIntermediario() ? 'S' : 'N');

        return s.toString();
    }

    /** Atalho: monta a cadeia e devolve ja assinada em Base64. */
    public String assinarRps(DadosAssinaturaRps dados, PrivateKey chave) {
        return assinarTexto(montarAssinaturaRps(dados), chave);
    }

    // ------------------------------------------------------------------
    // 2) assinatura do XML (XMLDSig)
    // ------------------------------------------------------------------

    /**
     * Assina o XML no padrao XMLDSig, com a assinatura envelopada na raiz.
     *
     * <p>Usa {@code javax.xml.crypto.dsig} (JSR-105), que ja vem no JDK: nao ha
     * BouncyCastle nem outra dependencia envolvida.
     */
    public String assinarXml(String xml, PrivateKey chave, X509Certificate certificado) {
        try {
            Document doc = parse(xml);

            // guarda o que o prefetch ja SignedInfo: o provider precisa do
            // DOM dele, nao de um new SignedInfo() solto.
            Element raiz = doc.getDocumentElement();

            XMLSignatureFactory factory = XMLSignatureFactory.getInstance("DOM");

            SignedInfo signedInfo = factory.newSignedInfo(
                    factory.newCanonicalizationMethod(CanonicalizationMethod.EXCLUSIVE,
                            (C14NMethodParameterSpec) null),
                    factory.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
                    Collections.singletonList(
                            factory.newReference(
                                    "",
                                    factory.newDigestMethod(DigestMethod.SHA1, null),
                                    List.of(
                                            factory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null),
                                            factory.newTransform(ALGO_C14N_EXCLUSIVA, (TransformParameterSpec) null)),
                                    null,
                                    null)));

            KeyInfoFactory kif = factory.getKeyInfoFactory();
            X509Data x509Data = kif.newX509Data(Collections.singletonList(certificado));
            KeyInfo keyInfo = kif.newKeyInfo(Collections.singletonList(x509Data));

            // assina depois de addSignature: o provider popula SignedInfo e
            // assina por referencia.
            // A referencia e "" (documento inteiro), entao nao ha Id para
            // registrar. Chamar setIdAttributeNS aqui estoura com
            // "Id is not an attribute".
            DOMSignContext contexto = new DOMSignContext(chave, raiz);
            contexto.setDefaultNamespacePrefix(null);

            XMLSignature assinatura = factory.newXMLSignature(signedInfo, keyInfo);
            assinatura.sign(contexto);

            return serializar(doc);

        } catch (NfseSpException e) {
            throw e;
        } catch (Exception e) {
            throw new NfseSpException("Falha ao assinar o XML da NFS-e: " + e.getMessage(), e);
        }
    }

    /** Digest SHA1 em Base64. Usado em diagnostico. */
    public String sha1Base64(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            return Base64.getEncoder().encodeToString(md.digest(texto.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new NfseSpException("Falha ao calcular SHA-1: " + e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------
    // internos
    // ------------------------------------------------------------------

    private Document parse(String xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        // o XML vem da propria biblioteca, mas nao custa travar o parser
        f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        DocumentBuilder builder = f.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    private String serializar(Document doc) throws Exception {
        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        t.setOutputProperty(OutputKeys.INDENT, "no");
        StringWriter w = new StringWriter();
        t.transform(new DOMSource(doc), new StreamResult(w));
        return w.toString();
    }

    private String daEsquerda(String valor, char preenchimento, int tamanho) {
        String v = valor == null ? "" : valor;
        if (v.length() >= tamanho) {
            return v.substring(v.length() - tamanho);
        }
        return String.valueOf(preenchimento).repeat(tamanho - v.length()) + v;
    }

    private String daDireita(String valor, char preenchimento, int tamanho) {
        String v = valor == null ? "" : valor;
        if (v.length() >= tamanho) {
            return v.substring(0, tamanho);
        }
        return v + String.valueOf(preenchimento).repeat(tamanho - v.length());
    }

    private String umCaractere(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new NfseSpException(
                    "Tributacao e status do RPS sao obrigatorios na assinatura (1 caractere cada).");
        }
        return valor.substring(0, 1);
    }

    /** "1.234,56" e "1234.56" viram "123456". */
    private String semCentavos(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace(",", "").replace(".", "");
    }

    private String somenteDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }

    /**
     * Dados que entram na cadeia de assinatura do RPS.
     *
     * @param valorServicos      posicao 8 no leiaute 1
     * @param valorFinalCobrado  posicao 8 no leiaute 2
     */
    public record DadosAssinaturaRps(
            String imPrestador,
            String serieRps,
            String numeroRps,
            String dataEmissao,
            String tributacaoRps,
            String statusRps,
            boolean issRetido,
            String valorServicos,
            String valorFinalCobrado,
            String valorDeducoes,
            String codigoServico,
            String cpfOuCnpjTomador,
            boolean tomadorEhCpf,
            String cpfOuCnpjIntermediario,
            boolean intermediarioEhCpf,
            boolean issRetidoIntermediario,
            boolean leiaute2) {

        /** Monta a partir da nota, assumindo tomador e leiaute 1. */
        public static DadosAssinaturaRps deRps(String im, String serie, String numero,
                                                String dataIso, String tributacao, String status,
                                                boolean issRetido, String valorServicos,
                                                String deducoes, String codigoServico,
                                                String cpf, boolean tomadorEhCpf) {
            return new DadosAssinaturaRps(im, serie, numero, dataIso, tributacao, status,
                    issRetido, valorServicos, null, deducoes, codigoServico,
                    cpf, tomadorEhCpf, null, false, false, false);
        }
    }
}
