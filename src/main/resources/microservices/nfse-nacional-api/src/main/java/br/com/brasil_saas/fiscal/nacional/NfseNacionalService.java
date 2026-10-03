package br.com.brasil_saas.fiscal.nacional;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * A emissao, do contrato do ERP ao retorno que o ERP sabe ler.
 *
 * <h2>A ordem das etapas, e ela nao e' arbitraria</h2>
 *
 * <ol>
 *   <li><b>validar o que o ERP mandou</b> — antes de gastar certificado;</li>
 *   <li><b>montar</b> a declaracao, na ordem do XSD;</li>
 *   <li><b>validar contra o XSD</b> — pega a ordem errada das tags;</li>
 *   <li><b>assinar</b>, se nao houver ChaveDigital;</li>
 *   <li><b>enviar</b>;</li>
 *   <li><b>traduzir a resposta</b>.</li>
 * </ol>
 *
 * <p>Os passos 3 e 4 nesta ordem sao o que a documentacao exige: a assinatura
 * so e' avaliada se o XSD passou. Invertidos, um erro de tag aparece como
 * recusa de assinatura e quem depura mexe no certificado, que esta perfeito.
 *
 * <h2>A diferenca que mais importa, e nao e' de codigo</h2>
 *
 * <p>A Prefeitura de Sao Paulo responde sincrona e a API pode dizer "emitido".
 * O AGILIBlue tambem e' sincrono no {@code GerarNfse} — mas <b>em homologacao
 * ele responde igual, sem gravar</b>. O numero que volta em homologacao nao
 * existe na prefeitura.
 *
 * <p>Por isso {@link #emitir} nunca marca a nota como emitida quando esta em
 * homologacao: marca como <b>validada</b>, com alerta. Gravar o numero de
 * homologacao como se fosse nota real produz uma NFS-e que o ERP lista, que
 * ocupa lugar na serie e que ninguem podera consultar na prefeitura.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NfseNacionalService {

    private final NfseNacionalProperties props;
    private final NfseNacionalRegras regras;
    private final NfseNacionalDeclaracaoBuilder declaracaoBuilder;
    private final NfseNacionalSigner signer;
    private final NfseNacionalCertificadoService certificadoService;
    private final NfseNacionalValidadorXsd validador;
    private final NfseNacionalClient client;
    private final NfseNacionalRespostaParser parser;

    /**
     * Emite.
     *
     * @return o contrato que o ERP le
     */
    public RespostaNfsePadrao emitir(NfseNacionalController.EmissaoRequest req) {
        NfseNacionalDeclaracaoBuilder.DadosDeclaracao dados = req == null ? null : req.paraDados(props);

        // 1) validacao do que o ERP mandou
        List<String> problemas = regras.validar(req, props);
        if (!problemas.isEmpty()) {
            log.info("declaracao recusada na validacao local: {}", problemas);
            return RespostaNfsePadrao.recusado(String.join("; ", problemas));
        }

        // 2) monta
        String xml;
        try {
            xml = declaracaoBuilder.montar(dados);
        } catch (NfseNacionalException e) {
            return RespostaNfsePadrao.recusado("Falha ao montar a declaracao: " + e.getMessage());
        }

        // 3) XSD — antes da assinatura, porque e' a ordem da prefeitura
        if (props.isValidar()) {
            List<String> erros = validador.validar(xml);
            if (!erros.isEmpty()) {
                log.warn("declaracao invalida contra o XSD ({} erro(s)): {}", erros.size(), erros);
                return RespostaNfsePadrao.recusado(
                        "Declaracao invalida contra o XSD do AGILIBlue: "
                                + String.join("; ", erros));
            }
        }

        // 4) assinatura — so se a ChaveDigital nao satisfizer o xsd:choice
        String xmlAssinado = xml;
        if (!temChaveDigital()) {
            try {
                NfseNacionalCertificadoService.ParDeAssinatura par = certificadoService.carregar();
                xmlAssinado = signer.assinarDeclaracao(
                        xml, par.chavePrivada(), par.certificado());
            } catch (NfseNacionalException e) {
                return RespostaNfsePadrao.recusado(
                        "Falha ao assinar a declaracao: " + e.getMessage());
            }
        }

        // 5) envio
        NfseNacionalClient.Resposta http;
        try {
            http = client.gerarNfse(xmlAssinado);
        } catch (NfseNacionalException e) {
            // A rede falhou antes de a prefeitura receber. A nota nao foi
            // gravada, e por isso e' seguro devolver recusa: quem chama pode
            // repetir com o mesmo numero. O que nao seria seguro e' devolver
            // recusa depois do envio ter passado — ver o passo 6.
            return RespostaNfsePadrao.recusado(
                    "Falha de comunicacao com a prefeitura: " + e.getMessage());
        }

        // 6) a resposta
        NfseNacionalRespostaParser.RespostaAgilblue lida = parser.ler(http.corpo());
        if (!lida.emitida()) {
            return parser.paraContrato(lida, xmlAssinado);
        }

        if (props.isHomologacao()) {
            return validadaEmHomologacao(lida, xmlAssinado);
        }
        return parser.paraContrato(lida, xmlAssinado);
    }

    /**
     * Homologacao: a prefeitura validou e nao gravou.
     *
     * <p>Este e' o unico lugar do projeto em que o numero volta e nao pode ser
     * gravado como nota. O contrato nao tem um estado "validada", entao o que
     * se faz e devolver o sucesso com a {@code chaveNfse} preenchida com o
     * marcador — e nao com o numero, que e' o que o ERP gravaria.
     */
    private RespostaNfsePadrao validadaEmHomologacao(
            NfseNacionalRespostaParser.RespostaAgilblue lida, String xmlEnviado) {
        List<RespostaNfsePadrao.Mensagem> alertas = new ArrayList<>();
        alertas.add(new RespostaNfsePadrao.Mensagem("HOMOLOGACAO",
                "A prefeitura validou a declaracao e NAO gravou a NFS-e "
                        + "(parametro homologacao=true). O numero " + lida.numero()
                        + " que voltou nao existe no sistema municipal e nao deve ser "
                        + "gravado como nota emitida."));
        for (NfseNacionalRespostaParser.RespostaAgilblue.Mensagem m : lida.mensagens()) {
            alertas.add(new RespostaNfsePadrao.Mensagem(m.codigo(), m.paraExibir()));
        }
        return new RespostaNfsePadrao(true,
                props.getInscricaoMunicipalPrestador() == null
                        ? "" : props.getInscricaoMunicipalPrestador(),
                "", lida.autenticidade() == null ? "" : lida.autenticidade(),
                "", alertas, "", lida.xmlNfse() != null ? lida.xmlNfse() : xmlEnviado);
    }

    /**
     * Cancela.
     *
     * <p>A documentacao diz que "o cancelamento da NFS-e ocorre em tempo real", e
     * {@code CancelarNfse} esta na lista de sincronas. Então aqui — diferente
     * do ambiente nacional, onde o cancelamento é assincrono — a resposta
     * significa o que diz.
     */
    public RespostaNfsePadrao cancelar(NfseNacionalController.CancelamentoRequest req) {
        if (req == null || req.detalhes() == null || req.detalhes().isEmpty()) {
            return RespostaNfsePadrao.recusado("Nenhum detalhe de cancelamento.");
        }
        for (var d : req.detalhes()) {
            if (d.numero() == null || d.numero().isBlank()) {
                return RespostaNfsePadrao.recusado(
                        "Detalhe de cancelamento sem o numero da NFS-e. O AGILIBlue cancela por "
                                + "numero, nao por chave nacional.");
            }
            if (d.motivo() == null || d.motivo().isBlank()) {
                return RespostaNfsePadrao.recusado(
                        "Cancelamento sem justificativa: o AGILIBlue exige "
                                + "<JustificativaCancelamento>.");
            }
        }

        NfseNacionalRespostaParser.RespostaAgilblue ultima = null;
        for (var d : req.detalhes()) {
            String pedido = NfseNacionalCancelamentoBuilder.montar(
                    props.getUnidadeGestora(),
                    d.numero(),
                    props.getCnpjPrestador(),
                    props.getInscricaoMunicipalPrestador(),
                    d.codigoCancelamento(),
                    d.motivo());

            String assinado = pedido;
            if (!temChaveDigital()) {
                try {
                    NfseNacionalCertificadoService.ParDeAssinatura par = certificadoService.carregar();
                    assinado = signer.assinarDeclaracao(
                            pedido, par.chavePrivada(), par.certificado());
                } catch (NfseNacionalException e) {
                    return RespostaNfsePadrao.recusado(
                            "Falha ao assinar o cancelamento: " + e.getMessage());
                }
            }

            NfseNacionalClient.Resposta http;
            try {
                http = client.cancelarNfse(assinado);
            } catch (NfseNacionalException e) {
                return RespostaNfsePadrao.recusado(
                        "Falha de comunicacao no cancelamento: " + e.getMessage());
            }

            NfseNacionalRespostaParser.RespostaAgilblue lida = parser.ler(http.corpo());
            if (!lida.emitida()) {
                return parser.paraContrato(lida, assinado);
            }
            ultima = lida;
        }

        // O AGILIBlue cancela em tempo real: resposta sem recusa e' o cancelamento
        // feito. A chave devolvida e' o numero da nota, que e' como o ERP
        // referencia a NFS-e.
        return new RespostaNfsePadrao(true,
                props.getInscricaoMunicipalPrestador() == null
                        ? "" : props.getInscricaoMunicipalPrestador(),
                ultima == null ? "" : ultima.numero(),
                ultima == null ? "" : ultima.autenticidade(), "",
                List.of(), "", "");
    }

    private boolean temChaveDigital() {
        return props.getChaveDigital() != null && !props.getChaveDigital().isBlank();
    }
}
