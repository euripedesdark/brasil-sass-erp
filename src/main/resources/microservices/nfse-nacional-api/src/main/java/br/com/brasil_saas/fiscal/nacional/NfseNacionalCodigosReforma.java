package br.com.brasil_saas.fiscal.nacional;

import java.util.List;
import java.util.Map;

/**
 * Os codigos validos de {@code CodigoSituacaoTributaria} e
 * {@code CodigoTipoRetencao}.
 *
 * <p><b>Fonte: as tabelas do PDF</b>
 * {@code atualizacao-e-ajustes-estruturais-campo-tributacao-federal-pis-cofins-e-cssl.pdf},
 * secoes 4 e 5. As duas tabelas sao <b>imagens</b> dentro do PDF, sem camada de
 * texto: {@code pdftotext} nao pega nada. Os valores aqui foram lidos da pagina
 * renderizada.
 *
 * <h2>A tabela da situacao tem DUAS colunas de codigo</h2>
 *
 * <p>Um codigo negativo e um positivo, lado a lado, com a mesma descricao. O
 * negativo e' debito (saida) e o positivo e' credito (entrada). Sao <b>34
 * negativos</b>, de -1 a -34, e <b>34 positivos</b>, de 00 a 99.
 *
 * <p>O par de headers em -1 e' {@code 00 - Nenhum}: e' o valor padrao, e o
 * equivalente a "nao se aplica".
 *
 * <h2>Por que a tabela importa para o ERP</h2>
 *
 * <p>Os dois ultimos grupos sao os que o ERP vai usar no dia a dia, e nenhum
 * deles depende de decisao do emitente:
 *
 * <ul>
 *   <li>{@code -2} — Operacao Tributavel com Aliquota Basica</li>
 *   <li>{@code -1} / {@code 00} — Nenhum</li>
 * </ul>
 *
 * <p>Os demais sao tributacao com suspensao, isencao, substituicao, aliquota
 * zero, monofasica, por unidade de medida, aliquota diferenciada — e sao esses
 * que a prefeitura parametrizou no municipio, nao o ERP que escolhe.
 */
public final class NfseNacionalCodigosReforma {

    private NfseNacionalCodigosReforma() {
    }

