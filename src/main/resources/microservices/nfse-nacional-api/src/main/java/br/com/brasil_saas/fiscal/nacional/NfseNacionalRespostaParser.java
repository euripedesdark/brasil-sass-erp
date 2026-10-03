package br.com.brasil_saas.fiscal.nacional;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Le a resposta do AGILIBlue e traduz para o contrato que o ERP ja le.
 *
 * <h2>Os dois desfechos, e como separei</h2>
 *
 * <p>A resposta do {@code GerarNfse} tem um {@code xsd:choice} na raiz: ou vem
 * {@code Nfse}, que e' a nota emitida, ou vem {@code ListaMensagemRetorno}, que
 * sao as inconsistencias. <b>E' a unica coisa que decide se a nota saiu.</b>
 * Ler o codigo HTTP e' ler a coisa errada: o AGILIBlue devolve 200 com a lista
 * de problemas preenchida, porque uma recusa de negocio nao e' erro de
 * transporte.
 *
 * <table border="1">
 *   <caption>o que cada desfecho devolve</caption>
 *   <tr><th></th><th>{@code Nfse}</th><th>{@code ListaMensagemRetorno}</th></tr>
 *   <tr><td>{@code Numero}</td><td>a nota</td><td>ausente</td></tr>
 *   <tr><td>{@code CodigoAutenticidade}</td><td>o codigo de verificacao</td><td>ausente</td></tr>
 *   <tr><td>{@code SituacaoNfse}</td><td>1 = normal</td><td>ausente</td></tr>
 *   <tr><td>o que e'</td><td>emitida</td><td>recusada, com o porque</td></tr>
 * </table>
 *
 * <p><b>E a resposta traz a nota inteira de volta</b>, em
 * {@code Nfse/DeclaracaoPrestacaoServico}. Isso e' o que o ERP precisa para
 * guardar o XML, e o que permite reconciliar o que foi enviado com o que foi
 * gravado pela prefeitura.
 */
@Component
@RequiredArgsConstructor
public class NfseNacionalRespostaParser {

    private final NfseNacionalProperties props;

    /** Namespace do AGILIBlue. Diferente do padrao nacional e do da SP. */
    public static final String NS = "http://www.agili.com.br/nfse_v_1.00.xsd";

    /**
     * O que o AGILIBlue devolveu.
     *
     * @param numero           {@code Nfse/Numero}
     * @param autenticidade    {@code Nfse/CodigoAutenticidade} — o codigo de verificacao
     * @param dataEmissao      {@code Nfse/DataEmissao}
     * @param situacao         {@code Nfse/SituacaoNfse}
     * @param orgaoGerador     {@code Nfse/IdentificacaoOrgaoGerador}
     * @param mensagens        {@code ListaMensagemRetorno/MensagemRetorno}
     * @param xmlNfse          a nota completa, quando houve emissao
     */
    public record RespostaAgilblue(
            boolean emitida,
            String numero,
            String autenticidade,
            String dataEmissao,
            String situacao,
            String orgaoGerador,
            List<Mensagem> mensagens,
            String xmlNfse) {

        /**
         * Uma das quatro tags de {@code tcMensagemRetorno}: Codigo, Mensagem,
         * Correcao, Versao.
         *
         * <p><b>{@code Correcao} e' o campo que importa.</b> {@code Codigo} e'
         * um numero generico do AGILIBlue, e {@code Mensagem} e' o texto. O que
         * diz o que ajustar e' {@code Correcao}, e e' ele que precisa aparecer
         * na mensagem que o ERP mostra ao usuario. So o codigo e' o caso em que
         * o ERP marca "falha na emissao" sem dizer o que fazer.
         */
        public record Mensagem(String codigo, String mensagem, String correcao, String versao) {

            /** O texto util, do campo que diz o que corrigir. */
            public String paraExibir() {
                StringBuilder sb = new StringBuilder();
                if (codigo != null && !codigo.isBlank()) {
                    sb.append('[').append(codigo).append("] ");
                }
                if (mensagem != null && !mensagem.isBlank()) {
                    sb.append(mensagem);
                }
                if (correcao != null && !correcao.isBlank()) {
                    sb.append(" — ").append(correcao);
                }
                return sb.toString();
            }
        }

        /** Emitiu, mas com avisos. */
        public boolean temAviso() {
            return emitida && mensagens != null && !mensagens.isEmpty();
        }
    }

