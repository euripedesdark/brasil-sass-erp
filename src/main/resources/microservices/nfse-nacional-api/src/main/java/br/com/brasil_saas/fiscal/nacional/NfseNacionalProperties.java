package br.com.brasil_saas.fiscal.nacional;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuracao da NFS-e, lida de {@code nfse.nacional.*}.
 *
 * <p><b>O que fica aqui e' deste ERP. Os enderecos ficam em
 * {@link NfseNacionalOperacoes}.</b> Endereco e' informacao da prefeitura e
 * muda quando a prefeitura muda; o certificado e o cadastro do prestador sao
 * deste ERP.
 *
 * <h2>A homologacao e um parametro, nao um ambiente</h2>
 *
 * <p>Ver {@link NfseNacionalOperacoes}. Resume: a mesma URL, com
 * {@code ?homologacao=true}. Nao ha um segundo host para configurar, e o numero
 * que volta em homologacao <b>nao existe</b> na prefeitura.
 */
@Data
@ConfigurationProperties(prefix = "nfse.nacional")
public class NfseNacionalProperties {

    /**
     * A base do WebService.
     *
     * <p>Padrao: {@code https://nfse.rondonopolis.mt.gov.br/api}. A documentacao
     * traz o endereco de exemplo como {@code .../api/GerarNfse}, e a base
     * isolada com barra dupla, {@code .../api/}. A barra dupla e' normalizada
     * pelo servidor; aqui fica a forma sem duplicidade.
     */
    private String urlBase = NfseNacionalOperacoes.BASE_RONDONOPOLIS;

    /**
     * Liga a homologacao.
     *
     * <p>Padrao {@code true}. <b>Nao e' so por seguranca:</b> sem o
     * {@code ?homologacao=true} a prefeitura grava a nota, e a nota gravada
     * ocupa numero da serie de verdade.
     */
    private boolean homologacao = true;

    /**
     * CNPJ da prefeitura, para a tag {@code UnidadeGestora}.
     *
     * <p>A documentacao diz: "CNPJ da prefeitura (pegar o mesmo junto a
     * administracao publica municipal)". Vem da prefeitura, nao se deriva do
     * IBGE.
     */
    private String unidadeGestora;

    /**
     * Chave digital do prestador, de 32 caracteres.
     *
     * <p>A documentacao: "A chave digital podera ser obtida junto a
     * administracao publica municipal, atraves do sistema AGILIBlue NFS-e". E
     * <b>alternativa</b> a assinatura: o XSD e' um {@code xsd:choice} e as duas
     * nao podem ser enviadas juntas. Vazio faz a API assinar com o
     * certificado.
     */
    private String chaveDigital;

    /** CNPJ do prestador. E' o que assina. */
    private String cnpjPrestador;

    /**
     * Inscricao Municipal do prestador.
     *
     * <p>Os exemplos do material trazem com ponto ({@code 20330.52}). A
     * validacao local diz se chegou com ponto, em vez de deixar a prefeitura
     * recusar com mensagem de formato.
     */
    private String inscricaoMunicipalPrestador;

    /** Caminho do certificado A1 em .pfx. O mesmo arquivo da API de Sao Paulo. */
    private String certificadoCaminho;

    /** Senha do .pfx. */
    private String certificadoSenha;

    /**
     * Assina a declaracao quando nao ha ChaveDigital.
     *
     * <p>Padrao {@code true}. Desligar so em diagnostico: sem assinatura e sem
     * ChaveDigital, o {@code xsd:choice} fica sem ninguna das duas opcoes e a
     * prefeitura recusa antes de ler o resto.
     */
    private boolean assinar = true;

