package br.com.brasil_saas.fiscal.sp;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * As operações do WebService da Prefeitura de Sao Paulo.
 *
 * <p>Extraído do WSDL em {@code https://nfews.prefeitura.sp.gov.br/lotenfe.asmx?WSDL},
 * que <b>só responde quando a chamada apresenta o certificado A1</b> (mTLS) —
 * sem ele o WAF devolve 403, o que parece problema de rede mas é de
 * autenticação.
 *
 * <p>Quatro coisas do WSDL quebram a integração se adivinhadas, e nenhuma
 * delas aparece em mensagem de erro útil:
 * <ol>
 *   <li>o elemento no corpo SOAP leva o sufixo <b>Request</b>
 *       ({@code ConsultaCNPJRequest}), e não o nome da operação — errar isso
 *       devolve {@code 1102 "Mensagem XML de Pedido do serviço sem conteúdo"},
 *       que parece problema de conteúdo e é de nome de elemento;</li>
 *   <li>o {@code SOAPAction} é a URI {@code .../nfe/ws/<acao>}, que em duas
 *       operações não segue o nome ({@code consultaCNPJ},
 *       {@code testeenvio});</li>
 *   <li>o envelope é <b>SOAP 1.2</b> e mesmo assim o cabeçalho
 *       {@code SOAPAction} é obrigatório — o WSDL publica os dois bindings e
 *       o serviço só responde ao 1.2 <b>com</b> o cabeçalho;</li>
 *   <li>a resposta vem em {@code <RetornoXML>} — com o X maiúsculo.</li>
 * </ol>
 *
 * <p>As quatro primeiras operações são as implementadas por esta API. As
 * demais constam porque o WebService as oferece e podem ser úteis depois
 * (nota em lote, consulta por período).
 */
public final class NfseSpOperacoes {

    private NfseSpOperacoes() {
    }

    public record Operacao(String nome, String soapAction) {

        /**
         * Elemento da operação no corpo SOAP. Leva o sufixo {@code Request}:
         * é o nome do elemento global declarado no WSDL, nao o da operação.
         */
        public String elemento() {
            return nome + "Request";
        }

        /** Elemento da resposta, para casar o no {@code RetornoXML}. */
        public String elementoResposta() {
            return nome + "Response";
        }

        /** Cabeçalho {@code SOAPAction}, com aspas, como o WSDL declara. */
        public String cabecalhoSoapAction() {
            return "\"" + soapAction + "\"";
        }
    }

    private static final String BASE = "http://www.prefeitura.sp.gov.br/nfe/ws/";

    // Nome do elemento e SOAPAction sao LIDOS DO WSDL, nao deduzidos: em duas
    // operacoes a acao nao segue o nome do elemento
    // (ConsultaCNPJ -> consultaCNPJ, TesteEnvioLoteRPS -> testeenvio), e errar
    // isso devolve "Server did not recognize the value of HTTP Header
    // SOAPAction" sem dizer quais valores seriam validos.
    public static final Operacao CONSULTA_CNPJ =
            new Operacao("ConsultaCNPJ", BASE + "consultaCNPJ");
    public static final Operacao ENVIO_RPS =
            new Operacao("EnvioRPS", BASE + "envioRPS");
    public static final Operacao CANCELAMENTO_NFE =
            new Operacao("CancelamentoNFe", BASE + "cancelamentoNFe");

    public static final Operacao ENVIO_LOTE_RPS =
            new Operacao("EnvioLoteRPS", BASE + "envioLoteRPS");
    public static final Operacao TESTE_ENVIO_LOTE_RPS =
            new Operacao("TesteEnvioLoteRPS", BASE + "testeenvio");
    public static final Operacao CONSULTA_NFE =
            new Operacao("ConsultaNFe", BASE + "consultaNFe");
    public static final Operacao CONSULTA_NFE_RECEBIDAS =
            new Operacao("ConsultaNFeRecebidas", BASE + "consultaNFeRecebidas");
    public static final Operacao CONSULTA_NFE_EMITIDAS =
            new Operacao("ConsultaNFeEmitidas", BASE + "consultaNFeEmitidas");
    public static final Operacao CONSULTA_LOTE =
            new Operacao("ConsultaLote", BASE + "consultaLote");
    public static final Operacao CONSULTA_INFORMACOES_LOTE =
            new Operacao("ConsultaInformacoesLote", BASE + "consultaInformacoesLote");

    private static Operacao of(String nome) {
        return new Operacao(nome, BASE + nome);
    }

    /** Todas, na ordem do WSDL. Útil para documentação e diagnóstico. */
    public static Map<String, Operacao> todas() {
        Map<String, Operacao> m = new LinkedHashMap<>();
        for (Operacao o : new Operacao[]{ENVIO_RPS, ENVIO_LOTE_RPS, TESTE_ENVIO_LOTE_RPS,
                CANCELAMENTO_NFE, CONSULTA_NFE, CONSULTA_NFE_RECEBIDAS, CONSULTA_NFE_EMITIDAS,
                CONSULTA_LOTE, CONSULTA_INFORMACOES_LOTE, CONSULTA_CNPJ}) {
            m.put(o.nome(), o);
        }
        return m;
    }
}
