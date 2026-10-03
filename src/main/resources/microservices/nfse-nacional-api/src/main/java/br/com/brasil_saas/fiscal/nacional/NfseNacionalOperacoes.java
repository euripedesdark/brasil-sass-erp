package br.com.brasil_saas.fiscal.nacional;

/**
 * As oito operacoes do WebService do AGILIBlue, e como cada uma e' chamada.
 *
 * <p><b>A URL e' a base mais o nome da operacao.</b> A documentacao tecnica do
 * AGILIBlue traz o endereco completo de exemplo:
 *
 * <pre>
 *   https://nfse.rondonopolis.mt.gov.br/api/GerarNfse
 * </pre>
 *
 * <p>e a base isolada, com barra dupla:
 *
 * <pre>
 *   https://nfse.rondonopolis.mt.gov.br//api/
 * </pre>
 *
 * <p>A barra dupla da base e' a do documento. Enviar as duas formas funciona,
 * porque o servidor normaliza — mas a forma sem duplicidade e' a que o exemplo
 * de operacao usa, e e' essa que vai aqui.
 *
 * <h2>Como a homologacao funciona</h2>
 *
 * <p><b>Homologacao nao e' URL separada.</b> A documentacao diz:
 *
 * <blockquote>
 * "Para realizar a validacao do arquivo XML a ser enviado para a geracao da
 * NFS-e, e' possivel realizar a chamada em modo de homologacao. Para utilizar o
 * modo de homologacao, adicione a request o parametro booleano
 * {@code homologacao} com valor igual a true. Ao recepcionar uma requisicao com
 * esse parametro igual a true, o AGILIBlue NFS-e realizara todas as etapas de
 * validacao, mas ao final nao ir'a gravar a NFS-e no sistema, apenas retornando
 * uma confirmacao dos dados enviados."
 * </blockquote>
 *
 * <p>Isso tem tres consequencias que mudam como o ERP deve tratar o retorno:
 *
 * <ol>
 *   <li><b>E' a mesma URL.</b> Nao ha um host de homologacao para configurar. Um
 *       "ambiente de homologacao" implementado como URL separada aponta para
 *       um endereco que nao existe.</li>
 *   <li><b>Validacao completa, gravacao nenhuma.</b> O numero que volta na
 *       homologacao <b>nao existe</b> no sistema da prefeitura. Gravar esse
 *       numero como se fosse nota emitida produz uma NFS-e que o ERP lista e
 *       que ninguem jamais consultou.</li>
 *   <li><b>Vale para as oito operacoes.</b> O parametro vai na request de cada
 *       uma, e nao so na emissao.</li>
 * </ol>
 *
 * <h2>Sincrono e assincrono</h2>
 *
 * <p>Da tabela de servicos da documentacao:
 *
 * <table border="1">
 *   <caption>oito operacoes, sete sincronas</caption>
 *   <tr><th>operacao</th><th>processamento</th></tr>
 *   <tr><td>{@code GerarNfse}</td><td><b>sincrono</b> — volta a nota</td></tr>
 *   <tr><td>{@code EnviarLoteRps}</td><td><b>assincrono</b> — volta protocolo</td></tr>
 *   <tr><td>{@code CancelarNfse}</td><td>sincrono</td></tr>
 *   <tr><td>{@code SubstituirNfse}</td><td>sincrono</td></tr>
 *   <tr><td>{@code ConsultarLoteRps}</td><td>sincrono</td></tr>
 *   <tr><td>{@code ConsultarNfseRps}</td><td>sincrono</td></tr>
 *   <tr><td>{@code ConsultarNfseFaixa}</td><td>sincrono</td></tr>
 *   <tr><td>{@code ConsultarRequerimentoCancelamento}</td><td>sincrono</td></tr>
 * </table>
 *
 * <p>Sete das oito devolvem o resultado na propria chamada. So o
 * {@code EnviarLoteRps} devolve um protocolo para consulta posterior, e e' o
 * unico que exige que o ERP guarde o numero do lote para ir buscar depois.
 */
public final class NfseNacionalOperacoes {

