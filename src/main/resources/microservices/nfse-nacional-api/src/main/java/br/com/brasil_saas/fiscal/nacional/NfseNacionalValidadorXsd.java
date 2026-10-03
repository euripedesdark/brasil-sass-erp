package br.com.brasil_saas.fiscal.nacional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
 * Valida a declaracao contra o XSD do AGILIBlue, localmente.
 *
 * <h2>Por que aqui, e nao so la</h2>
 *
 * <p>A documentacao lista a validacao em tres etapas, nesta ordem:
 *
 * <ol>
 *   <li>a estrutura de forma sintatica</li>
 *   <li>a estrutura do XML baseado em um XSD</li>
 *   <li>a assinatura do prestador</li>
 * </ol>
 *
 * <p>"Cada validacao subsequente depende do sucesso da anterior." Isso significa
 * que um erro de XSD <b>impede</b> a avaliacao da assinatura. Sem validacao
 * local, um erro de tag chega como recusa de assinatura — e quem depura passa
 * a tarde mexendo no certificado, que esta perfeito.
 *
 * <h2>O XSD e' o do dono</h2>
 *
 * <p>{@code src/main/resources/schemas/nfse-v-100.xsd}, copia do arquivo que
 * veio no material da prefeitura. Nao ha um schema de terceiro no projeto
 * para isto, e se houvesse, ele seria de outro padrao: o do AGILIBlue usa
 * {@code ItemLei116} com ponto, CNAE com ponto, e nao tem IBS/CBS no
 * {@code GerarNfseEnvio} da versao 1.00.
 */
@Component
public class NfseNacionalValidadorXsd {

    private static final String SCHEMA = "schemas/nfse-v-100.xsd";

    /**
     * O schema do XMLDSig, com o nome que a PREFEITURA usa.
     *
     * <p>Nao e' o {@code xmldsig-core-schema.xsd} que circula no Maven e no
     * {@code nfse-client} de terceiro: a prefeitura distribui o mesmo schema da
     * W3C (revisao 1.7, baixado de w3.org/TR/xmldsig-core em 23/02/2007) com o
     * nome {@code webapi-nfse-signature.xsd}. Os dois sao estruturalmente
     * identicos — 46 elementos, 22 complexTypes, 3 simpleTypes, 20 atributos,
     * mesmo targetNamespace — mas o nome e' o que vai no {@code schemaLocation} do
     * import, e o nome que a prefeitura espera e' o dela.
     */
    private static final String SCHEMA_DSIG = "schemas/webapi-nfse-signature.xsd";
    private static final String NS_DSIG = "http://www.w3.org/2000/09/xmldsig#";

    private static final String NOME_XSD_AGILI = "nfse-v-100.xsd";
    private static final String NOME_XSD_DSIG = "webapi-nfse-signature.xsd";

    /**
     * O schema e' caro de montar e imutavel, entao entra uma vez.
     *
     * <p>Sem o duplo teste com {@code volatile}, duas requisicoes simultaneas
     * montam o schema duas vezes e uma delas pode ver o objeto pela metade. A
     * falha seria intermitente e so sob concorrencia.
     */
    private static volatile Schema schema;

