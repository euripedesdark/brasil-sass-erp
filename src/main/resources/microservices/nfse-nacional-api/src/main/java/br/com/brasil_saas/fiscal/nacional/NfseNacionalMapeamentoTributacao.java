package br.com.brasil_saas.fiscal.nacional;

import java.math.BigDecimal;
import java.math.RoundingMode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * O mapeamento de/para das retencoes federais.
 *
 * <h2>A regra, e ela e' federal</h2>
 *
 * <p>O aviso do AGILIBlue sobre a adequacao ao padrao ADN, secao "O que muda na
 * pratica", regra 2:
 *
 * <blockquote>
 * "Os valores correspondentes as retencoes de PIS e COFINS nao devem mais ser
 * enviados separadamente. Eles devem ser somados a CSLL e o valor total
 * (PIS + COFINS + CSLL) deve ser informado unicamente no campo CSLL."
 * </blockquote>
 *
 * <p>E a regra 1:
 *
 * <blockquote>
 * "Os valores informados nos campos de PIS e COFINS passam a ser utilizados
 * exclusivamente para fins de reducao da base de calculo do IBS e CBS."
 * </blockquote>
 *
 * <p><b>E o ISSQN nao entra nesta regra.</b> PIS, COFINS e CSLL sao tributos
 * federais; o ISSQN e' municipal e a NT 007/2026 nao o toca. Ele continua nos
 * campos proprios — {@code ValorBaseCalculoISSQN}, {@code AliquotaISSQN},
 * {@code ValorISSQNCalculado}, {@code ValorISSQNRecolher} — e a retencao e' a
 * tag {@code ISSQNRetido}. Agrupar PIS e COFINS em CSLL nao soma CSLL ao ISSQN,
 * e nao toca em nenhum dos dois grupos.
 *
 * <h2>Por que o mapeamento 1:1 produz nota errada, e nao erro</h2>
 *
 * <p>O ERP tem tres campos de retencao federal porque a contabilidade federal
 * trata os tres como tributos distintos, e o XSD tem tres tags com esses nomes.
 * <b>Mandar um a um passa na validacao de XSD.</b> E produz o liquido errado,
 * com as retencoes contadas duas vezes, e a base do IBS/CBS fora. Nenhum dos dois
 * e' erro de formato, entao a recusa da prefeitura chega como divergencia de
 * apuracao, e nao como "tag invalida".
 *
 * <table border="1">
 *   <caption>o de/para</caption>
 *   <tr><th>o que o ERP tem</th><th>para onde vai</th></tr>
 *   <tr><td>retencao PIS</td><td rowspan="3">soma em {@code ValorCsll}</td></tr>
 *   <tr><td>retencao COFINS</td></tr>
 *   <tr><td>retencao CSLL</td></tr>
 *   <tr><td>reducao de base do IBS/CBS por PIS</td><td>{@code ValorPis}</td></tr>
 *   <tr><td>reducao de base do IBS/CBS por COFINS</td><td>{@code ValorCofins}</td></tr>
 *   <tr><td>ISSQN</td><td>nos campos proprios, fora deste mapeamento</td></tr>
 *   <tr><td>INSS, IRRF, outras</td><td>continuam separados; o aviso nao os cita</td></tr>
 * </table>
 *
 * <p>As tags {@code ValorPis} e {@code ValorCofins} nao sumiram: <b>mudaram de
 * significado.</b> Eram retencao; agora sao reducao de base do IBS/CBS. O ERP
 * mantem as duas coisas separadas de proposito — a contabilidade precisa da
 * retencao, a prefeitura precisa da reducao — e e' aqui que uma vira a outra.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NfseNacionalMapeamentoTributacao {

    /**
     * O que o ERP tem, antes do mapeamento.
     *
     * @param retencaoPis       retencao de PIS, do ponto de vista do ERP
     * @param retencaoCofins    retencao de COFINS
     * @param retencaoCsll      retencao de CSLL
     * @param reducaoBasePis    quanto de PIS reduz a base do IBS/CBS
     * @param reducaoBaseCofins quanto de COFINS reduz a base do IBS/CBS
     */
    public record Impostos(
            BigDecimal retencaoPis,
            BigDecimal retencaoCofins,
            BigDecimal retencaoCsll,
            BigDecimal reducaoBasePis,
            BigDecimal reducaoBaseCofins) {
    }

    /**
     * O que vai no XML, depois do mapeamento.
     *
     * @param valorCsll     PIS + COFINS + CSLL, tudo em uma tag so
     * @param valorPis      reducao de base do IBS/CBS atribuida ao PIS
     * @param valorCofins   reducao de base do IBS/CBS atribuida ao COFINS
     * @param retencaoTotal a soma das tres, so para conference e log
     */
    public record ParaXml(
            BigDecimal valorCsll,
            BigDecimal valorPis,
            BigDecimal valorCofins,
            BigDecimal retencaoTotal) {
    }

    /**
     * Faz o mapeamento.
     *
     * <p>Ausente vira zero: as tres tags sao obrigatorias no XSD e a prefeitura
     * valida formato. Nota sem retencao tem retencao zero, e nao "campo
     * faltando" — que seria recusado.
     */
    public ParaXml mapear(Impostos i) {
        BigDecimal pis = zero(i == null ? null : i.retencaoPis());
        BigDecimal cofins = zero(i == null ? null : i.retencaoCofins());
        BigDecimal csll = zero(i == null ? null : i.retencaoCsll());

        BigDecimal total = pis.add(cofins).add(csll).setScale(2, RoundingMode.HALF_UP);

        if (total.signum() > 0 && pis.signum() == 0 && cofins.signum() == 0 && csll.signum() == 0) {
            // impossivel, mas cheap: se o total e' positivo, alguma das tres
            // entrou. registraria aqui se o agrupamento mudar de regra.
            log.debug("agrupamento federal: total {} em ValorCsll", total);
        }

        return new ParaXml(
                total,
                zero(i == null ? null : i.reducaoBasePis()),
                zero(i == null ? null : i.reducaoBaseCofins()),
                total);
    }

    /**
     * O que a prefeitura faz com o ISSQN, que e' municipal e nao entra no
     * agrupamento federal.
     *
     * <p><b>O {@code ValorLiquido} nao e' recalculado aqui, e essa e' a decisao
     * conscientemente tomada.</b> Os dez exemplos do material nao dizem qual e' a
     * conta, e tres deles tem um {@code ValorLiquido} sem formula nenhuma:
     *
     * <table border="1">
     *   <caption>o que os exemplos do proprio material mostram</caption>
     *     <tr><th>perfil</th><th>ValorLiquido</th><th>servicos menos deducoes</th></tr>
     *     <tr><td>Normal</td><td>1000.00</td><td>1000.00</td></tr>
     *     <tr><td>ConstrucaoCivil</td><td>1000.00</td>
     *         <td>1000.00, e o {@code ValorDeducaoConstCivil=100} nao baixou</td></tr>
     *     <tr><td>Imune</td><td><b>900.00</b></td><td>1000.00</td></tr>
     *     <tr><td>Isento</td><td><b>100.00</b></td><td>1000.00</td></tr>
     *     <tr><td>SN ME EPP</td><td><b>970.00</b></td><td>1000.00</td></tr>
     *   </table>
     *
     * <p>E o dado que fecha o caso: <b>os dez exemplos tem
     * {@code ValorISSQNRecolher=0} e todas as retencoes federais em zero.</b>
     * Nenhum exercita retencao, entao nenhum diz se o ISSQN sai do liquido.
     * Três tem {@code ISSQNRetido=1} e mesmo assim {@code ValorISSQNRecolher=0} —
     * retido sem valor a recolher.
     *
     * <p>Inventar a conta aqui seria o pior dos erros possiveis: uma formula errada
     * produz o liquido errado em todas as notas, sem nenhuma recusa, porque
     * {@code ValorLiquido} esta em {@code tsDescricao} e aceita qualquer numero.
     * O ERP manda o liquido que ele ja tem, e o que a API verifica e' a aritmetica
     * que o material <b>confirma</b>: base vezes aliquota igual ao
     * {@code ValorISSQNCalculado}.
     *
     * @see NfseNacionalRegras a verificacao da aritmetica do ISSQN
     */
    public String porQueNaoRecalculaLiquido() {
        return "ValorLiquido nao e' recalculado: os 10 exemplos do material nao exercitam "
                + "nenhuma retencao (ValorISSQNRecolher=0 nos 10, e as federais em zero) e "
                + "tres deles tem liquido sem formula. A conta e' definicao da prefeitura.";
    }

    private static BigDecimal zero(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