    /**
     * {@code CodigoSituacaoTributaria}: debito (negativo) e credito (positivo).
     *
     * <p>Os valores de -1 ate -34 e de 00 a 99, com a mesma descricao nos dois
     * lados. O positivo de "Outras Operacoes" e' 99 e o negativo e' -34; nao ha
     * simetria entre as colunas, elas estao em ordem decrescente.
     */
    public static final Map<String, String> SITUACAO_TRIBUTARIA = Map.ofEntries(
            // ---- credito: saida do tributo (positivo) ----
            Map.entry("99", "Outras Operacoes"),
            Map.entry("98", "Outras Operacoes de Entrada"),
            Map.entry("75", "Operacao de Aquisicao por Substituicao Tributaria"),
            Map.entry("74", "Operacao de Aquisicao sem Incidencia da Contribuicao"),
            Map.entry("73", "Operacao de Aquisicao a Aliquota Zero"),
            Map.entry("72", "Operacao de Aquisicao com Suspensao"),
            Map.entry("71", "Operacao de Aquisicao com Isencao"),
            Map.entry("70", "Operacao de Aquisicao sem Direito a Credito"),
            Map.entry("67", "Credito Presumido - Outras Operacoes"),
            Map.entry("66", "Credito Presumido - Aquisicao a Receitas Tributadas e "
                    + "Nao-Tributadas no Mercado Interno e de Exportacao"),
            Map.entry("65", "Credito Presumido - Aquisicao a Receitas Nao-Tributadas "
                    + "no Mercado Interno e de Exportacao"),
            Map.entry("64", "Credito Presumido - Aquisicao a Receitas Tributadas no "
                    + "Mercado Interno e de Exportacao"),
            Map.entry("63", "Credito Presumido - Aquisicao a Receitas Tributadas e "
                    + "Nao-Tributadas no Mercado Interno"),
            Map.entry("62", "Credito Presumido - Aquisicao a Receitas Tributadas "
                    + "Vinculada Exclusivamente a Receita de Exportacao"),
            Map.entry("61", "Credito Presumido - Aquisicao a Receitas Tributadas "
                    + "Vinculada Exclusivamente a Receita Nao-Tributada no Mercado Interno"),
            Map.entry("60", "Credito Presumido - Aquisicao a Receitas Tributadas "
                    + "Vinculada Exclusivamente a Receita Tributada no Mercado Interno"),
            Map.entry("56", "Operacao com Direito a Credito - Vinculada a Receitas "
                    + "Tributadas e Nao-Tributadas no Mercado Interno e de Exportacao"),
            Map.entry("55", "Operacao com Direito a Credito - Vinculada a Receitas "
                    + "Nao Tributadas no Mercado Interno e de Exportacao"),
            Map.entry("54", "Operacao com Direito a Credito - Vinculada a Receitas "
                    + "Tributadas no Mercado Interno e de Exportacao"),
            Map.entry("53", "Operacao com Direito a Credito - Vinculada a Receitas "
                    + "Tributadas e Nao-Tributadas no Mercado Interno"),
            Map.entry("52", "Operacao com Direito a Credito - Vinculada Exclusivamente "
                    + "a Receita de Exportacao"),
            Map.entry("51", "Operacao com Direito a Credito - Vinculada Exclusivamente "
                    + "a Receita Nao-Tributada no Mercado Interno"),
            Map.entry("50", "Operacao com Direito a Credito - Vinculada Exclusivamente "
                    + "a Receita Tributada no Mercado Interno"),
            Map.entry("49", "Outras Operacoes de Saida"),
            Map.entry("09", "Operacao com Suspensao da Contribuicao"),
            Map.entry("08", "Operacao sem Incidencia da Contribuicao"),
            Map.entry("07", "Operacao Isenta da Contribuicao"),
            Map.entry("06", "Operacao Tributavel a Aliquota Zero"),
            Map.entry("05", "Operacao Tributavel por Substituicao Tributaria"),
            Map.entry("04", "Operacao Tributavel monofasica - Revenda a Aliquota Zero"),
            Map.entry("03", "Operacao Tributavel com Aliquota por Unidade de Medida "
                    + "de Produto"),
            Map.entry("02", "Operacao Tributavel com Aliquota Diferenciada"),
            Map.entry("01", "Operacao Tributavel com Aliquota Basica"),
            Map.entry("00", "Nenhum"),
            // ---- debito: entrada do tributo (negativo) ----
            Map.entry("-34", "Outras Operacoes"),
            Map.entry("-33", "Outras Operacoes de Entrada"),
            Map.entry("-32", "Operacao de Aquisicao por Substituicao Tributaria"),
            Map.entry("-31", "Operacao de Aquisicao sem Incidencia da Contribuicao"),
            Map.entry("-30", "Operacao de Aquisicao a Aliquota Zero"),
            Map.entry("-29", "Operacao de Aquisicao com Suspensao"),
            Map.entry("-28", "Operacao de Aquisicao com Isencao"),
            Map.entry("-27", "Operacao de Aquisicao sem Direito a Credito"),
            Map.entry("-26", "Credito Presumido - Outras Operacoes"),
            Map.entry("-25", "Credito Presumido - Aquisicao a Receitas Tributadas e "
                    + "Nao-Tributadas no Mercado Interno e de Exportacao"),
            Map.entry("-24", "Credito Presumido - Aquisicao a Receitas Nao-Tributadas "
                    + "no Mercado Interno e de Exportacao"),
            Map.entry("-23", "Credito Presumido - Aquisicao a Receitas Tributadas no "
                    + "Mercado Interno e de Exportacao"),
            Map.entry("-22", "Credito Presumido - Aquisicao a Receitas Tributadas e "
                    + "Nao-Tributadas no Mercado Interno"),
            Map.entry("-21", "Credito Presumido - Aquisicao a Receitas Tributadas "
                    + "Vinculada Exclusivamente a Receita de Exportacao"),
            Map.entry("-20", "Credito Presumido - Aquisicao a Receitas Tributadas "
                    + "Vinculada Exclusivamente a Receita Nao-Tributada no Mercado Interno"),
            Map.entry("-19", "Credito Presumido - Aquisicao a Receitas Tributadas "
                    + "Vinculada Exclusivamente a Receita Tributada no Mercado Interno"),
            Map.entry("-18", "Operacao com Direito a Credito - Vinculada a Receitas "
                    + "Tributadas e Nao-Tributadas no Mercado Interno e de Exportacao"),
            Map.entry("-17", "Operacao com Direito a Credito - Vinculada a Receitas "
                    + "Nao Tributadas no Mercado Interno e de Exportacao"),
            Map.entry("-16", "Operacao com Direito a Credito - Vinculada a Receitas "
                    + "Tributadas no Mercado Interno e de Exportacao"),
            Map.entry("-15", "Operacao com Direito a Credito - Vinculada a Receitas "
                    + "Tributadas e Nao-Tributadas no Mercado Interno"),
            Map.entry("-14", "Operacao com Direito a Credito - Vinculada Exclusivamente "
                    + "a Receita de Exportacao"),
            Map.entry("-13", "Operacao com Direito a Credito - Vinculada Exclusivamente "
                    + "a Receita Nao-Tributada no Mercado Interno"),
            Map.entry("-12", "Operacao com Direito a Credito - Vinculada Exclusivamente "
                    + "a Receita Tributada no Mercado Interno"),
            Map.entry("-11", "Outras Operacoes de Saida"),
            Map.entry("-10", "Operacao com Suspensao da Contribuicao"),
            Map.entry("-9", "Operacao sem Incidencia da Contribuicao"),
            Map.entry("-8", "Operacao Isenta da Contribuicao"),
            Map.entry("-7", "Operacao Tributavel a Aliquota Zero"),
            Map.entry("-6", "Operacao Tributavel por Substituicao Tributaria"),
            Map.entry("-5", "Operacao Tributavel monofasica - Revenda a Aliquota Zero"),
            Map.entry("-4", "Operacao Tributavel com Aliquota por Unidade de Medida "
                    + "de Produto"),
            Map.entry("-3", "Operacao Tributavel com Aliquota Diferenciada"),
            Map.entry("-2", "Operacao Tributavel com Aliquota Basica"),
            Map.entry("-1", "Nenhum"));

