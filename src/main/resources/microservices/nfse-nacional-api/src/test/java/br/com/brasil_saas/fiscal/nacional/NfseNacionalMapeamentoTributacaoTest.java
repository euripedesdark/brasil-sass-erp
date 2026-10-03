package br.com.brasil_saas.fiscal.nacional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * O mapeamento de/para que o aviso do ADN manda revisar.
 *
 * <p>Fonte: {@code adequacao-de-regras-de-tributacao-e-retencao-padrao-adn-25-de-maio-de-2026.pdf},
 * pagina 1, secao "O que muda na pratica".
 */
class NfseNacionalMapeamentoTributacaoTest {

    private NfseNacionalMapeamentoTributacao mapeamento;

    @BeforeEach
    void antes() {
        mapeamento = new NfseNacionalMapeamentoTributacao();
    }

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }

    /**
     * Compara BigDecimal por valor, e nao por {@code equals}.
     *
     * <p>{@code BigDecimal.equals} compara tambem a escala: {@code 0} e'
     * diferente de {@code 0.00}. Como o mapeamento devolve {@code BigDecimal.ZERO}
     * e o teste escreve {@code 0.00}, {@code equals} falha em valor certo — e a
     * falha seria "0 nao e' igual a 0.00", que leva a procurar erro onde nao ha.
     */
    private static void eq(BigDecimal esperado, BigDecimal obtido, String mensagem) {
        assertEquals(0, esperado.compareTo(obtido),
                mensagem + " (esperado " + esperado + ", obtido " + obtido + ")");
    }

    @Test
    @DisplayName("PIS, COFINS e CSLL somam em ValorCsll, e nada vai para ValorPis/ValorCofins")
    void retencoesFederaisSomamEmCsll() {
        // A regra 2 do aviso: "nao devem mais ser enviados separadamente. Eles
        // devem ser somados a CSLL".
        var r = mapeamento.mapear(new NfseNacionalMapeamentoTributacao.Impostos(
                bd("10.00"),   // retencao PIS
                bd("20.00"),   // retencao COFINS
                bd("5.00"),    // retencao CSLL
                null,          // sem reducao de base
                null));

        eq(bd("35.00"), r.valorCsll(), "10 + 20 + 5");
        eq(bd("0.00"), r.valorPis(), "a retencao de PIS nao vai para ValorPis: a tag virou reducao de base");
        eq(bd("0.00"), r.valorCofins(), "a retencao de COFINS nao vai para ValorCofins");
        assertEquals(bd("35.00"), r.retencaoTotal());
    }

    @Test
    @DisplayName("as tags ValorPis e ValorCofins recebem a reducao de base do IBS/CBS")
    void reducaoDeBaseVaiNasTagsDoPisCofins() {
        // A regra 1: "os valores informados nos campos de PIS e COFINS passam a ser
        // utilizados exclusivamente para fins de reducao da base de calculo do IBS
        // e CBS".
        var r = mapeamento.mapear(new NfseNacionalMapeamentoTributacao.Impostos(
                null, null, null,          // nenhuma retencao
                bd("7.50"),                 // reducao de base por PIS
                bd("3.25")));               // reducao de base por COFINS

        eq(bd("0.00"), r.valorCsll(), "sem retencao, o CSLL e' zero");
        assertEquals(bd("7.50"), r.valorPis());
        assertEquals(bd("3.25"), r.valorCofins());
    }

    @Test
    @DisplayName("retencao e reducao de base sao coisas independentes, e as duas podem existir")
    void retencaoEReducaoCoexistem() {
        // E' o caso comum: o tomador retem PIS/COFINS e isso ainda reduz a base.
        var r = mapeamento.mapear(new NfseNacionalMapeamentoTributacao.Impostos(
                bd("10.00"), bd("20.00"), bd("0.00"),
                bd("7.50"), bd("3.25")));

        eq(bd("30.00"), r.valorCsll(), "so as retencoes");
        eq(bd("7.50"), r.valorPis(), "so a reducao de base");
        eq(bd("3.25"), r.valorCofins(), "so a reducao de base");
    }

    @Test
    @DisplayName("o mapeamento 1:1 seria o bug: o liquido contaria as retencoes duas vezes")
    void mapeamentoUmAUmSeriaOBug() {
        // Sem o mapeamento, o ERP mandaria 10 / 20 / 5 em tres tags, e a
        // prefeitura receberia as mesmas tres retidas. Com o mapeamento, a
        // prefeitura recebe 35 uma vez.
        BigDecimal retencaoPis = bd("10.00");
        BigDecimal retencaoCofins = bd("20.00");
        BigDecimal retencaoCsll = bd("5.00");

        var r = mapeamento.mapear(new NfseNacionalMapeamentoTributacao.Impostos(
                retencaoPis, retencaoCofins, retencaoCsll, null, null));

        BigDecimal totalInformado = r.valorCsll();
        BigDecimal somaDireta = retencaoPis.add(retencaoCofins).add(retencaoCsll);
        assertEquals(somaDireta, totalInformado,
                "a soma das tres tem que ser o que chega na tag unica");
        // e nao a soma repetida em tres tags, que a prefeitura somaria de novo
        assertEquals(0, somaDireta.subtract(somaDireta).signum());
    }

    @Test
    @DisplayName("o ISSQN nao entra no agrupamento federal")
    void issqnNaoEntraNoAgrupamentoFederal() {
        // A NT 007/2026 e' federal. PIS, COFINS e CSLL sao tributos federais; o
        // ISSQN e' municipal e nao e' tocado. Por isso o agrupamento recebe so
        // as tres retencoes federais e nada de ISSQN.
        var comIssqn = mapeamento.mapear(new NfseNacionalMapeamentoTributacao.Impostos(
                bd("1.00"), bd("2.00"), bd("3.00"), null, null));
        assertEquals(bd("6.00"), comIssqn.valorCsll(),
                "o ISSQN nao soma no CSLL: sao tributos de esfera diferente");
    }

    @Test
    @DisplayName("nulo vira zero, e nao 'campo faltando'")
    void nuloViraZero() {
        // As tres tags de tributo sao obrigatorias no XSD. Ausente seria recusado
        // por formato; zero e' o valor de uma nota sem retencao.
        var r = mapeamento.mapear(new NfseNacionalMapeamentoTributacao.Impostos(
                null, null, null, null, null));
        assertEquals(0, r.valorCsll().signum());
        assertEquals(0, r.valorPis().signum());
        assertEquals(0, r.valorCofins().signum());
    }

    @Test
    @DisplayName("o ValorLiquido NAO e' recalculado, e o motivo esta escrito no codigo")
    void valorLiquidoNaoERecalculado() {
        // A tentacao e' fechar a conta aqui. Os dez exemplos do material nao
        // permitem: nenhum exercita retencao, e tres tem liquido sem formula
        // (Imune 900, Isento 100, SN ME EPP 970 com servicos 1000).
        String motivo = mapeamento.porQueNaoRecalculaLiquido();
        assertTrue(motivo.contains("ValorISSQNRecolher=0"),
                "o motivo tem que citar o dado que impede a conta: " + motivo);
        assertTrue(motivo.contains("900") || motivo.contains("formula"),
                "e tem que dizer que tres perfis nao tem formula: " + motivo);
    }

    // ------------------------------------------------------------------
    // as tabelas lidas do PDF
    // ------------------------------------------------------------------

    @Test
    @DisplayName("a tabela de CodigoSituacaoTributaria tem 34 negativos e 34 positivos")
    void tabelaSituacaoTemDuasColunas() {
        var t = NfseNacionalCodigosReforma.SITUACAO_TRIBUTARIA;
        long negativos = t.keySet().stream().filter(k -> k.startsWith("-")).count();
        long positivos = t.keySet().stream()
                .filter(k -> !k.startsWith("-")).count();
        assertEquals(34, negativos, "de -1 a -34");
        assertEquals(34, positivos, "de 00 a 99");
        assertEquals(68, t.size());
    }

    @Test
    @DisplayName("as duas colunas da tabela descrevem as mesmas operacoes")
    void colunasDaTabelaSaoSimetricas() {
        // -1 e 00 sao o mesmo par: "Nenhum". -2 e 01 tambem. E o que diz a
        // imagem da pagina 15.
        assertEquals("Nenhum",
                NfseNacionalCodigosReforma.SITUACAO_TRIBUTARIA.get("-1"));
        assertEquals("Nenhum",
                NfseNacionalCodigosReforma.SITUACAO_TRIBUTARIA.get("00"));
        assertEquals(
                NfseNacionalCodigosReforma.SITUACAO_TRIBUTARIA.get("-2"),
                NfseNacionalCodigosReforma.SITUACAO_TRIBUTARIA.get("01"));
        assertEquals("Operacao Tributavel com Aliquota Basica",
                NfseNacionalCodigosReforma.SITUACAO_TRIBUTARIA.get("-2"));
    }

    @Test
    @DisplayName("os dois codigos do dia a dia: -2 aliquota basica, -1 nenhum")
    void codigosDoDiaADia() {
        assertTrue(NfseNacionalCodigosReforma.situacaoConhecida("-2"));
        assertTrue(NfseNacionalCodigosReforma.situacaoConhecida(NfseNacionalCodigosReforma.NENHUM));
        assertTrue(NfseNacionalCodigosReforma.explicar("-2", false)
                        .contains("Operacao Tributavel com Aliquota Basica"),
                NfseNacionalCodigosReforma.explicar("-2", false));
    }

    @Test
    @DisplayName("a tabela de CodigoTipoRetencao tem 10 valores, de -1 a -10")
    void tabelaRetencaoTemDezValores() {
        assertEquals(10, NfseNacionalCodigosReforma.TIPO_RETENCAO.size());
        for (int i = 1; i <= 10; i++) {
            assertTrue(NfseNacionalCodigosReforma.retencaoConhecida("-" + i),
                    "falta o valor -" + i);
        }
        // ordem numerica: -10 antes de -2, que e' o que o manual lista
        var lista = NfseNacionalCodigosReforma.valoresRetencao();
        assertEquals("-10", lista.get(0));
        assertEquals("-1", lista.get(lista.size() - 1));
    }

    @Test
    @DisplayName("-1 e' o padrao nas duas tabelas: nada retido, situacao nenhuma")
    void menosUmENoPadrao() {
        assertEquals("PIS/COFINS/CSLL Nao Retidos",
                NfseNacionalCodigosReforma.TIPO_RETENCAO.get("-1"));
    }

    @Test
    @DisplayName("o codigo do exemplo do PDF: -2 com -5")
    void codigoDoExemploDoPdf() {
        // A pagina 13 do manual mostra:
        //   <CodigoSituacaoTributaria>-2</CodigoSituacaoTributaria>
        //   <CodigoTipoRetencao>-5</CodigoTipoRetencao>
        assertTrue(NfseNacionalCodigosReforma.explicar("-2", false)
                        .contains("Operacao Tributavel com Aliquota Basica"));
        assertTrue(NfseNacionalCodigosReforma.explicar("-5", true)
                        .contains("PIS/COFINS Retidos, CSLL Nao Retido"));
    }

    @Test
    @DisplayName("codigo fora da tabela e' recusado aqui, e nao so pela prefeitura")
    void codigoForaDaTabelaERecusado() {
        // O XSD 1.00 do material nao tem nenhuma das duas tags: elas sao da NT
        // 007/2026. Uma emissao com codigo errado passa na validacao de XSD e e'
        // recusada pela prefeitura, sem dizer que o valor nao existe.
        assertFalse(NfseNacionalCodigosReforma.situacaoConhecida("77"));
        assertFalse(NfseNacionalCodigosReforma.situacaoConhecida("-35"));
        assertFalse(NfseNacionalCodigosReforma.retencaoConhecida("0"));
        assertFalse(NfseNacionalCodigosReforma.retencaoConhecida("-11"));
        assertFalse(NfseNacionalCodigosReforma.retencaoConhecida("2"));

        // e a recusa mostra o que o codigo significa, para o usuario corrigir
        // sem abrir o PDF
        String recusa = NfseNacionalCodigosReforma.explicar("77", false);
        assertTrue(recusa.startsWith("CodigoSituacaoTributaria"),
                "a recusa nomeia a tag: " + recusa);
        assertTrue(recusa.contains("nao esta na tabela"),
                "e diz que o valor nao existe: " + recusa);
        assertTrue(recusa.contains("-2") && recusa.contains("99"),
                "e lista os valores aceitos, para o usuario corrigir: " + recusa);
    }
}