    /**
     * O formato da declaracao.
     *
     * <p><b>Pre e' antes da reforma, pos e' depois, e a escolha muda o XML.</b>
     * Os dois formatos tem 28 tags no mesmo nivel da declaracao, e quatro em cada
     * lado sao distintas:
     *
     * <table border="1">
     *   <caption>o que so existe em um dos formatos</caption>
     *   <tr><th>somente antes</th><th>somente depois</th></tr>
     *   <tr><td>{@code RegimeEspecialTributacao}</td>
     *       <td>{@code ItemLei116AtividadeEconomica}</td></tr>
     *   <tr><td>{@code CodigoAtividadeEconomica}</td><td>{@code CodigoNBS}</td></tr>
     *   <tr><td>{@code ValorDeducaoConstCivil}</td><td>{@code MunicipioPrestacaoServico}</td></tr>
     *   <tr><td>{@code Complements} → {@code Complemento}</td><td>{@code PisCofins}</td></tr>
     *   <tr><td>{@code DadosServico/ItemLei116}</td><td>—</td></tr>
     * </table>
     *
     * <p>E a ordem muda tambem: no formato novo, os dois codigos de atividade
     * economica ocupam o lugar do {@code CodigoAtividadeEconomica}, e o
     * {@code PisCofins} entra entre {@code ValorDescontos} e {@code ValorPis}.
     *
     * <p><b>Padrao {@code POS_REFORMA}.</b> A NT SE/CGNFS-e nº 007/2026 vale
     * desde <b>9 de fevereiro de 2026</b>, e o XML que o proprio AGILIBlue
     * distribuiu em 21/05/2026 ja e' no formato novo. Emitir no formato antigo
     * hoje produz nota sem os codigos de IBS/CBS.
     *
     * <p>Mandar os dois formatos misturados nao e' o que o servidor recusa — e'
     * o que a prefeitura <b>valida</b>: cada etapa so roda se a anterior
     * passou, e a etapa de XSD vem antes da assinatura.
     */
    private String formato = "POS_REFORMA";

    /**
     * Valida a declaracao contra o XSD 1.00 antes de enviar.
     *
     * <p><b>Padrao {@code false}, e o motivo nao e' desconfianca do validador.</b>
     * O unico XSD que existe e' o {@code nfse-v-100.xsd}, que e' do formato
     * <b>antes</b> da reforma. Validar uma declaracao do formato novo contra ele
     * <b>rejeita nota valida</b>:
     *
     * <pre>
     *   Element 'CodigoNBS': This element is not expected.
     *   Expected is ( ExigibilidadeISSQN ).
     * </pre>
     *
     * <p>Foi o que o validador respondeu para o {@code xml-21-de-maio-de-2026.xml}
     * do proprio material. Como a prefeitura nao publicou o XSD novo, a
     * validacao local nao tem contra o que comparar o formato padrao, e ligar
     * isso por padrao seria uma recusa garantida em toda emissao.
     *
     * <p>Ela continua disponivel: serve para o formato <b>antes</b> da reforma, e
     * util em diagnostico. Para o formato depois, a primeira verificacao e' a da
     * prefeitura.
     */
    private boolean validar = false;

    /** Prazo de rede, em milissegundos. */
    private long timeoutoMs = 60_000L;

    /**
     * O formato resolvido.
     *
     * @throws IllegalArgumentException se o nome nao for reconhecido, em vez de
     *         cair no padrao — formato errado em silencio produz nota sem
     *         IBS/CBS e sem nenhuma recusa do servidor
     */
    public Formato formato() {
        return Formato.de(formato);
    }

    /** Os dois formatos da declaracao. */
    public enum Formato {
        /** Antes da NT 007/2026. E' o que o {@code nfse-v-100.xsd} descreve. */
        PRE_REFORMA,
        /** Depois da NT 007/2026, vigente desde 09/02/2026. */
        POS_REFORMA;

        static Formato de(String nome) {
            if (nome == null || nome.isBlank()) {
                return POS_REFORMA;
            }
            return switch (nome.trim().toUpperCase(java.util.Locale.ROOT)) {
                case "PRE_REFORMA", "PRE", "ANTES", "1.00", "V1_00" -> PRE_REFORMA;
                case "POS_REFORMA", "POS", "DEPOIS", "POST", "007" -> POS_REFORMA;
                default -> throw new IllegalArgumentException(
                        "Formato desconhecido: '" + nome + "'. Use POS_REFORMA (vigente desde "
                                + "09/02/2026) ou PRE_REFORMA (o que o XSD 1.00 descreve). "
                                + "Formatos errados em silencio produzem nota sem IBS/CBS.");
            };
        }

        public boolean posReforma() {
            return this == POS_REFORMA;
        }
    }

    /**
     * As operacoes do endereco configurado.
     */
    public NfseNacionalOperacoes operacoes() {
        return new NfseNacionalOperacoes(urlBase);
    }

}
