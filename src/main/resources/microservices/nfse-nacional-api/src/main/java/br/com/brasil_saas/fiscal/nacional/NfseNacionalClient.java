package br.com.brasil_saas.fiscal.nacional;

import java.io.StringWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.time.Duration;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

/**
 * Falar com o WebService do AGILIBlue.
 *
 * <p><b>Este cliente nao e' SOAP, e nao e' JSON.</b> A documentacao tecnica e
 * explicita: o cliente "envia para uma URL previamente informada um arquivo XML
 * com sua solicitacao". O corpo da requisicao e' o XML puro, com
 * {@code Content-Type: text/xml}.
 *
 * <p>E' o mesmo transporte que a API de Sao Paulo usa — e por isso o codigo de
 * {@code NfseSpClient} serve de base. O que muda e' o conteudo do corpo, o
 * destino, e a resposta, que aqui volta em XML com
 * {@code GerarNfseResposta}.
 *
 * <h2>A validacao acontece em tres etapas, nesta ordem</h2>
 *
 * <blockquote>
 * "O WebService, ao receber esses dados, validara a estrutura conforme a ordem a
 * seguir, sendo que a cada validacao subsequente depende do sucesso da anterior:
 * a estrutura de forma sintatica; a estrutura do XML baseado em um XSD; a
 * assinatura do prestador do servico que autoriza a operacao."
 * </blockquote>
 *
 * <p><b>A ordem importa e vale registrar:</b> uma assinatura sobre um XML que
 * nao passa no XSD vai ser rejeitada com mensagem de sintaxe, e quem depura vai
 * culpar a assinatura. Por isso {@link NfseNacionalValidadorXsd} roda
 * <b>antes</b> de {@link NfseNacionalSigner}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NfseNacionalClient {

    private final NfseNacionalProperties props;
    private final NfseNacionalCertificadoService certificadoService;

    /** Resposta do AGILIBlue, ja desserializada. */
    public record Resposta(int status, String corpo, String url) {

        public boolean ok() {
            return status >= 200 && status < 300;
        }
    }

    // ------------------------------------------------------------------
    // as oito operacoes
    // ------------------------------------------------------------------

    public Resposta gerarNfse(String declaracao) {
        return post(props.operacoes().urlGerarNfse(), declaracao);
    }

    public Resposta enviarLoteRps(String lote) {
        return post(props.operacoes().urlEnviarLoteRps(), lote);
    }

    public Resposta cancelarNfse(String pedido) {
        return post(props.operacoes().urlCancelarNfse(), pedido);
    }

    public Resposta substituirNfse(String pedido) {
        return post(props.operacoes().urlSubstituirNfse(), pedido);
    }

    public Resposta consultarLoteRps(String pedido) {
        return post(props.operacoes().urlConsultarLoteRps(), pedido);
    }

    public Resposta consultarNfseRps(String pedido) {
        return post(props.operacoes().urlConsultarNfseRps(), pedido);
    }

    public Resposta consultarNfseFaixa(String pedido) {
        return post(props.operacoes().urlConsultarNfseFaixa(), pedido);
    }

    public Resposta consultarRequerimentoCancelamento(String pedido) {
        return post(props.operacoes().urlConsultarRequerimentoCancelamento(), pedido);
    }

    // ------------------------------------------------------------------
    // transporte
    // ------------------------------------------------------------------

    private Resposta post(String url, String xml) {
        try {
            log.info("POST {} ({} bytes)", url, xml == null ? 0 : xml.length());
            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofMillis(props.getTimeoutoMs()))
                    // <b>Content-Type: application/xml. Nao text/xml.</b>
                    //
                    // <p>Com text/xml o AGILIBlue responde HTTP 500 e 53 bytes:
                    //
                    // <pre>
                    //   ALERTA: XML invalida ou nao informada corretamente.
                    // </pre>
                    //
                    // <p>Com application/xml responde HTTP 202 com a
                    // <code>GerarNfseResposta</code> completa, lista de erros de
                    // negocio inclusa. <b>E' a mesma diferenca entre um erro de
                    // transporte e uma resposta de verdade</b> — e o ALERTA nao
                    // diz que o problema e' o content-type, entao quem depura
                    // procurar erro no XML.
                    //
                    // <p>Os outros tipos que respondem 500 sao
                    // application/soap+xml e application/octet-stream, com
                    // "ERRO: O sistema identificou uma inconsistencia".
                    .header("Content-Type", "application/xml; charset=utf-8")
                    .header("Accept", "application/xml")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            xml == null ? "" : xml, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> resp = cliente().send(
                    req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            Resposta r = new Resposta(resp.statusCode(), resp.body(), url);
            if (!r.ok()) {
                log.warn("AGILIBlue respondeu {} em {}: {}", r.status(), url, resumir(r.corpo()));
            }
            return r;

        } catch (NfseNacionalException e) {
            throw e;
        } catch (InterruptedException e) {
            // Sem restore: quem chamou esta metodo em loop e precisa do cancelamento
            Thread.currentThread().interrupt();
            throw new NfseNacionalException("Chamada ao AGILIBlue interrompida: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new NfseNacionalException(
                    "Falha ao chamar o AGILIBlue em " + url + ": " + e.getMessage(), e);
        }
    }

    /**
     * O cliente HTTP, com o A1 para quando a autenticacao for por assinatura.
     *
     * <p><b>Nao ha usuario e senha.</b> A autenticacao do AGILIBlue e' a
     * {@code ChaveDigital} dentro do XML ou o {@code dsig:Signature}. Se for
     * assinatura, o certificado entra no handshake TLS; se for chave digital, o
     * certificado nao participa e o transporte e' um POST comum.
     *
     * <p>Por isso o SSL so e' montado quando ha certificado configurado. Um
     * cliente com mTLS que o municipal nao exige e' um handshake que pode ser
     * recusado do lado de la, e a falha aparece como erro de conexao, sem
     * relacao com a nota.
     */
    private HttpClient cliente() throws Exception {
        HttpClient.Builder b = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(props.getTimeoutoMs()))
                .version(HttpClient.Version.HTTP_1_1);

        if (props.getCertificadoCaminho() != null && !props.getCertificadoCaminho().isBlank()) {
            b.sslContext(sslContext(certificadoService.carregar()));
        }
        return b.build();
    }

    private SSLContext sslContext(NfseNacionalCertificadoService.ParDeAssinatura par)
            throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null, null);
        ks.setKeyEntry(par.alias(), par.chavePrivada(), new char[0],
                new java.security.cert.Certificate[]{par.certificado()});

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, new char[0]);

        // Confia na cadeia publica do sistema. A cadeia do AGILIBlue e' ICP-Brasil e
        // valida normalmente; "confia em tudo" resolveria o sintoma e entregaria
        // a declaracao assinada a um host que nao e' a prefeitura.
        TrustManagerFactory tmf =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init((KeyStore) null);

        SSLContext ctx = SSLContext.getInstance("TLSv1.2");
        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
        return ctx;
    }

    // ------------------------------------------------------------------
    // XML
    // ------------------------------------------------------------------

    /**
     * Le um XML do AGILIBlue.
     *
     * <p>O parser e' endurecido contra XXE porque o corpo vem da rede: sem
     * {@code disallow-doctype-decl} e sem bloquear entidades externas, um
     * servidor comprometido — ou uma resposta interceptada — le qualquer arquivo
     * da maquina.
     */
    public static Document parse(String xml) {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(true);
            f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            f.setFeature("http://xml.org/sax/features/external-general-entities", false);
            f.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            f.setXIncludeAware(false);
            f.setExpandEntityReferences(false);
            return f.newDocumentBuilder().parse(
                    new java.io.ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new NfseNacionalException("XML invalido do AGILIBlue: " + e.getMessage(), e);
        }
    }

    /** Serializa sem declaracao e sem indentacao, que e' como o AGILIBlue espera. */
    public static String serializar(Document doc) {
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.setOutputProperty(OutputKeys.INDENT, "no");
            t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            StringWriter w = new StringWriter();
            t.transform(new DOMSource(doc), new StreamResult(w));
            return w.toString();
        } catch (Exception e) {
            throw new NfseNacionalException("Falha ao serializar o XML: " + e.getMessage(), e);
        }
    }

    private static String resumir(String texto) {
        if (texto == null) {
            return "(vazio)";
        }
        String t = texto.strip();
        return t.length() <= 400 ? t : t.substring(0, 400) + "...";
    }
}
