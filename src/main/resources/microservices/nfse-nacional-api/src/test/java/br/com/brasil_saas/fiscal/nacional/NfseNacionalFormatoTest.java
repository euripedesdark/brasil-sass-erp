package br.com.brasil_saas.fiscal.nacional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Os dois formatos sao dois, e nao um so.
 *
 * <p><b>Esta classe existe porque eu misturei os dois.</b> O
 * {@code xml-21-de-maio-de-2026.xml} do material do dono ja e' do formato de
 * depois da reforma, e o {@code GerarNfseEnvio - Normal.xml} e' do formato de
 * antes. Os dois tem 28 tags no mesmo nivel da declaracao, e quatro em cada lado
 * sao distintas — de modo que um XML do formato novo passa quase inteiro no
 * XSD do formato antigo, e o que sobra e' erro de posicao.
 */
class NfseNacionalFormatoTest {

    private NfseNacionalMapeamentoTributacao mapeamento;

    @BeforeEach
    void antes() {
        mapeamento = new NfseNacionalMapeamentoTributacao();
    }

    private NfseNacionalDeclaracaoBuilder builder(String formato) {
        NfseNacionalProperties p = new NfseNacionalProperties();
        p.setUnidadeGestora("03347101000121");
        p.setCnpjPrestador("12530068000161");
        p.setInscricaoMunicipalPrestador("110863");
        p.setChaveDigital("e3573c9c92992c02042b3b3726916b36");
        p.setFormato(formato);
        return new NfseNacionalDeclaracaoBuilder(p, mapeamento);
    }

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }

    /**
     * A declaracao completa, com a atividade economica do formato pedido.
     *
     * <p>Preencher as duas atividades de uma vez e' recusado pelo builder, e com
     * razao: no XSD sao alternativa. Por isso o helper recebe o formato e
     * preenche so a tag que ele usa.
     */
    private NfseNacionalDeclaracaoBuilder.DadosDeclaracao comOsDois(String formato) {
        boolean pos = "POS_REFORMA".equals(formato);
        var D = NfseNacionalDeclaracaoBuilder.DadosDeclaracao.class;
        var base = comOsDois();
        return new NfseNacionalDeclaracaoBuilder.DadosDeclaracao(
                base.unidadeGestora(), base.prestador(), base.rps(), base.nfseSubstituida(),
                base.tomador(), base.intermediario(), base.materialUsado(), base.regime(),
                pos ? new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.AtividadeEconomica(null, null, "070901")
                        : new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.AtividadeEconomica("62040.00", null, null),
                base.incidencia(), base.municipioPrestacao(), base.valores(),
                base.servico(), base.beneficioProcesso(), base.cartaCorrecao(),
                pos ? new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.Reforma("-2", "-3", "124033200")
                        : new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.Reforma(null, null, null));
    }

    /** O esqueleto, com as duas atividades preenchidas, para os testes de montagem. */
    private NfseNacionalDeclaracaoBuilder.DadosDeclaracao comOsDois() {
        return new NfseNacionalDeclaracaoBuilder.DadosDeclaracao(
                "03347101000121",

                new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.IdentificacaoPrestador(
                        "e3573c9c92992c02042b3b3726916b36", "12530068000161", null, "110863"),

                new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.Rps(
                        "1", "NFSe", "-2", "nota", null, null,
                        java.time.LocalDate.of(2026, 9, 27)),
                0L,

                new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.DadosTomador(
                        "77257895000179", null, null, null, "CLIENTE LTDA", "1", false,
                        "Rua", "Bandeira Azul", "737", "Casa", "Jardim Vitoria",
                        "5107602", "RONDONOPOLIS", "MT", "79680000", null, null, null,
                        "88888888", "cliente@x.com.br", null),

                null, null,

                new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.Regime("-2", "Nenhum", false, false, false,
                        "-3", "Prestador do servico", "62040.00", "-1", "Exigivel"),

                new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.AtividadeEconomica("62040.00", null, "070901"),

                new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.Incidencia("5107602", "RONDONOPOLIS", "MT"),
                new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.MunicipioPrestacao("5103403", "CUIABA", "MT"),

                new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.Valores(
                        bd("181.54"), bd("0"), bd("181.54"), bd("5.00"),
                        bd("9.08"), bd("0"), null, null,
                        bd("100.00"), bd("173.54"),
                        bd("10.00"), bd("20.00"), bd("8.00"),
                        bd("5.00"), bd("3.00"),
                        bd("0"), bd("0"), bd("0"),
                        "observacao", null),

                new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.ItemServico("Consultoria", "62040.00", "1.06",
                        bd("1"), bd("181.54"), bd("0")),

                "beneficio",
                null,
                new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.Reforma("-2", "-3", "124033200"));
    }

    private static List<String> tagsNivel0(String xml, String bloco) throws Exception {
        Document doc = NfseNacionalClient.parse(xml);
        NodeList nl = doc.getElementsByTagNameNS("*", bloco);
        Element pai = (Element) nl.item(0);
        List<String> out = new ArrayList<>();
        NodeList filhos = pai.getChildNodes();
        for (int i = 0; i < filhos.getLength(); i++) {
            Node n = filhos.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                out.add(n.getLocalName());
            }
        }
        return out;
    }

    // ------------------------------------------------------------------

    @Test
    @DisplayName("o formato depois da reforma tem as 4 tags novas e nao tem as 4 antigas")
    void posReformaTemAsNovas() throws Exception {
        String xml = builder("POS_REFORMA").montar(comOsDois("POS_REFORMA"));
        List<String> tags = tagsNivel0(xml, "DeclaracaoPrestacaoServico");

        assertTrue(tags.contains("ItemLei116AtividadeEconomica"), "so no novo: " + tags);
        assertTrue(tags.contains("CodigoNBS"), "so no novo: " + tags);
        assertTrue(tags.contains("MunicipioPrestacaoServico"), "so no novo: " + tags);
        assertTrue(tags.contains("PisCofins"), "so no novo: " + tags);

        assertFalse(tags.contains("RegimeEspecialTributacao"), "so no antigo: " + tags);
        assertFalse(tags.contains("CodigoAtividadeEconomica"), "so no antigo: " + tags);
        assertFalse(tags.contains("ValorDeducaoConstCivil"), "so no antigo: " + tags);
        assertFalse(tags.contains("Complemento"), "so no antigo: " + tags);
        assertFalse(tags.contains("Rps"), "o Rps sumiu no novo: " + tags);
        assertFalse(tags.contains("NfseSubstituida"), "so no antigo: " + tags);
    }

    @Test
    @DisplayName("o formato antes da reforma tem as 4 antigas e nao tem as 4 novas")
    void preReformaTemAsAntigas() throws Exception {
        String xml = builder("PRE_REFORMA").montar(comOsDois("PRE_REFORMA"));
        List<String> tags = tagsNivel0(xml, "DeclaracaoPrestacaoServico");

        assertTrue(tags.contains("RegimeEspecialTributacao"), "so no antigo: " + tags);
        assertTrue(tags.contains("CodigoAtividadeEconomica"), "so no antigo: " + tags);
        assertTrue(tags.contains("ValorDeducaoConstCivil"), "so no antigo: " + tags);
        assertTrue(tags.contains("Complemento"), "so no antigo: " + tags);
        assertTrue(tags.contains("Rps"), "so no antigo: " + tags);

        assertFalse(tags.contains("ItemLei116AtividadeEconomica"), "so no novo: " + tags);
        assertFalse(tags.contains("CodigoNBS"), "so no novo: " + tags);
        assertFalse(tags.contains("MunicipioPrestacaoServico"), "so no novo: " + tags);
        assertFalse(tags.contains("PisCofins"), "so no novo: " + tags);
    }

    @Test
    @DisplayName("a ordem do formato novo bate com o xml de 21/05/2026, tag a tag")
    void ordemDoFormatoNovo() throws Exception {
        List<String> tags = tagsNivel0(
                builder("POS_REFORMA").montar(comOsDois("POS_REFORMA")), "DeclaracaoPrestacaoServico");

        // A sequencia do exemplo xml-21-de-maio-de-2026.xml do material.
        assertEquals(List.of(
                "IdentificacaoPrestador", "DadosTomador",
                "OptanteSimplesNacional", "OptanteMEISimei", "ISSQNRetido",
                "ResponsavelISSQN", "ItemLei116AtividadeEconomica", "CodigoNBS",
                "ExigibilidadeISSQN", "MunicipioPrestacaoServico", "MunicipioIncidencia",
                "ValorServicos", "ValorDescontos", "PisCofins",
                "ValorPis", "ValorCofins", "ValorInss", "ValorIrrf", "ValorCsll",
                "ValorOutrasRetencoes", "ValorBaseCalculoISSQN", "AliquotaISSQN",
                "ValorISSQNCalculado", "ValorISSQNRecolher", "ValorLiquido",
                "Observacao", "ListaServico", "Versao"),
                tags);
    }

    @Test
    @DisplayName("o PisCofins fica entre ValorDescontos e ValorPis, e nao depois")
    void pisCofinsNaPosicaoCerta() throws Exception {
        // A posicao e' o que faz o XSD aceitar. PisCofins depois de ValorCsll
        // produz "Invalid content was found starting with element PisCofins".
        String xml = builder("POS_REFORMA").montar(comOsDois("POS_REFORMA"));
        int descontos = xml.indexOf("<ValorDescontos>");
        int pisCofins = xml.indexOf("<PisCofins>");
        int pis = xml.indexOf("<ValorPis>");
        assertTrue(descontos < pisCofins, "PisCofins depois de ValorDescontos");
        assertTrue(pisCofins < pis, "e antes de ValorPis");
    }

    @Test
    @DisplayName("no formato novo, DadosServico nao tem ItemLei116")
    void itemLei116SaiDoServicoNoFormatoNovo() throws Exception {
        String antes = builder("PRE_REFORMA").montar(comOsDois("PRE_REFORMA"));
        assertTrue(antes.contains("<ItemLei116>1.06</ItemLei116>"),
                "no antigo o codigo do servico vai no item: " + antes);

        String depois = builder("POS_REFORMA").montar(comOsDois("POS_REFORMA"));
        assertFalse(depois.contains("<ItemLei116>"),
                "no novo ele sobe para a declaracao; no item nao cabe mais");
        assertTrue(depois.contains("<ItemLei116AtividadeEconomica>070901"),
                "e volta como ItemLei116AtividadeEconomica");
        assertTrue(depois.contains("<CodigoNBS>124033200"), "com o NBS ao lado");
    }

    @Test
    @DisplayName("a atividade economica aceita uma das tres, nunca duas")
    void atividadeEconomicaEAlternativa() {
        // O XSD tem CodigoAtividadeEconomica, CodigoCnaeAtividadeEconomica e
        // ItemLei116AtividadeEconomica, e a prefeitura parametriza qual usar.
        var base = comOsDois();

        var duas = new NfseNacionalDeclaracaoBuilder.DadosDeclaracao(
                base.unidadeGestora(), base.prestador(), base.rps(), base.nfseSubstituida(),
                base.tomador(), base.intermediario(), base.materialUsado(), base.regime(),
                new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.AtividadeEconomica("62040.00", "62040.00", null),
                base.incidencia(), base.municipioPrestacao(), base.valores(),
                base.servico(), base.beneficioProcesso(), base.cartaCorrecao(), base.reforma());

        NfseNacionalException e = assertThrows(NfseNacionalException.class,
                () -> builder("POS_REFORMA").montar(duas));
        assertTrue(e.getMessage().contains("tres tags"),
                "a recusa tem que explicar a alternativa: " + e.getMessage());
    }

    @Test
    @DisplayName("o formato padrao e' o depois da reforma")
    void padraoPosReforma() {
        // A NT 007/2026 vale desde 09/02/2026, e o XML que o proprio AGILIBlue
        // mandou em 21/05/2026 ja e' do formato novo.
        assertEquals(NfseNacionalProperties.Formato.POS_REFORMA,
                new NfseNacionalProperties().formato());
        assertTrue(NfseNacionalProperties.Formato.POS_REFORMA.posReforma());
        assertFalse(NfseNacionalProperties.Formato.PRE_REFORMA.posReforma());
    }

    @Test
    @DisplayName("formato desconhecido falha alto, em vez de cair no padrao")
    void formatoDesconhecidoFalhaAlto() {
        // Formato errado em silencio produz nota sem IBS/CBS e sem recusa do
        // servidor. Falhar na configuracao e' o unico lugar onde ainda se pode
        // ver.
        assertThrows(IllegalArgumentException.class,
                () -> NfseNacionalProperties.Formato.de("V2_REFORMA"));
        assertThrows(IllegalArgumentException.class,
                () -> NfseNacionalProperties.Formato.de("1.01"));
        // e os apelidos que o dono usaria
        assertEquals(NfseNacionalProperties.Formato.PRE_REFORMA,
                NfseNacionalProperties.Formato.de("PRE"));
        assertEquals(NfseNacionalProperties.Formato.POS_REFORMA,
                NfseNacionalProperties.Formato.de("depois"));
        assertEquals(NfseNacionalProperties.Formato.PRE_REFORMA,
                NfseNacionalProperties.Formato.de("1.00"));
    }

    @Test
    @DisplayName("a validacao local fica desligada por padrao, e o motivo esta no codigo")
    void validacaoDesligadaPorPadrao() {
        // O unico XSD e' o 1.00, do formato ANTES. Validar o formato novo contra
        // ele rejeita nota valida: "Element 'CodigoNBS': This element is not
        // expected."
        assertFalse(new NfseNacionalProperties().isValidar());
    }

    @Test
    @DisplayName("tomador no exterior usa EnderecoExterior, nao Endereco")
    void tomadorNoExterior() {
        var base = comOsDois();

        var exterior = new NfseNacionalDeclaracaoBuilder.DadosDeclaracao(
                base.unidadeGestora(), base.prestador(), base.rps(), base.nfseSubstituida(),
                new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.DadosTomador(null, null, null, null, "Gustavo Luis Picca", "2", true,
                        null, "Sesame Street, 192", null, null, null,
                        null, null, null, null, "2496", "Alabama", "Montgomery",
                        null, "gpicca2018@gmail.com", null),
                base.intermediario(), base.materialUsado(), base.regime(),
                new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.AtividadeEconomica("62040.00", null, null),
                base.incidencia(), base.municipioPrestacao(), base.valores(),
                base.servico(), base.beneficioProcesso(), base.cartaCorrecao(), base.reforma());

        String xml = builder("PRE_REFORMA").montar(exterior);
        assertTrue(xml.contains("<LocalEndereco>2</LocalEndereco>"), xml);
        assertTrue(xml.contains("<EnderecoExterior>"), "endereco de exterior: " + xml);
        assertTrue(xml.contains("<CodigoPaisBacen>2496</CodigoPaisBacen>"), xml);
        assertTrue(xml.contains("<NomeEstado>Alabama</NomeEstado>"), xml);
        assertTrue(xml.contains("<NomeMunicipio>Montgomery</NomeMunicipio>"), xml);
        assertFalse(xml.contains("<Endereco>"), "e nao Endereco de pais: " + xml);
    }

    @Test
    @DisplayName("material usado emite obra, notas e itens, na ordem do XSD")
    void materialUsado() {
        var base = comOsDois();

        var material = new NfseNacionalDeclaracaoBuilder.DadosDeclaracao(
                base.unidadeGestora(), base.prestador(), base.rps(), base.nfseSubstituida(),
                base.tomador(), base.intermediario(),
                new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.MaterialUsado("OBRA1", "ART1", List.of(
                        new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.NotaUsada(null, "14375732000170", "FORNECEDOR", "123", null,
                                List.of(new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.ItemUsado("1", "Cimento", "1", "Kg",
                                        bd("10"), bd("50.00")))))),
                base.regime(),
                new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.AtividadeEconomica("62040.00", null, null),
                base.incidencia(),
                base.municipioPrestacao(), base.valores(), base.servico(),
                base.beneficioProcesso(), base.cartaCorrecao(), base.reforma());

        String xml = builder("PRE_REFORMA").montar(material);
        assertTrue(xml.contains("<CodigoObra>OBRA1</CodigoObra>"), xml);
        assertTrue(xml.contains("<Art>ART1</Art>"), xml);
        assertTrue(xml.contains("<NomeFornecedor>FORNECEDOR</NomeFornecedor>"), xml);
        assertTrue(xml.contains("<DescricaoItemUsado>Cimento</DescricaoItemUsado>"), xml);
        assertTrue(xml.contains("<ValorTotal>50.00</ValorTotal>"), xml);
    }

    @Test
    @DisplayName("o intermediaria, quando existe, e' o responsavel pelo ISSQN")
    void intermediario() {
        var base = comOsDois();

        var comInterm = new NfseNacionalDeclaracaoBuilder.DadosDeclaracao(
                base.unidadeGestora(), base.prestador(), base.rps(), base.nfseSubstituida(),
                base.tomador(),
                new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.DadosIntermediario(null, "14375732000170", "123", "CONTABIL LTDA"),
                base.materialUsado(), base.regime(),
                new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.AtividadeEconomica("62040.00", null, null),
                base.incidencia(),
                base.municipioPrestacao(), base.valores(), base.servico(),
                base.beneficioProcesso(), base.cartaCorrecao(), base.reforma());

        String xml = builder("PRE_REFORMA").montar(comInterm);
        assertTrue(xml.contains("<DadosIntermediario>"), xml);
        assertTrue(xml.contains("<Cnpj>14375732000170</Cnpj>"), xml);
        assertTrue(xml.contains("<RazaoSocial>CONTABIL LTDA</RazaoSocial>"), xml);
    }
}
