package br.com.brasil_saas.fiscal.sp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.time.Duration;

/**
 * Fala com o WebService da Prefeitura de Sao Paulo.
 *
 * <p>E so um POST SOAP. Nao ha necessidade de WSDL nem de gerador de stub: o
 * WSDL publicado responde 403 e a mensagem e montada a mao porque o schema tem
 * poucas mudancas de versao em versao.
 *
 * <p><b>A prefeitura exige mTLS.</b> O certificado A1 e apresentado no
 * handshake TLS, alem de assinar a mensagem — e por isso que um cliente HTTP
 * comum, com o certificado so dentro do XML, leva 403 do WAF. Sem o
 * {@code SSLContext} com a chave, a resposta e
 * {@code 403 - Forbidden: Access is denied} e nao um erro de schema.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NfseSpClient {

    private final NfseSpProperties properties;
    private final NfseSpCertificadoService certificadoService;

    private volatile HttpClient http;
    private volatile String certificadoEmUso;

    /**
     * Envia a mensagem e devolve o XML de retorno cru.
     *
     * @throws NfseSpException se a conexao falhar ou a prefeitura responder
     *                         algo que nao seja XML
     */
    public String enviar(String soapEnvelope) {
        return enviar(soapEnvelope, (String) null);
    }

    /**
     * @param soapAction o nome da operacao, usado no cabecalho {@code SOAPAction}.
     *                   A prefeitura responde
     *                   {@code "Unable to handle request without a valid action
     *                   parameter"} sem ele. Em SOAP 1.2 o valor vai entre
     *                   aspas.
     */
    public String enviarPara(String soapEnvelope, NfseSpOperacoes.Operacao operacao) {
        return enviar(soapEnvelope, operacao == null ? null : operacao.cabecalhoSoapAction());
    }

    public String enviar(String soapEnvelope, String soapAction) {
        var par = certificadoService.carregar();
        HttpClient client = cliente(par);

        HttpRequest.Builder pedido = HttpRequest.newBuilder()
                .uri(URI.create(properties.getUrl()))
                .timeout(Duration.ofSeconds(properties.getTimeoutLeituraSegundos()))
                // SOAP 1.2. Nao por causa do namespace, mas porque o
                // Content-Type com o parametro `action` (que e o padrao
                // SOAP 1.2) responde 415: a prefeitura quer a acao no
                // cabecalho SOAPAction, como o WSDL declara.
                .header("Content-Type", "application/soap+xml;charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(soapEnvelope, StandardCharsets.UTF_8));

        HttpRequest.Builder comAction = pedido;
        if (soapAction != null && !soapAction.isBlank()) {
            comAction = pedido.header("SOAPAction", "\"" + soapAction + "\"");
        }
        HttpRequest request = comAction.build();

        try {
            HttpResponse<String> resposta = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            String corpo = resposta.body();

            if (resposta.statusCode() == 403) {
                throw new NfseSpException(
                        "A prefeitura recusou a conexao (403). Quase sempre e o certificado A1: "
                                + "ela exige mTLS, ou seja, o certificado tem de ser apresentado no "
                                + "handshake TLS, alem de assinar a mensagem. Se o certificado mudou, "
                                + "confira brasil-saas.fiscal.nfse-sp.certificado-caminho.");
            }
            if (resposta.statusCode() >= 400) {
                throw new NfseSpException(
                        "A prefeitura respondeu HTTP " + resposta.statusCode() + ": " + motivoDoFault(corpo));
            }
            if (corpo == null || corpo.isBlank()) {
                throw new NfseSpException("A prefeitura respondeu vazio.");
            }
            return corpo;

        } catch (NfseSpException e) {
            throw e;
        } catch (java.net.http.HttpTimeoutException e) {
            throw new NfseSpException(
                    "A prefeitura nao respondeu em " + properties.getTimeoutLeituraSegundos()
                            + "s. Ela costuma levar 1s, mas em horario de pico demora. "
                            + "Nao reenvie sem conferir se a nota foi emitida.", e);
        } catch (Exception e) {
            throw new NfseSpException("Falha de rede ao falar com a prefeitura: " + e.getMessage(), e);
        }
    }

    /**
     * HTTP/1.1 com o certificado do cliente.
     *
     * <p>Recria o {@link HttpClient} quando o certificado muda, porque o
     * {@code SSLContext} e preso ao cliente.
     */
    private HttpClient cliente(NfseSpCertificadoService.ParDeAssinatura par) {
        String assinaturaDoCertificado = certificadoEmUso;
        HttpClient atual = http;

        if (atual != null && par != null && assinaturaDoCertificado.equals(assinaturaDoCertificado)) {
            return atual;
        }
        synchronized (this) {
            if (http != null && par != null && certificadoEmUso.equals(assinaturaDoCertificado)) {
                return http;
            }
            try {
                SSLContext sslContext = sslContext(par);
                http = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(properties.getTimeoutConexaoSegundos()))
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        // A prefeitura so aceita HTTP/1.1. Com HTTP/2 (padrao
                        // do java.net.http) a conexao morre com "Received
                        // RST_STREAM: Use HTTP/1.1 for request".
                        .version(HttpClient.Version.HTTP_1_1)
                        .sslContext(sslContext)
                        .build();
                certificadoEmUso = par == null ? null : certificadoService.certificadoBase64(par.certificado());
                return http;
            } catch (NfseSpException e) {
                throw e;
            } catch (Exception e) {
                throw new NfseSpException(
                        "Nao foi possivel montar o contexto TLS com o certificado A1: " + e.getMessage(), e);
            }
        }
    }

    /** {@code SSLContext} com a chave e o certificado do A1, para o mTLS. */
    private SSLContext sslContext(NfseSpCertificadoService.ParDeAssinatura par) throws Exception {
        if (par == null) {
            return SSLContext.getDefault();
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keystoreCom(par), properties.getCertificadoSenha().toCharArray());

        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(kmf.getKeyManagers(), null, null);
        return ctx;
    }

    /** Recarrega o PKCS#12, porque o {@code SSLContext} exige o keystore inteiro. */
    private KeyStore keystoreCom(NfseSpCertificadoService.ParDeAssinatura par) throws Exception {
        String caminho = properties.getCertificadoCaminho();
        String senha = properties.getCertificadoSenha();
        if (caminho == null || caminho.isBlank()) {
            throw new NfseSpException("Configure o certificado A1 para usar mTLS com a prefeitura.");
        }
        KeyStore ks = KeyStore.getInstance("PKCS12");
        try (InputStream in = Files.newInputStream(Paths.get(caminho))) {
            ks.load(in, senha.toCharArray());
        }
        return ks;
    }

    /**
     * Extrai o motivo de um SOAP Fault.
     *
     * <p>O corpo do fault carrega a causa real, mas vem escondido depois de
     * centenas de caracteres de namespace. Cortar em 300 corta justamente o que
     * explica a falha.
     */
    private String motivoDoFault(String corpo) {
        if (corpo == null || corpo.isBlank()) {
            return "(corpo vazio)";
        }
        // o prefixo varia (soap:, ns0:...), entao casa pelo nome local
        for (String tag : new String[]{"Reason", "faultstring", "faultcode"}) {
            java.util.regex.Matcher m = java.util.regex.Pattern
                    .compile("<(?:[A-Za-z0-9_.-]+:)?" + tag + "(\\s[^>]*)?>(.*?)</(?:[A-Za-z0-9_.-]+:)?" + tag + ">",
                            java.util.regex.Pattern.DOTALL)
                    .matcher(corpo);
            if (m.find()) {
                String texto = m.group(2).replaceAll("\\s+", " ").trim();
                if (!texto.isEmpty()) {
                    return texto;
                }
            }
        }
        return resumir(corpo);
    }

    private String resumir(String texto) {
        if (texto == null) {
            return "";
        }
        String limpo = texto.replaceAll("\\s+", " ").trim();
        return limpo.length() > 300 ? limpo.substring(0, 300) + "..." : limpo;
    }
}
