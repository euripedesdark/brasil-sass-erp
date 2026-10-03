package br.com.brasil_saas.fiscal.nacional;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Monta o {@code CancelarNfseEnvio}.
 *
 * <p><b>Identico ao exemplo do material, e nao por acaso.</b> A estrutura vem
 * do {@code Leiame.txt} que veio junto dos exemplos, no arquivo
 * {@code CancelarNfse.xml}:
 *
 * <pre>
 *   CancelarNfseEnvio
 *     UnidadeGestora
 *     PedidoCancelamento
 *       IdentificacaoNfse
 *         Numero
 *         IdentificacaoPrestador
 *           ChaveDigital | dsig:Signature
 *           CpfCnpj
 *             Cpf | Cnpj
 *           InscricaoMunicipal
 *       CodigoCancelamento
 *       JustificativaCancelamento
 *       Versao
 * </pre>
 *
 * <p>Dois pontos em que o exemplo ensina e que custam a nota se ignorados:
 *
 * <ul>
 *   <li><b>{@code InscricaoMunicipal} com ponto.</b> O exemplo economico traz
 *       {@code 20330.52}. A validacao local rejeita ponto de proposito, porque
 *       o tipo do XSD e' numerico — mas no cancelamento o valor vem do que a
 *       prefeitura ja cadastrou, e e' este mesmo. Por isso aqui a tag leva
 *       o valor como a prefeitura devolveu, e a decisao de formatar fica com
 *       quem cadastrou.</li>
 *   <li><b>{@code CodigoCancelamento} e' a lista da prefeitura.</b> A
 *       documentacao lista "Lista de motivos de cancelamento" entre as
 *       tabelas que o municipio disponibiliza. O valor tem de estar nessa
 *       lista, e nao ser um numero escolhido aqui.</li>
 * </ul>
 */
public final class NfseNacionalCancelamentoBuilder {

    private NfseNacionalCancelamentoBuilder() {
    }

    /**
     * @param unidadeGestora       CNPJ da prefeitura
     * @param numero               o numero da NFS-e a cancelar
     * @param cnpjPrestador        CNPJ de quem pede
     * @param inscricaoMunicipal   IM de quem pede
     * @param codigoCancelamento   o motivo, da lista da prefeitura; vazio usa 1
     * @param justificativa        o texto do motivo, obrigatorio
     * @return o XML sem assinatura
     */
    public static String montar(String unidadeGestora,
                                String numero,
                                String cnpjPrestador,
                                String inscricaoMunicipal,
                                String codigoCancelamento,
                                String justificativa) {
        Document doc = novo();
        Element raiz = doc.getDocumentElement();

        txt(raiz, "UnidadeGestora", unidadeGestora);

        Element pedido = sub(raiz, "PedidoCancelamento");

        Element identNfse = sub(pedido, "IdentificacaoNfse");
        txt(identNfse, "Numero", numero);

        // Sem atributo Id, pelo mesmo motivo do GerarNfse: o XSD nao declara
        // atributo em tcIdentificacaoPrestador.
        Element prest = sub(identNfse, "IdentificacaoPrestador");
        // ChaveDigital, quando houver, entra aqui — e NfseNacionalSigner
        // preenche a assinatura no lugar dela.

        Element cpfCnpj = sub(prest, "CpfCnpj");
        if (digitos(cnpjPrestador).length() == 14) {
            txt(cpfCnpj, "Cnpj", digitos(cnpjPrestador));
        } else {
            txt(cpfCnpj, "Cpf", digitos(cnpjPrestador));
        }
        txt(prest, "InscricaoMunicipal", inscricaoMunicipal);

        txt(pedido, "CodigoCancelamento",
                codigoCancelamento == null || codigoCancelamento.isBlank()
                        ? "1" : codigoCancelamento);
        txt(pedido, "JustificativaCancelamento", justificativa);
        txt(pedido, "Versao", NfseNacionalDeclaracaoBuilder.VERSAO);

        return NfseNacionalClient.serializar(doc);
    }

    private static Document novo() {
        try {
            Document doc = javax.xml.parsers.DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().newDocument();
            doc.appendChild(doc.createElementNS(
                    NfseNacionalDeclaracaoBuilder.NS, "CancelarNfseEnvio"));
            return doc;
        } catch (Exception e) {
            throw new NfseNacionalException("Falha ao criar o cancelamento: " + e.getMessage(), e);
        }
    }

    private static Element sub(Element pai, String nome) {
        Element e = pai.getOwnerDocument().createElementNS(
                NfseNacionalDeclaracaoBuilder.NS, nome);
        pai.appendChild(e);
        return e;
    }

    private static void txt(Element pai, String nome, String valor) {
        if (valor == null || valor.isBlank()) {
            return;
        }
        Element e = pai.getOwnerDocument().createElementNS(
                NfseNacionalDeclaracaoBuilder.NS, nome);
        e.setTextContent(valor);
        pai.appendChild(e);
    }

    private static String digitos(String s) {
        return s == null ? "" : s.replaceAll("\\D", "");
    }
}