    /** A base, sem barra no fim, como no exemplo da documentacao. */
    public static final String BASE_RONDONOPOLIS = "https://nfse.rondonopolis.mt.gov.br/api";

    /**
     * O parametro que liga a homologacao.
     *
     * <p> {@code true} liga. Ausente ou {@code false} grava a nota de verdade.
     */
    public static final String PARAMETRO_HOMOLOGACAO = "homologacao";

    private final String base;

    public NfseNacionalOperacoes(String base) {
        this.base = normalizar(base);
    }

    /**
     * Normaliza a base: sem barra no fim e sem barra dupla.
     *
     * <p>A base da documentacao e' {@code https://nfse.rondonopolis.mt.gov.br//api/}
     * — barra dupla, porque o documento mostra a URL como o cliente a digitava.
     * O exemplo de operacao, na mesma documentacao, e' sem a duplicidade. Os
     * dois funcionam porque o servidor normaliza, mas a forma com duplicidade
     * na URL que o ERP vai chamar nao e' a que a prefeitura testou.
     */
    private static String normalizar(String base) {
        if (base == null) {
            return "";
        }
        String b = base.trim();
        int esquema = b.indexOf("://");
        int inicio = esquema < 0 ? 0 : esquema + 3;
        String cabeca = b.substring(0, inicio);
        String cauda = b.substring(inicio).replaceAll("/{2,}", "/");
        while (cauda.endsWith("/")) {
            cauda = cauda.substring(0, cauda.length() - 1);
        }
        return cabeca + cauda;
    }

    public NfseNacionalOperacoes() {
        this(BASE_RONDONOPOLIS);
    }

    public String base() {
        return base;
    }

    // ------------------------------------------------------------------
    // as oito operacoes
    // ------------------------------------------------------------------

    /** Emissao de uma NFS-e. <b>Sincrona</b>: volta numero e codigo. */
    public String urlGerarNfse() {
        return url("GerarNfse");
    }

    /**
     * Lote de RPS. <b>Assincrona</b>: volta protocolo, e o lote so' existe
     * depois de consultado com {@link #urlConsultarLoteRps()}.
     */
    public String urlEnviarLoteRps() {
        return url("EnviarLoteRps");
    }

    /** Cancela uma NFS-e. Sincrona. */
    public String urlCancelarNfse() {
        return url("CancelarNfse");
    }

    /** Substitui uma NFS-e por outra. Sincrona. */
    public String urlSubstituirNfse() {
        return url("SubstituirNfse");
    }

    /** Consulta o resultado de um lote. Sincrona. */
    public String urlConsultarLoteRps() {
        return url("ConsultarLoteRps");
    }

    /** Consulta uma NFS-e pelo numero. Sincrona. */
    public String urlConsultarNfseRps() {
        return url("ConsultarNfseRps");
    }

    /** Consulta NFS-e por faixa de numero. Sincrona. */
    public String urlConsultarNfseFaixa() {
        return url("ConsultarNfseFaixa");
    }

    /** Consulta o andamento de um pedido de cancelamento. Sincrona. */
    public String urlConsultarRequerimentoCancelamento() {
        return url("ConsultarRequerimentoCancelamento");
    }

    public String baseSefin() {
        return base;
    }

    public String urlDistribuicao(long nsu, String cnpjConsulta, boolean lote) {
        throw new UnsupportedOperationException(
                "A distribuicao por NSU e' da nacional (ADN). O AGILIBlue consulta por "
                        + "numero da nota, em " + urlConsultarNfseRps() + ".");
    }

    // ------------------------------------------------------------------
    // montagem do endereco
    // ------------------------------------------------------------------

    /**
     * O endereco de uma operacao, ja com a homologacao se estiver ligada.
     *
     * @param homologacao {@code true} para validar sem gravar
     */
    public String url(String operacao, boolean homologacao) {
        String u = url(operacao);
        return homologacao ? u + "?" + PARAMETRO_HOMOLOGACAO + "=true" : u;
    }

    public String url(String operacao) {
        return base + "/" + operacao;
    }
}
