package br.com.brasil_saas.fiscal.sp;

import br.com.brasil_saas.fiscal.sp.NfseSpRespostaParser.Mensagem;

import java.util.List;

/**
 * Contrato de resposta da NFS-e de São Paulo.
 *
 * <p>Este é o formato que as duas implementações têm de falar. Não existe
 * segundo formato: quem não falar isto, recusa.
 *
 * <h2>Por que um contrato só</h2>
 *
 * <p>Até 26/09/2026 a API Java devolvia o objeto plano e o bridge Ruby
 * embrulhava tudo em {@code "error"}, ainda por cima com o sucesso em inglês:
 *
 * <pre>
 * Java: {"sucesso":true,"numero_nfse":"26","codigo_verificacao":"CHGEIPFB", ...}
 * Ruby: {"error":{"success":true,"numero_nfse":"29","codigo_verificacao":"VFGXSERH", ...}}
 * </pre>
 *
 * <p>O ERP lia só o plano. Com o fallback ligado, a prefeitura emitia a nota,
 * o ERP não achava o sucesso, respondia "a prefeitura não confirmou" e
 * marcava a nota como falha <b>sem guardar número, código de verificação nem
 * chave</b> — que são justamente o que permite cancelar. Isso produziu as notas
 * 29 e 30 órfãs em 26/09/2026: existem na prefeitura e o ERP não sabe quais.
 *
 * <p>Arrumar o ERP para aceitar os dois formatos seria mais barato e é a escolha
 * errada: leitor de dois formatos é o mesmo bug com o acoplamento escondido. O
 * contrato fica aqui, num lugar só, e as duas implementações obedecem.
 *
 * <h2>Regras
 *
 * <ol>
 *   <li>O sucesso é sempre {@code sucesso}, em português, e é a única forma de
 *       dizer que saiu. Nenhuma outra chave significa sucesso.</li>
 *   <li><b>Nada é embrulhado.</b> O objeto devolvido é o contrato, no primeiro
 *       nível. Sem {@code error}, sem {@code data}, sem {@code result}.</li>
 *   <li>Recusa vem com {@code sucesso: false} e {@code erro} preenchido com
 *       texto. A resposta HTTP pode ser 4xx, mas o ERP não olha o status para
 *       decidir: olha {@code sucesso}.</li>
 *   <li>Campo ausente é String vazia, nunca {@code null} e nunca omitido. O
 *       ERP não precisa checar se a chave existe, e assim um campo novo não
 *       quebra quem lê.</li>
 *   <li><b>Só existe "não deu para saber" com HTTP 5xx ou corpo ilegível.</b>
 *       Resposta bem formada com {@code sucesso: false} é recusa, e recusa
 *       significa que nada foi emitido — é seguro reemitir. É a distinção que
 *       impede duplicidade: o ERP só manda a pessoa conferir quando não recebeu
 *       resposta bem formada, e nesses casos os identificadores precisam vir
 *       junto para dar para cancelar.</li>
 * </ol>
 *
 * @param sucesso        {@code true} emitido, {@code false} recusado
 * @param chaveNfse      inscrição municipal do prestador.despite o nome, não é
 *                       chave — é a IM, e a prefeitura manda assim
 * @param numeroNfse     número da nota na prefeitura
 * @param codigoVerificacao  o que permite cancelar quando não há número
 * @param chaveNotaNacional  chave nacional de 44 posições
 * @param alertas        avisos que não impediram a emissão
 * @param erro           motivo da recusa, só quando {@code sucesso} é falso
 * @param xmlAssinado    XML do pedido, quando a prefeitura devolve
 */
public record RespostaNfsePadrao(
        boolean sucesso,
        String chaveNfse,
        String numeroNfse,
        String codigoVerificacao,
        String chaveNotaNacional,
        List<Mensagem> alertas,
        String erro,
        String xmlAssinado) {

    /** Erro usado quando a recusa veio sem texto aproveitável. */
    public static final String ERRO_SEM_DETALHE = "A prefeitura recusou sem informar o motivo.";

    public RespostaNfsePadrao {
        // Campo ausente vira vazio, nunca null. O ERP le direto sem checar, e um
        // campo novo nao vira NullPointerException aqui dentro.
        chaveNfse = vazioSeNulo(chaveNfse);
        numeroNfse = vazioSeNulo(numeroNfse);
        codigoVerificacao = vazioSeNulo(codigoVerificacao);
        chaveNotaNacional = vazioSeNulo(chaveNotaNacional);
        erro = vazioSeNulo(erro);
        xmlAssinado = vazioSeNulo(xmlAssinado);
        alertas = alertas == null ? List.of() : List.copyOf(alertas);
    }

    public boolean emitido() {
        return sucesso;
    }

    /**
     * Constrói a resposta de emissão a partir do que a prefeitura devolveu.
     *
     * @param numero         número da nota, ou vazio
     * @param verificacao    código de verificação, ou vazio
     * @param chaveNacional  chave nacional, ou vazio
     * @param inscricao      IM do prestador
     */
    public static RespostaNfsePadrao emitido(String inscricao, String numero, String verificacao,
                                             String chaveNacional, List<Mensagem> alertas,
                                             String xml) {
        return new RespostaNfsePadrao(true, inscricao, numero, verificacao, chaveNacional,
                alertas, "", xml);
    }

    /** Resposta de recusa. O motivo é obrigatório, porque é o que orienta a correção. */
    public static RespostaNfsePadrao recusado(String motivo) {
        return new RespostaNfsePadrao(false, "", "", "", "", List.of(),
                motivo == null || motivo.isBlank() ? ERRO_SEM_DETALHE : motivo, "");
    }

    private static String vazioSeNulo(String valor) {
        return valor == null ? "" : valor;
    }
}