    /** Le a resposta. */
    public RespostaAgilblue ler(String corpo) {
        if (corpo == null || corpo.isBlank()) {
            return new RespostaAgilblue(false, null, null, null, null, null,
                    List.of(new RespostaAgilblue.Mensagem(null,
                            "O AGILIBlue respondeu vazio.", null, null)), null);
        }

        // <b>A resposta do AGILIBlue nem sempre e' XML.</b> Em recusa de
        // primeira etapa ela devolve texto puro, e o exemplo que cameu na
        // homologacao e':
        //
        // <pre>
        //   HTTP 500
        //   ALERTA: XML invalida ou nao informada corretamente.
        // </pre>
        //
        // <p>Sem esta checagem, o parser tentava ler isso como XML e devolvia
        // "Content is not allowed in prolog" — que descreve o problema do
        // parser e nao o da prefeitura, e mandava quem depura procurar bug
        // onde o servidor ja tinha dito a causa.
        String texto = corpo.strip();
        if (texto.isEmpty()) {
            return new RespostaAgilblue(false, null, null, null, null, null,
                    List.of(new RespostaAgilblue.Mensagem(null,
                            "A prefeitura respondeu vazio.", null, null)), null);
        }
        if (!texto.startsWith("<")) {
            return new RespostaAgilblue(false, null, null, null, null, null,
                    List.of(new RespostaAgilblue.Mensagem(null,
                            "A prefeitura respondeu em texto, e nao em XML: " + curto(texto),
                            "A recusa vem antes da validacao de XSD, entao a causa "
                                    + "esta no proprio XML enviado: falta a "
                                    + "ChaveDigital ou a dsig:Signature no "
                                    + "IdentificacaoPrestador, ou ha tag fora de ordem.",
                            null)),
                    null);
        }

        Document doc;
        try {
            doc = NfseNacionalClient.parse(corpo);
        } catch (NfseNacionalException e) {
            return new RespostaAgilblue(false, null, null, null, null, null,
                    List.of(new RespostaAgilblue.Mensagem(null,
                            "Resposta do AGILIBlue parece XML mas nao abre: "
                                    + curto(texto), e.getMessage(), null)),
                    null);
        }

        String numero = texto(doc, "Numero");
        String autenticidade = texto(doc, "CodigoAutenticidade");
        List<RespostaAgilblue.Mensagem> mensagens = coletarMensagens(doc);

        // A escolha da raiz: Nfse presente e' emissao, ListaMensagemRetorno e'
        // recusa. Sem Nfse e sem mensagens, a resposta nao faz sentido.
        boolean emitida = numero != null || autenticidade != null;

        return new RespostaAgilblue(
                emitida,
                numero,
                autenticidade,
                texto(doc, "DataEmissao"),
                texto(doc, "SituacaoNfse"),
                texto(doc, "IdentificacaoOrgaoGerador"),
                mensagens,
                emitida ? NfseNacionalClient.serializar(doc) : null);
    }

    /**
     * Traduz para o contrato do ERP.
     *
     * <p><b>Em homologacao o numero que volta nao existe.</b> O AGILIBlue
     * valida tudo e nao grava, mas o {@code GerarNfseResposta} vem igual. O
     * contrato vai marcar isso num alerta, porque sem isso o ERP guarda um
     * numero que ninguem podera consultar na prefeitura e que ocupa lugar na
     * serie.
     */
    public RespostaNfsePadrao paraContrato(RespostaAgilblue r, String xmlEnviado) {
        String im = props.getInscricaoMunicipalPrestador();
        if (im == null) {
            im = "";
        }

        if (!r.emitida()) {
            String porque = r.mensagens().isEmpty()
                    ? "O AGILIBlue nao devolveu a nota nem a lista de erros."
                    : r.mensagens().stream().map(RespostaAgilblue.Mensagem::paraExibir)
                            .reduce((a, b) -> a + "; " + b).orElse("");
            return RespostaNfsePadrao.recusado("O AGILIBlue recusou a NFS-e: " + porque);
        }

        List<RespostaNfsePadrao.Mensagem> alertas = new ArrayList<>();
        for (RespostaAgilblue.Mensagem m : r.mensagens()) {
            alertas.add(new RespostaNfsePadrao.Mensagem(m.codigo(), m.paraExibir()));
        }
        if (r.situacao() != null && !"1".equals(r.situacao())) {
            // Situacao diferente de 1 significa que a nota existe mas nao esta
            // normal — cancelada, substituida, em analise. O ERP precisa saber
            // disso no momento da gravacao, nao na proxima consulta.
            alertas.add(new RespostaNfsePadrao.Mensagem("SITUACAO",
                    "A prefeitura devolveu SituacaoNfse=" + r.situacao()
                            + ". A nota foi gravada, mas nao esta em situacao normal."));
        }

        return new RespostaNfsePadrao(true, im, r.numero(), r.autenticidade(), "",
                alertas, "", r.xmlNfse() != null ? r.xmlNfse() : xmlEnviado);
    }

    /** As mensagens de {@code ListaMensagemRetorno/MensagemRetorno}. */
    private static List<RespostaAgilblue.Mensagem> coletarMensagens(Document doc) {
        List<RespostaAgilblue.Mensagem> out = new ArrayList<>();
        NodeList nl = doc.getElementsByTagNameNS("*", "MensagemRetorno");
        for (int i = 0; i < nl.getLength(); i++) {
            Element e = (Element) nl.item(i);
            out.add(new RespostaAgilblue.Mensagem(
                    filho(e, "Codigo"),
                    filho(e, "Mensagem"),
                    filho(e, "Correcao"),
                    filho(e, "Versao")));
        }
        return out;
    }

    /** O texto da primeira tag com esse nome, em qualquer namespace. */
    /**
     * Encurta o texto sem cortar no meio de uma palavra.
     *
     * <p>O nome e' {@code curto} e nao {@code resumir} porque {@code texto} e'
     * {@code texto(Document, tag)} neste arquivo: dois metodos chamadas
     * {@code texto} com assinaturas diferentes no mesmo corpo e' a hora em que
     * um deles vira o outro.
     */
    private static String curto(String s) {
        if (s == null) {
            return "(vazio)";
        }
        String t = s.strip().replaceAll("\\s+", " ");
        return t.length() <= 300 ? t : t.substring(0, 300) + "...";
    }

    private static String texto(Document doc, String tag) {
        NodeList nl = doc.getElementsByTagNameNS("*", tag);
        return nl.getLength() == 0 ? null : nl.item(0).getTextContent();
    }

    private static String filho(Element pai, String tag) {
        NodeList nl = pai.getElementsByTagNameNS("*", tag);
        return nl.getLength() == 0 ? null : nl.item(0).getTextContent();
    }

}
