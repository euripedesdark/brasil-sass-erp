package br.com.brasil_saas.fiscal.sp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Le o XML de retorno da Prefeitura de Sao Paulo.
 *
 * <p>A prefeitura nomeia o no de retorno com <b>CAIXA ALTA</b> e com o
 * sufixo da operacao — {@code <RetornoConsultaCNPJ>}, {@code <RetornoEnvioRPS>},
 * {@code <RetornoCancelamentoNFe>} — e nao com o nome do metodo. Buscar
 * case-sensitivamente por {@code retorno_consulta} simplesmente nao acha nada.
 *
 * <p>Alem disso a resposta vem aninhada no SOAP:
 * {@code Body > operacao_response > retorno_xml}, e o
 * {@code retorno_xml} pode ser um elemento ou uma string CDATA.
 */
@Slf4j
@Component
public class NfseSpRespostaParser {

    /** Resposta de uma operacao. */
    public record Resposta(boolean sucesso, String descricao, List<Mensagem> alertas,
                           List<Mensagem> erros, ChaveNfe chaveNfe) {
    }

    /** Erro ou alerta, com o codigo da prefeitura. */
    public record Mensagem(String codigo, String descricao) {
        @Override
        public String toString() {
            return codigo == null ? descricao : "[" + codigo + "] " + descricao;
        }
    }

    /** Dados que identificam a NFS-e emitida. */
    public record ChaveNfe(String inscricaoPrestador, String numeroNfe,
                           String codigoVerificacao, String chaveNotaNacional) {
    }

    /**
     * Extrai o {@code retorno_xml} do envelope SOAP e interpreta.
     *
     * @param nomeOperacao o nome do no da resposta, ex.: {@code consulta_cnpj_response}
     */
    public Resposta parse(String soapResponse, String nomeOperacao) {
        Element soap = parseXml(soapResponse, "SOAP").getDocumentElement();

        // O WSDL devolve <RetornoXML> com o X MAIUSCULO, e o XML real vem
        // DENTRO dele como texto escapado, nao como elementos filhos. Buscar
        // <Erro> direto no SOAP nao acha nada.
        Element container = primeiroNo(soap, "RetornoXML", "retorno_xml");
        if (container == null) {
            container = elementoComNomeContendo(soap, "retorno");
        }

        Element retorno = elementoDeRetorno(container, nomeOperacao);
        if (retorno == null) {
            String texto = textoDe(soap, nomeOperacao);
            retorno = elementoDeRetornoTexto(texto, nomeOperacao);
        }
        if (retorno == null) {
            throw new NfseSpException(
                    "Nao consegui localizar o retorno da prefeitura no XML. "
                            + "Esperava <RetornoXML> com o XML da resposta. Recebido: "
                            + resumir(soapResponse));
        }

        boolean sucesso = "true".equalsIgnoreCase(textoDe(retorno, "Sucesso"))
                || "1".equals(textoDe(retorno, "Sucesso"));

        return new Resposta(
                sucesso,
                textoDe(retorno, "Sucesso"),
                coletar(retorno, "Alerta"),
                coletar(retorno, "Erro"),
                chaveNfe(retorno));
    }

    /**
     * Extrai o XML de retorno de dentro de {@code <RetornoXML>}.
     *
     * <p>O texto pode chegar como elemento (quando o servidor deserializa) ou
     * como CDATA/string escapada (que e o caso normal). Nos dois casos o
     * conteudo e o XML da prefeitura e precisa ser parseado de novo.
     */
    private Element elementoDeRetorno(Element container, String nomeOperacao) {
        if (container == null) {
            return null;
        }
        if (container.getElementsByTagName("*").getLength() > 0) {
            // ja veio como elemento
            return container;
        }
        return elementoDeRetornoTexto(container.getTextContent(), nomeOperacao);
    }

    private Element elementoDeRetornoTexto(String texto, String nomeOperacao) {
        if (texto == null) {
            return null;
        }
        String limpo = texto.trim();
        if (limpo.isEmpty() || !limpo.startsWith("<")) {
            return null;
        }
        try {
            Document d = parseXml(limpo, nomeOperacao);
            return d.getDocumentElement();
        } catch (NfseSpException e) {
            return null;
        }
    }

