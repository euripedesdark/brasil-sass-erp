package br.com.brasil_saas.fiscal.mdfe;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Contrato unico do MDF-e, no mesmo formato do {@code contrato_nfse}.
 *
 * <h3>Por que um contrato e nao o retorno cru da lib</h3>
 * O retorno da lib muda de nome conforme a chamada: {@code TRetEnviMDFe},
 * {@code TRetConsStatServ}, {@code TRetEvento}, {@code TRetConsReciMDFe}. Cada
 * um tem {@code cStat} e {@code xMotivo}, mas tambem tem {@code tpAmb} em
 * {@code String} numa e em {@code Object} noutra, e o nome do campo de retorno
 * muda conforme o chamador. Se o ERP fosse ler isso direto, cada endpoint
 * ganha um formato e qualquer ajuste na lib quebra a tela.
 *
 * <p>Com um contrato so, a lib pode mudar sem tocar no ERP. E o emissor que se
 * ajusta, nunca o consumidor.
 *
 * <h3>Os tres estados de {@code sucesso}</h3>
 * O mesmo desenho que ja foi feito para NFS-e, e pelo mesmo motivo:
 * <ul>
 *   <li>{@code true} — autorizado, com protocolo. Pode seguir.</li>
 *   <li>{@code false} — a SEFAZ recusou, com {@code cStat} e {@code xMotivo}.
 *       Reemitir e seguro.</li>
 *   <li>{@code null} — <b>nao deu para saber</b>. A chamada saiu mas a resposta
 *       nao voltou, ou o contrario: o MDF-e pode ter sido autorizado do outro lado.
 *       Reemitir aqui e o caminho da duplicidade — a Chave Natural (UF, CNPJ,
 *       serie, numero, modelo, forma de emissao) e rejeitada pela SVRS, mas o
 *       numero e sorteado no {@code cMDFe} e o documento ja gravado nao sabe
 *       disso. Confirmar antes.</li>
 * </ul>
 *
 * <p>Terceiro estado nao e paranoia: e o que impediu a duplicidade na NFS-e.
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonPropertyOrder({"sucesso", "cStat", "xMotivo", "operacao", "tempoMs", "dados"})
public record RespostaMdfePadrao(
        Boolean sucesso,
        Integer cStat,
        String xMotivo,
        String operacao,
        Long tempoMs,
        Map<String, Object> dados) {

    /**
     * Monta uma resposta. O contrato fala sucessor {@code null}, e nao
     * {@code false}, justamente para o terceiro estado existir: quem le
     * precisa distinguir "recusou" de "nao sei".
     */
    public static RespostaMdfePadrao resposta(Boolean sucesso, int cStat,
                                             String xMotivo, String operacao,
                                             long tempoMs, Map<String, Object> dados) {
        return new RespostaMdfePadrao(
                sucesso,
                cStat > 0 ? cStat : null,
                xMotivo == null || xMotivo.isBlank() ? null : xMotivo,
                operacao,
                tempoMs,
                dados == null ? new LinkedHashMap<>() : dados);
    }

    /** O nome do contrato, igual ao contrato_nfse da NFS-e. */
    @JsonProperty("contrato_mdfe")
    public String contrato() {
        return "contrato_mdfe";
    }

    public boolean autorizado() {
        return Boolean.TRUE.equals(sucesso);
    }

    /** Resposta de terceiro estado: a SVRS nao respondeu. */
    public static RespostaMdfePadrao indefinido(String operacao, String motivo) {
        Map<String, Object> vazio = new LinkedHashMap<>();
        return new RespostaMdfePadrao(null, null, motivo, operacao, 0L, vazio);
    }
}
