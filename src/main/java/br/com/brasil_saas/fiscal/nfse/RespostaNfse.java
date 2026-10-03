package br.com.brasil_saas.fiscal.nfse;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Le a resposta da NFS-e em um formato so.
 *
 * <h2>Por que o emissor foi arrumado e nao o leitor</h2>
 *
 * <p>As duas implementacoes nao falam o mesmo idioma. A API Java devolve o
 * objeto plano; o bridge Ruby embrulha tudo em {@code "error"} e escreve o
 * sucesso em ingles:
 *
 * <pre>
 * Java: {"sucesso":true,"numero_nfse":"26","codigo_verificacao":"CHGEIPFB", ...}
 * Ruby: {"error":{"success":true,"numero_nfse":"29","codigo_verificacao":"VFGXSERH", ...}}
 * </pre>
 *
 * <p>Quando o ERP so sabia ler o plano, o fallback ligava e a prefeitura
 * emitia a nota — o ERP nao achava o sucesso, respondia "a prefeitura nao
 * confirmou" e marcava {@code FALHA_EMISSAO} sem guardar numero, codigo de
 * verificacao nem chave, que sao justamente o que permite cancelar. Aconteceu
 * com as notas <b>29</b> e <b>30</b> em 26/09/2026.
 *
 * <h2>Por que nao dois formatos</h2>
 *
 * <p>Arrumar o ERP para aceitar os dois parece mais barato e e a escolha errada.
 * Um leitor que conhece dois formatos e um emissor que escolhe entre eles e o
 * mesmo acoplamento, so que escondido: a proxima implementacao, ou um campo novo
 * da Ruby, volta a quebrar o ERP sem ninguem ver. Com o emissor arrumado, o
 * contrato fica em um lugar so e o leitor passa a ser o contrato.
 *
 * <p>Por isso {@link #ler} so engole o formato canonico. Se chegar outra coisa,
 * devolve {@code null} e o servico recusa com "formato desconhecido" em vez de
 * emitir errado. Falhar alto e melhor que adivinhar.
 *
 * <h2>Contrato</h2>
 *
 * <p>Um unico formato, sempre nestas chaves:
 *
 * <ul>
 *   <li>{@code sucesso} — {@code true} emitido, {@code false} recusado</li>
 *   <li>{@code numero_nfse}</li>
 *   <li>{@code codigo_verificacao}</li>
 *   <li>{@code chave_nota_nacional}</li>
 *   <li>{@code alertas} — lista de texto</li>
 *   <li>{@code erro} — texto, so na recusa</li>
 *   <li>{@code xml_assinado} — so quando a prefeitura devolve</li>
 * </ul>
 */
final class RespostaNfse {

    private RespostaNfse() {
    }

    /**
     * Le a resposta no contrato canonico, ou {@code null} se ela nao estiver nele.
     *
     * @return mapa com o contrato, ou {@code null} se a resposta e de outro formato
     */
    static Map<String, Object> ler(Map<String, Object> bruto) {
        if (bruto == null) {
            return null;
        }
        // So o plano e o canonico. O embrulho em "error" e recusado de proposito:
        // aceitar aqui seria voltar a dois formatos porque o emissor nao foi
        // arrumado, e o problema reaparece no proximo emissor.
        if (bruto.get("error") instanceof Map) {
            return null;
        }
        if (!bruto.containsKey("sucesso")) {
            return null;
        }
        return new LinkedHashMap<>(bruto);
    }

    /**
     * Diz se a prefeitura confirmou.
     *
     * <p>Tres estados, e o terceiro e o que importa:
     *
     * <ul>
     *   <li>{@code true} — confirmou, a nota existe</li>
     *   <li>{@code false} — respondeu que nao, com o motivo em {@code erro}</li>
     *   <li>{@code null} — <b>nao deu para saber</b>: resposta vazia ou fora do
     *       contrato</li>
     * </ul>
     *
     * <p>Tratar o terceiro como {@code false} produz duplicidade. Dizer "nao
     * emitiu" quando nao se sabe e mentira, e a pessoa reemite.
     */
    static Boolean confirmado(Map<String, Object> resposta) {
        if (resposta == null) {
            return null;
        }
        Object sucesso = resposta.get("sucesso");
        if (sucesso == null) {
            return null;
        }
        return Boolean.parseBoolean(String.valueOf(sucesso));
    }

    /** A recusa da prefeitura, quando houver. */
    static String motivo(Map<String, Object> resposta) {
        if (resposta == null) {
            return null;
        }
        Object erro = resposta.get("erro");
        if (erro instanceof Map<?, ?> mapa) {
            Object descricao = mapa.get("descricao");
            return descricao == null ? null : String.valueOf(descricao);
        }
        if (erro != null && !String.valueOf(erro).isBlank()) {
            return String.valueOf(erro);
        }
        return null;
    }

    /**
     * Os identificadores que a prefeitura devolve.
     *
     * <p>Quando {@link #confirmado} e {@code null}, estes campos sao a unica
     * pista do que aconteceu — e o que permite cancelar depois.
     */
    static String identificadores(Map<String, Object> resposta) {
        if (resposta == null) {
            return null;
        }
        String numero = texto(resposta.get("numero_nfse"));
        String verificacao = texto(resposta.get("codigo_verificacao"));
        String chave = texto(resposta.get("chave_nota_nacional"));
        if (numero == null && verificacao == null && chave == null) {
            return null;
        }
        return "numero=" + (numero == null ? "?" : numero)
                + " codigo_verificacao=" + (verificacao == null ? "?" : verificacao)
                + " chave=" + (chave == null ? "?" : chave);
    }

    private static String texto(Object valor) {
        if (valor == null) {
            return null;
        }
        String s = String.valueOf(valor);
        return s.isBlank() ? null : s;
    }
}
