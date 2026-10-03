package br.com.brasil_saas.fiscal.nacional;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

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
import java.security.MessageDigest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A assinatura do prestador, na forma que o AGILIBlue aceita.
 *
 * <h2>Este e' o reaproveitamento que o projeto tem de verdade</h2>
 *
 * <p>Na documentacao do AGILIBlue, a terceira e ultima etapa de validacao e' "a
 * assinatura do prestador do servico que autoriza a operacao". E o XSD do
 * material traz, dentro de {@code tcIdentificacaoPrestador}:
 *
 * <pre>
 *   &lt;xsd:choice&gt;
 *     &lt;xsd:element name="ChaveDigital" type="tsChaveDigital"/&gt;
 *     &lt;xsd:element ref="dsig:Signature"/&gt;
 *   &lt;/xsd:choice&gt;
 * </pre>
 *
 * <p>{@code dsig} e' {@code http://www.w3.org/2000/09/xmldsig#} — <b>XMLDSig
 * padrao, a mesma coisa que a API de Sao Paulo ja produz com o A1</b>. O
 * codigo usado aqui e' o JSR-105 do proprio JDK
 * ({@code javax.xml.crypto.dsig}), sem BouncyCastle e sem biblioteca de
 * terceiros. E' por isso que a assinatura da prefeitura de Sao Paulo serve
 * para a de Rondonopolis sem trocar de algoritmo.
 *
 * <h2>A diferenca que existe, e e' de posicao</h2>
 *
 * <p>A API de Sao Paulo assina o envelope SOAP inteiro, com a
 * {@code Signature} na raiz. Aqui a {@code Signature} vai
 * <b>dentro de {@code IdentificacaoPrestador}</b>, como primeiro filho, porque
 * e' ali que o {@code xsd:choice} a declara. Assinar e colocar no lugar certo
 * sao coisas separadas: a assinatura pode estar correta e o documento ser
 * recusado so porque a {@code Signature} veio depois de {@code CpfCnpj}.
 *
 * <h2>Quando o certificado nao e' usado</h2>
 *
 * <p>Se a prefeitura fornecer {@code ChaveDigital}, o {@code choice} e'
 * satisfeito por ela e <b>esta classe nao roda</b>. Mandar as duas e' o erro
 * mais provavel aqui, porque as duas autenticam e parece que "mais e' melhor".
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NfseNacionalSigner {

    private static final String NS_DSIG = "http://www.w3.org/2000/09/xmldsig#";
    private static final String ALGO_C14N_EXCLUSIVA = "http://www.w3.org/2001/10/xml-exc-c14n#";
    private static final String ALGO_C14N_INCLUSIVA =
            "http://www.w3.org/TR/2001/REC-xml-c14n-20010315";

    private final NfseNacionalCertificadoService certificadoService;

    /**
     * Assina a declaracao, colocando a {@code Signature} onde o XSD declara.
     *
     * @param xml a declaracao ainda sem assinatura
     * @return o XML com {@code <ds:Signature>} dentro de
     *         {@code IdentificacaoPrestador}
     */
    public String assinarDeclaracao(String xml, PrivateKey chave, X509Certificate certificado) {
        try {
            Document doc = parse(xml);

            NodeList prestadores = doc.getElementsByTagNameNS("*", "IdentificacaoPrestador");
            if (prestadores.getLength() != 1) {
                throw new NfseNacionalException("IdentificacaoPrestador nao encontrado ou "
                        + "duplicado: " + prestadores.getLength() + " ocorrencia(s).");
            }
            Element prest = (Element) prestadores.item(0);

            if (doc.getElementsByTagNameNS("*", "ChaveDigital").getLength() > 0) {
                // O xsd:choice e' satisfeito pela ChaveDigital. Assinar tambem
                // viola o choice e a prefeitura recusa — e a recusa vem como
                // erro de XSD, que nao aponta para a causa.
                throw new NfseNacionalException(
                        "A declaracao ja tem ChaveDigital, e o XSD aceita uma ou outra, nao as "
                                + "duas. Ou configure nfse.nacional.chave-digital, ou "
                                + "remova-a para a API assinar com o certificado.");
            }

            XMLSignatureFactory factory = XMLSignatureFactory.getInstance("DOM");

            // <b>A referencia e' o documento inteiro, e nao um #Id.</b> O XSD do
            // AGILIBlue nao permite atributo em IdentificacaoPrestador, entao nao
            // existe id para apontar. A forma correta aqui e' URI="" com o
            // transform ENVELOPED: o digest cobre o documento todo, e a propria
            // Signature e' excluida do calculo.
            //
            // <b>Esta e' a mesma referencia que a API de Sao Paulo usa</b>, e e'
            // por isso que o codigo daqui e' o de la sem mudanca de algoritmo. O
            // que muda entre as duas prefeituras e' apenas ONDE a Signature e'
            // depositada: na raiz do envelope SOAP, e dentro de
            // IdentificacaoPrestador aqui.
            Reference referencia = factory.newReference(
                    "",
                    factory.newDigestMethod(DigestMethod.SHA256, null),
                    List.of(
                            factory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null),
                            factory.newTransform(ALGO_C14N_EXCLUSIVA, (TransformParameterSpec) null)),
                    null, null);

            SignedInfo signedInfo = factory.newSignedInfo(
                    factory.newCanonicalizationMethod(ALGO_C14N_EXCLUSIVA,
                            (C14NMethodParameterSpec) null),
                    factory.newSignatureMethod(SignatureMethod.RSA_SHA256, null),
                    List.of(referencia));

            KeyInfoFactory kif = factory.getKeyInfoFactory();
            KeyInfo keyInfo = kif.newKeyInfo(
                    List.of(kif.newX509Data(List.of(certificado))));

            // O contexto aponta para IdentificacaoPrestador. E' isso que coloca
            // a Signature dentro dele, e nao na raiz.
            DOMSignContext contexto = new DOMSignContext(chave, prest);
            contexto.setDefaultNamespacePrefix("ds");
            contexto.putNamespacePrefix(NS_DSIG, "ds");

            // <b>A Signature tem que entrar como PRIMEIRO filho.</b> Sem o
            // setNextSibling, o provider a Acrescenta no fim do elemento, e o
            // resultado e':
            //
            //   <IdentificacaoPrestador><CpfCnpj>...</CpfCnpj>
            //     <ds:Signature>...</ds:Signature></IdentificacaoPrestador>
            //
            // enquanto o xsd:sequence do AGILIBlue e':
            //
            //   sequence( choice(ChaveDigital | dsig:Signature), CpfCnpj, InscricaoMunicipal? )
            //
            // A recusa do XSD e' "Invalid content was found starting with element
            // 'CpfCnpj'" — que aponta para a tag seguinte e nao para a causa, que
            // e' a assinatura estar duas posicoes atras.
            if (prest.getFirstChild() != null) {
                contexto.setNextSibling(prest.getFirstChild());
            }

            factory.newXMLSignature(signedInfo, keyInfo).sign(contexto);

            return serializar(doc);

        } catch (NfseNacionalException e) {
            throw e;
        } catch (Exception e) {
            throw new NfseNacionalException(
                    "Falha ao assinar a declaracao: " + e.getMessage(), e);
        }
    }

    /**
     * Assina usando o certificado configurado.
     *
     * @return o XML assinado, ou o mesmo XML se {@code assinar} estiver desligado
     */
    public String assinarSePrecisar(String xml) {
        NfseNacionalProperties props = certificadoService.properties();
        if (!props.isAssinar()) {
            log.warn("nfse.nacional.assinar=false: a declaracao vai sem assinatura");
            return xml;
        }
        NfseNacionalCertificadoService.ParDeAssinatura par = certificadoService.carregar();
        return assinarDeclaracao(xml, par.chavePrivada(), par.certificado());
    }

    /**
     * Assina o texto, em SHA-1 com RSA.
     *
     * <p>Existe porque a Prefeitura de Sao Paulo exige uma cadeia de 86
     * posicoes assim. <b>O AGILIBlue nao usa isso</b> — a assinatura dele e' o
     * {@code dsig:Signature} de {@link #assinarDeclaracao}. O metodo fica para
     * o caso de a prefeitura passar a exigir, e para ficar escrito que a
     * diferenca existe.
     */
    public String assinarTexto(String texto, PrivateKey chave) {
        try {
            Signature signature = Signature.getInstance("SHA1withRSA");
            signature.initSign(chave);
            signature.update(texto.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (Exception e) {
            throw new NfseNacionalException("Falha ao assinar o texto: " + e.getMessage(), e);
        }
    }

    /** Digest SHA-1 em Base64. Usado em diagnostico. */
    public String sha1Base64(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            return Base64.getEncoder().encodeToString(
                    md.digest(texto.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new NfseNacionalException("Falha ao calcular SHA-1: " + e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------

    private static Document parse(String xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        // o XML e' nosso, mas nao custa travar o parser
        f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        DocumentBuilder builder = f.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    private static String serializar(Document doc) throws Exception {
        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        t.setOutputProperty(OutputKeys.INDENT, "no");
        t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        StringWriter w = new StringWriter();
        t.transform(new DOMSource(doc), new StreamResult(w));
        return w.toString();
    }
}