    /** Le o grupo {@code Detalhe} da consulta de CNPJ (IM e se emite NFS-e). */
    public DetalheCnpj detalheCnpj(String soapResponse) {
        Element soap = parseXml(soapResponse, "SOAP").getDocumentElement();
        Element container = primeiroNo(soap, "RetornoXML", "retorno_xml");
        Element raiz = elementoDeRetorno(container, "consulta_cnpj_response");
        if (raiz == null) {
            return null;
        }
        Element detalhe = primeiroNo(raiz, "Detalhe");
        if (detalhe == null) {
            return null;
        }
        return new DetalheCnpj(
                textoDe(detalhe, "InscricaoMunicipal"),
                "true".equalsIgnoreCase(textoDe(detalhe, "EmiteNFe")));
    }

    public record DetalheCnpj(String inscricaoMunicipal, boolean emiteNfe) {
    }

    // ------------------------------------------------------------------

    private ChaveNfe chaveNfe(Element retorno) {
        // <ChaveNFeRPS><ChaveNFe><InscricaoPrestador>...
        Element chave = primeiroNo(retorno, "ChaveNFe");
        if (chave == null) {
            return null;
        }
        return new ChaveNfe(
                textoDe(chave, "InscricaoPrestador"),
                textoDe(chave, "NumeroNFe"),
                textoDe(chave, "CodigoVerificacao"),
                textoDe(chave, "ChaveNotaNacional"));
    }

    /**
     * Coleta {@code <Alerta>} ou {@code <Erro>}.
     *
     * <p>Quando ha varios, a prefeitura repete o elemento; quando ha um so,
     * ele traz tambem os dados do RPS que originou o erro.
     */
    private List<Mensagem> coletar(Element raiz, String tag) {
        List<Mensagem> saida = new ArrayList<>();
        NodeList nodes = raiz.getElementsByTagName(tag);
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (n.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element e = (Element) n;
            String codigo = textoDe(e, "Codigo");
            String descricao = textoDe(e, "Descricao");
            if (codigo != null || descricao != null) {
                saida.add(new Mensagem(codigo, descricao));
            }
        }
        return saida;
    }

    /** Primeiro elemento cujo nome local contenha o trecho, ignorando caixa. */
    private Element elementoComNomeContendo(Element raiz, String trecho) {
        NodeList todos = raiz.getElementsByTagName("*");
        for (int i = 0; i < todos.getLength(); i++) {
            Element e = (Element) todos.item(i);
            if (nomeLocal(e).toLowerCase().contains(trecho)) {
                return e;
            }
        }
        return null;
    }

    /** Primeiro elemento cujo nome (sem prefixo) casa, ignorando caixa. */
    private Element primeiroNo(Element raiz, String... nomes) {
        for (String nome : nomes) {
            NodeList direto = raiz.getElementsByTagName(nome);
            if (direto.getLength() > 0) {
                return (Element) direto.item(0);
            }
            NodeList todos = raiz.getElementsByTagName("*");
            for (int i = 0; i < todos.getLength(); i++) {
                Element e = (Element) todos.item(i);
                if (nome.equalsIgnoreCase(nomeLocal(e))) {
                    return e;
                }
            }
        }
        return null;
    }

    private String textoDe(Element pai, String tag) {
        if (pai == null) {
            return null;
        }
        NodeList nodes = pai.getElementsByTagName(tag);
        if (nodes.getLength() == 0) {
            // talvez com prefixo de namespace
            NodeList todos = pai.getElementsByTagName("*");
            for (int i = 0; i < todos.getLength(); i++) {
                Element e = (Element) todos.item(i);
                if (tag.equalsIgnoreCase(nomeLocal(e))) {
                    return e.getTextContent() == null ? null : e.getTextContent().trim();
                }
            }
            return null;
        }
        String t = nodes.item(0).getTextContent();
        return t == null ? null : t.trim();
    }

    private String nomeLocal(Element e) {
        String ln = e.getLocalName();
        return ln != null ? ln : e.getNodeName();
    }

    private Document parseXml(String xml, String contexto) {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(true);
            f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder b = f.newDocumentBuilder();
            // a prefeitura as vezes manda o Header SOAP com namespace estrito
            b.setErrorHandler(new org.xml.sax.helpers.DefaultHandler() {
                @Override
                public void error(org.xml.sax.SAXParseException e) {
                }

                @Override
                public void fatalError(org.xml.sax.SAXParseException e) throws org.xml.sax.SAXException {
                    throw e;
                }
            });
            return b.parse(new InputSource(new StringReader(xml)));
        } catch (Exception e) {
            throw new NfseSpException("Nao consegui ler o XML de retorno (" + contexto + "): " + e.getMessage(), e);
        }
    }

    private String resumir(String s) {
        if (s == null) {
            return "";
        }
        String l = s.replaceAll("\\s+", " ").trim();
        return l.length() > 300 ? l.substring(0, 300) + "..." : l;
    }
}