    /**
     * Valida.
     *
     * @return os erros com linha e coluna; lista vazia significa que passou
     */
    public List<String> validar(String xml) {
        List<String> erros = new ArrayList<>();
        if (xml == null || xml.isBlank()) {
            return List.of("XML vazio");
        }
        try {
            Validator v = schema().newValidator();
            // o default interrompe na primeira falha; a lista inteira orienta mais
            v.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException e) {
                    // aviso de schema nao impede o envio
                }

                @Override
                public void error(SAXParseException e) {
                    erros.add(descrever(e));
                }

                @Override
                public void fatalError(SAXParseException e) {
                    erros.add(descrever(e));
                }
            });
            v.validate(new StreamSource(
                    new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))));
        } catch (NfseNacionalException e) {
            throw e;
        } catch (Exception e) {
            // Falha do validador em si e' diferente de declaracao invalida.
            // Confundir as duas faz a API recusar uma declaracao boa por
            // problema de ambiente — e o usuario nao tem como corrigir isso.
            throw new NfseNacionalException(
                    "Nao foi possivel validar contra o XSD: " + e.getMessage(), e);
        }
        return erros;
    }

    private static String descrever(SAXParseException e) {
        return "linha " + e.getLineNumber() + ", coluna " + e.getColumnNumber()
                + ": " + e.getMessage();
    }

    private static Schema schema() {
        Schema s = schema;
        if (s == null) {
            synchronized (NfseNacionalValidadorXsd.class) {
                s = schema;
                if (s == null) {
                    s = carregar();
                    schema = s;
                }
            }
        }
        return s;
    }

    private static Schema carregar() {
        Path dir = null;
        try {
            dir = prepararSchemas();
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            trySet(factory, XMLConstants.ACCESS_EXTERNAL_DTD, "");
            trySet(factory, XMLConstants.ACCESS_EXTERNAL_SCHEMA, "file");
            return factory.newSchema(dir.resolve(NOME_XSD_AGILI).toFile());

        } catch (NfseNacionalException e) {
            throw e;
        } catch (Exception e) {
            throw new NfseNacionalException(
                    "Falha ao carregar o XSD do AGILIBlue: " + e.getMessage(), e);
        }
    }

    /**
     * Monta um diretorio temporario com os dois schemas, ja com o import do
     * XMLDSig no XSD do AGILIBlue.
     *
     * <p><b>Defeito no XSD distribuido pela prefeitura.</b> O
     * {@code nfse-v-100.xsd} referencia {@code dsig:Signature} na linha 574,
     * dentro de {@code tcIdentificacaoPrestador}:
     *
     * <pre>
     *   &lt;xsd:choice&gt;
     *     &lt;xsd:element name="ChaveDigital" type="tsChaveDigital"/&gt;
     *     &lt;xsd:element ref="dsig:Signature"/&gt;
     *   &lt;/xsd:choice&gt;
     * </pre>
     *
     * <p>e declara {@code xmlns:dsig} no elemento raiz — mas <b>nao tem
     * {@code xsd:import}</b> para aquele namespace. Como esta, o arquivo nao
     * carrega num validador JAXP:
     * {@code src-resolve.4.2: Error resolving component 'dsig:Signature'}.
     *
     * <p>A correcao no arquivo da prefeitura seria uma linha, e o
     * {@code schemaLocation} e' o nome que ela distribui ao lado:
     *
     * <pre>
     *   &lt;xsd:import namespace="http://www.w3.org/2000/09/xmldsig#"
     *               schemaLocation="webapi-nfse-signature.xsd"/&gt;
     * </pre>
     *
     * <p>As duas coisas vao juntas no mesmo lote: o {@code nfse-v-100.xsd}
     * referencia {@code dsig:Signature} e declara {@code xmlns:dsig}, e o
     * {@code webapi-nfse-signature.xsd} e' o schema daquele namespace. So que
     * nenhuma das duas declara a ligacao — o import — e sem ele o par nao
     * carrega.
     *
     * <p><b>Por que nao editei o arquivo do dono.</b> Ele esta sob controle de
     * versao e e' a fonte da verdade do que a prefeitura espera. Alterar schema
     * de terceiro no lugar sem querer faz o comportamento divergir sem ninguem
     * perceber. A correcao e' aplicada numa copia, em disco temporario, e o
     * arquivo do dono continua byte a byte igual. O dia que a prefeitura
     * mandar o XSD com o import, este metodo detecta e nao insere nada.
     *
     * <p>Por que arquivo em disco, e nao uma alteracao no DOM: o
     * {@code schemaLocation} do import e' relativo ao documento que o contem, e
     * resolver isso dentro de um jar empacotado e' justamente onde a resolucao
     * por classpath falha. Em disco, com os dois arquivos lado a lado, o
     * caminho e' o mesmo que qualquer ferramenta de XML esperaria.
     */
    private static Path prepararSchemas() throws Exception {
        Path dir = Files.createTempDirectory("nfse-agilblue-xsd");
        dir.toFile().deleteOnExit();

        String agili = new String(bytesDoClasspath(SCHEMA), StandardCharsets.UTF_8);
        Files.writeString(dir.resolve(NOME_XSD_AGILI), inserirImportDoDsig(agili),
                StandardCharsets.UTF_8);
        Files.writeString(dir.resolve(NOME_XSD_DSIG),
                new String(bytesDoClasspath(SCHEMA_DSIG), StandardCharsets.UTF_8),
                StandardCharsets.UTF_8);

        return dir;
    }

    /**
     * Insere o {@code xsd:import} do XMLDSig depois da tag de abertura do schema.
     *
     * <p><b>Por que sem o atributo {@code prefix}.</b> Ele existe em XSD 1.1 e
     * nao em 1.0, que e' o que o AGILIBlue usa. Com ele, o validador responde
     * {@code s4s-att-not-allowed: Attribute 'prefix' cannot appear in element
     * 'import'}.
     *
     * <p><b>Por que a declaracao {@code xmlns:dsig} do raiz fica.</b> E' ela
     * que amarra o prefixo {@code dsig} ao namespace, e sem ela o
     * {@code ref="dsig:Signature"} da linha 574 nao resolve. O import entra sem
     * prefixo e o {@code schemaLocation} relativo resolve por causa dos dois
     * arquivos estarem lado a lado em disco.
     *
     * @return o texto pronto; sem mudanca se o import ja estiver la
     */
    static String inserirImportDoDsig(String xsd) {
        if (xsd.contains("schemaLocation=\"" + NOME_XSD_DSIG + "\"")) {
            return xsd; // ja declara o import; nao mexe
        }

        String imp = "<xsd:import namespace=\"" + NS_DSIG + "\""
                + " schemaLocation=\"" + NOME_XSD_DSIG + "\"/>";

        java.util.regex.Matcher m =
                java.util.regex.Pattern.compile("<xsd:schema\\b", Pattern.DOTALL)
                        .matcher(xsd);
        if (!m.find()) {
            throw new NfseNacionalException(
                    "Nao encontrei a tag <xsd:schema> no XSD do AGILIBlue; o arquivo pode "
                            + "estar em outro formato.");
        }
        int fim = xsd.indexOf('>', m.start());
        if (fim < 0) {
            throw new NfseNacionalException(
                    "Tag <xsd:schema> do XSD do AGILIBlue esta sem fechamento.");
        }

        // A tag de abertura so e' auto-fechada se o caractere antes do '>' for
        // '/'. Procurar a ultima barra e' erro: dentro do proprio XSD, o
        // namespace da W3C e' 'http://www.w3.org/2001/XMLSchema', e a ultima
        // barra antes do '>' cai no meio de um valor de atributo — o import
        // entra dentro da string, e o validador responde que o xmlns "nao pode
        // conter o caractere <".
        boolean autoFechada = fim > 0 && xsd.charAt(fim - 1) == '/';
        int pos = autoFechada ? fim : fim + 1;
        return xsd.substring(0, pos) + "\n    " + imp + "\n  " + xsd.substring(pos);
    }

    private static byte[] bytesDoClasspath(String caminho) throws Exception {
        ClassPathResource recurso = new ClassPathResource(caminho);
        if (!recurso.exists()) {
            throw new NfseNacionalException("Schema ausente no classpath: " + caminho);
        }
        try (InputStream in = recurso.getInputStream()) {
            return in.readAllBytes();
        }
    }


    private static void trySet(SchemaFactory f, String chave, String valor) {
        try {
            f.setProperty(chave, valor);
        } catch (Exception e) {
            // ignorado de proposito
        }
    }
}