    /**
     * {@code CodigoTipoRetencao}: de -1 a -10, todos negativos.
     *
     * <p>Descreve <b>o que foi retido</b> de PIS, COFINS e CSLL. O padrao e' o
     * {@code -1}, que e' "nao retidos".
     */
    public static final Map<String, String> TIPO_RETENCAO = Map.ofEntries(
            Map.entry("-1", "PIS/COFINS/CSLL Nao Retidos"),
            Map.entry("-2", "PIS/COFINS Retido"),
            Map.entry("-3", "PIS/COFINS Nao Retido"),
            Map.entry("-4", "PIS/COFINS/CSLL Retidos"),
            Map.entry("-5", "PIS/COFINS Retidos, CSLL Nao Retido"),
            Map.entry("-6", "PIS Retido, COFINS/CSLL Nao Retido"),
            Map.entry("-7", "COFINS Retido, PIS/CSLL Nao Retido"),
            Map.entry("-8", "PIS Nao Retido, COFINS/CSLL Retidos"),
            Map.entry("-9", "PIS/COFINS Nao Retidos, CSLL Retido"),
            Map.entry("-10", "COFINS Nao Retido, PIS/CSLL Retidos"));

    /** O codigo que significa "nada a declarar". */
    public static final String NENHUM = "-1";

    /** Os valores de {@code CodigoSituacaoTributaria}, como o manual os lista. */
    public static List<String> valoresSituacao() {
        return SITUACAO_TRIBUTARIA.keySet().stream().sorted().toList();
    }

    /** Os valores de {@code CodigoTipoRetencao}, como o manual os lista. */
    public static List<String> valoresRetencao() {
        return TIPO_RETENCAO.keySet().stream().sorted((a, b) -> {
            // ordem numerica, e nao alfabetica: -10 vem antes de -2
            try {
                return Integer.compare(Integer.parseInt(a), Integer.parseInt(b));
            } catch (NumberFormatException e) {
                return a.compareTo(b);
            }
        }).toList();
    }

    /**
     * O codigo existe na tabela?
     *
     * <p>Verificar na tabela e melhor do que confiar no XSD, porque o XSD 1.00 do
     * material <b>nao tem</b> nenhuma das duas tags: elas foram criadas pela NT
     * 007/2026 e a prefeitura so publica o schema novo. Uma emissao com codigo
     * fora da tabela passa na validacao de XSD e e' recusada pela prefeitura.
     */
    public static boolean situacaoConhecida(String codigo) {
        return codigo != null && SITUACAO_TRIBUTARIA.containsKey(codigo.trim());
    }

    public static boolean retencaoConhecida(String codigo) {
        return codigo != null && TIPO_RETENCAO.containsKey(codigo.trim());
    }

    /**
     * A mensagem de recusa, com o codigo e o que ele significa.
     *
     * <p>Mostrar o valor e' o que permite o usuario corrigir sem abrir o PDF.
     */
    public static String explicar(String codigo, boolean retencao) {
        Map<String, String> tabela = retencao ? TIPO_RETENCAO : SITUACAO_TRIBUTARIA;
        String chave = codigo == null ? "" : codigo.trim();
        String desc = tabela.get(chave);
        String tag = retencao ? "CodigoTipoRetencao" : "CodigoSituacaoTributaria";
        if (desc == null) {
            return tag + " '" + chave + "' nao esta na tabela do manual. Valores aceitos: "
                    + (retencao ? valoresRetencao() : valoresSituacao());
        }
        return tag + " '" + chave + "' = " + desc;
    }
}
