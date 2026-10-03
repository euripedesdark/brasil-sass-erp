package br.com.brasil_saas.fiscal.nacional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
 * O formato <b>antes</b> da reforma, validado contra o XSD 1.00.
 *
 * <p>Este e' o unico formato que a validacao local cobre, porque o
 * {@code nfse-v-100.xsd} e' dele. O formato depois da reforma esta em
 * {@link NfseNacionalFormatoTest}, que nao valida contra XSD porque nao ha
 * schema para ele.
 *
 * <p><b>Sobre os exemplos do material.</b> O {@code GerarNfseEnvio - Normal.xml}
 * tem {@code encoding="utf-8"} mas foi salvo com encoding misto — a maioria dos
 * acentos em UTF-8, alguns em Latin-1. O {@code Simei.xml} ainda tem BOM e
 * declara {@code encoding="1.0"}, que e' um nome de encoding e nao um encoding.
 * Os dois dao {@code Content is not allowed in prolog}. {@link #corrigir} repara
 * isso so para o teste; a API emite em UTF-8 de verdade, e
 * {@link #saidaEmUtf8()} verifica.
 */
class NfseNacionalDeclaracaoBuilderTest {

    private NfseNacionalProperties props;
    private NfseNacionalDeclaracaoBuilder builder;
    private NfseNacionalValidadorXsd validador;

    @BeforeEach
    void antes() {
        props = new NfseNacionalProperties();
        props.setUnidadeGestora("43212247000184");
        props.setCnpjPrestador("12530068000161");
        props.setInscricaoMunicipalPrestador("110863");
        // O xsd:choice de IdentificacaoPrestador tem minOccurs 1: sem
        // ChaveDigital nem assinatura, o XSD recusa na segunda tag.
        props.setChaveDigital("e3573c9c92992c02042b3b3726916b36");
        // o XSD 1.00 descreve o formato de ANTES da reforma
        props.setFormato("PRE_REFORMA");
        builder = new NfseNacionalDeclaracaoBuilder(
                props, new NfseNacionalMapeamentoTributacao());
        validador = new NfseNacionalValidadorXsd();
    }

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }

    /**
     * O perfil "Normal" do material, no agrupamento do XSD.
     *
     * <p>Um unico lugar monta os dados, de proposito: com o record having dozens
     * of positional fields, scattering the construction across tests is how a
     * value ends up in the wrong field without the compiler noticing. That is
     * the de/para bug the AGILIBlue notice warns about.
     */
    private NfseNacionalDeclaracaoBuilder.DadosDeclaracao normal() {
        return new NfseNacionalDeclaracaoBuilder.DadosDeclaracao(
                props.getUnidadeGestora(),

                new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.IdentificacaoPrestador(props.getChaveDigital(),
                        props.getCnpjPrestador(), null,
                        props.getInscricaoMunicipalPrestador()),
                null, null,

                new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.DadosTomador(
                        "77257895000179", null, null, null,
                        "CIA DE SERVICOS DOMESTICOS DI MARIA LTDA", "1", false,
                        "Rua", "Bandeira Azul", "737", "Casa", "Jardim Vitoria",
                        "5000203", "Agua Clara", "MS", "79680000", null, null, null,
                        "88888888", "cia.servico@dom.com.br", null),
                null, null,

                new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.Regime("-2", "Nenhum", false, false, false,
                        "-3", "Prestador do servico", "62040.00", "-1", "Exigivel"),

                new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.AtividadeEconomica("62040.00", null, null),
                new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.Incidencia("5000203", "Agua Clara", "MS"),
                null,

                new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.Valores(
                        bd("1000.00"), bd("0"), bd("1000.00"), bd("5.0"),
                        bd("50.00"), bd("0"), null, null, bd("0"), bd("1000.00"),
                        null, null, null, null, null,
                        bd("0"), bd("0"), bd("0"),
                        "Gerado via WebService", null),

                new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.ItemServico("Descricao do servico prestado", "62040.00", "1.06",
                        bd("1"), bd("1000.00"), bd("0")),
                null, null,
                new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.Reforma(null, null, null));
    }

    // ------------------------------------------------------------------
    // o XSD
    // ------------------------------------------------------------------

    @Test
    @DisplayName("a declaracao gerada passa no XSD 1.00")
    void passaNoXsd() {
        List<String> erros = validador.validar(builder.montar(normal()));
        assertTrue(erros.isEmpty(), "a declaracao deveria passar no XSD, e nao passou:\n"
                + String.join("\n", erros));
    }

    @Test
    @DisplayName("a ordem das tags dentro da declaracao e' a do XSD")
    void ordemDasTags() throws Exception {
        List<String> ordem = tagsNivel0(builder.montar(normal()));
        assertEquals(List.of(
                "IdentificacaoPrestador", "DadosTomador", "RegimeEspecialTributacao",
                "OptanteSimplesNacional", "OptanteMEISimei", "ISSQNRetido",
                "ResponsavelISSQN", "CodigoAtividadeEconomica", "ExigibilidadeISSQN",
                "MunicipioIncidencia", "ValorServicos", "ValorDescontos", "ValorPis",
                "ValorCofins", "ValorInss", "ValorIrrf", "ValorCsll",
                "ValorOutrasRetencoes", "ValorBaseCalculoISSQN", "AliquotaISSQN",
                "ValorISSQNCalculado", "ValorISSQNRecolher", "ValorDeducaoConstCivil",
                "ValorLiquido", "Observacao", "Complemento", "ListaServico", "Versao"),
                ordem);
    }

    @Test
    @DisplayName("o xsd:choice de autenticacao e' obrigatorio")
    void choiceDeAutenticacaoEObrigatorio() {
        props.setChaveDigital(null);
        List<String> erros = validador.validar(builder.montar(normal()));
        assertFalse(erros.isEmpty(),
                "sem ChaveDigital e sem assinatura, o XSD tem de recusar");
        assertTrue(erros.stream().anyMatch(e -> e.contains("ChaveDigital")
                        || e.contains("Signature") || e.contains("CpfCnpj")),
                "a recusa tem de apontar a escolha do choice: " + erros);
    }

    @Test
    @DisplayName("o schema da assinatura e' o da Prefeitura, com o nome dela")
    void schemaDaAssinaturaEODaPrefeitura() throws IOException {
        // A prefeitura distribui o par: nfse-v-100.xsd, que referencia
        // dsig:Signature, e webapi-nfse-signature.xsd, que e' o schema daquele
        // namespace. O nome importa porque e' o que vai no schemaLocation.
        Path assinatura = Path.of("src/main/resources/schemas/webapi-nfse-signature.xsd");
        assertTrue(Files.exists(assinatura),
                "o schema da assinatura tem que estar com o nome da Prefeitura");

        String xsd = Files.readString(assinatura, StandardCharsets.UTF_8);
        assertTrue(xsd.contains("targetNamespace=\"http://www.w3.org/2000/09/xmldsig#\""),
                "o targetNamespace do XMLDSig");
        // o modulo vive em src/main/resources/microservices/nfse-nacional-api,
        // e o material em src/main/resources/db/seed/issqn: dois niveis acima
        assertTrue(Files.exists(Path.of("../../db/seed/issqn/webapi-nfse-signature.xsd")),
                "o schema da assinatura tambem no material do dono");
    }

    @Test
    @DisplayName("o XSD do dono nao declara o import, e por isso nao carrega sozinho")
    void xsdDoDonoNaoDeclaraOImport() throws IOException {
        // Confirmado nas duas copias do arquivo, byte a byte pelo sha256: a de
        // Downloads e a do material. Ambas tem o ref="dsig:Signature" e o
        // xmlns:dsig, e nenhuma tem o xsd:import.
        String xsd = Files.readString(
                Path.of("src/main/resources/schemas/nfse-v-100.xsd"), StandardCharsets.UTF_8);

        assertTrue(xsd.contains("ref=\"dsig:Signature\""), "a referencia existe");
        assertTrue(xsd.contains("xmlns:dsig=\"http://www.w3.org/2000/09/xmldsig#\""),
                "o prefixo esta declarado");
        assertFalse(xsd.contains("schemaLocation"),
                "e mesmo assim o import nao esta declarado: e' o defeito do XSD "
                        + "entregue, e a correcao fica numa copia temporaria");
    }

    // ------------------------------------------------------------------
    // o conteudo
    // ------------------------------------------------------------------

    @Test
    @DisplayName("ItemLei116 e' 1.06, com ponto — nao 01.01.01.000 do banco")
    void itemDaLci16() {
        String xml = builder.montar(normal());
        assertTrue(xml.contains("<ItemLei116>1.06</ItemLei116>"),
                "ItemLei116 no formato do padrao nacional: " + xml);
        // bc_fis_issqn.codigo = 01.01.01.000 e' o codigo do servico no ERP, nao
        // o item da LC 116. Mandar o de banco aqui emite a nota errada.
        assertFalse(xml.contains("01.01.01.000"),
                "o codigo do banco nao vai em ItemLei116: " + xml);
    }

    @Test
    @DisplayName("CodigoAtividadeEconomica e' CNAE com ponto, como nos exemplos")
    void cnaeComPonto() {
        assertTrue(builder.montar(normal()).contains("<CodigoAtividadeEconomica>62040.00"));
    }

    @Test
    @DisplayName("o regime sai em tres tags separadas, nao em um campo so")
    void regimeEmTresTags() {
        String xml = builder.montar(normal());
        assertTrue(xml.contains("<OptanteSimplesNacional>0</OptanteSimplesNacional>"));
        assertTrue(xml.contains("<OptanteMEISimei>0</OptanteMEISimei>"));
        assertTrue(xml.contains("<ISSQNRetido>0</ISSQNRetido>"));
        assertTrue(xml.contains("<Codigo>-2</Codigo>"), "RegimeEspecialTributacao");
        assertTrue(xml.contains("<Codigo>-3</Codigo>"), "ResponsavelISSQN");
    }

    @Test
    @DisplayName("os decimais vao com ponto, nunca com virgula")
    void decimaisComPonto() {
        String xml = builder.montar(normal());
        assertTrue(xml.contains("<ValorServicos>1000.00</ValorServicos>"));
        assertTrue(xml.contains("<ValorISSQNCalculado>50.00</ValorISSQNCalculado>"));
        assertFalse(xml.matches("(?s).*<ValorServicos>[0-9]+,[0-9]+.*"), "virgula em decimal");
    }

    @Test
    @DisplayName("a saida e' UTF-8 de verdade, e nao Latin-1 disfarcado")
    void saidaEmUtf8() {
        String xml = builder.montar(normal());
        byte[] bytes = xml.getBytes(StandardCharsets.UTF_8);
        // se round-trippar, o conteudo e' UTF-8 valido
        assertEquals(xml, new String(bytes, StandardCharsets.UTF_8),
                "o XML tem de ser UTF-8 valido de ponta a ponta");
    }

    @Test
    @DisplayName("as 3 flags de regime sao independentes, e cada uma vira 0 ou 1")
    void flagsIndependentes() {
        var base = normal();
        String xml = builder.montar(new NfseNacionalDeclaracaoBuilder.DadosDeclaracao(
                base.unidadeGestora(), base.prestador(), base.rps(), base.nfseSubstituida(),
                base.tomador(), base.intermediario(), base.materialUsado(),
                new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.Regime(null, null, true, true, false, null, null, "62040.00", null, null),
                base.atividade(), base.incidencia(), base.municipioPrestacao(),
                base.valores(), base.servico(), base.beneficioProcesso(),
                base.cartaCorrecao(), base.reforma()));

        assertTrue(xml.contains("<OptanteSimplesNacional>1</OptanteSimplesNacional>"));
        assertTrue(xml.contains("<OptanteMEISimei>1</OptanteMEISimei>"),
                "simples e MEI sao flags separadas, e as duas podem ser 1");
        assertTrue(xml.contains("<ISSQNRetido>0</ISSQNRetido>"));
    }

    @Test
    @DisplayName("a retencao federal sai agrupada em ValorCsll, e o ISSQN nao e' tocado")
    void agrupamentoFederalNoXml() {
        var base = normal();
        String xml = builder.montar(new NfseNacionalDeclaracaoBuilder.DadosDeclaracao(
                base.unidadeGestora(), base.prestador(), base.rps(), base.nfseSubstituida(),
                base.tomador(), base.intermediario(), base.materialUsado(), base.regime(),
                base.atividade(), base.incidencia(), base.municipioPrestacao(),
                new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.Valores(
                        bd("1000.00"), bd("0"), bd("1000.00"), bd("5.0"),
                        bd("50.00"), bd("0"), null, null, bd("0"), bd("1000.00"),
                        bd("10.00"), bd("20.00"), bd("5.00"),
                        bd("7.50"), bd("3.25"),
                        bd("0"), bd("0"), bd("0"), null, null),
                base.servico(), base.beneficioProcesso(), base.cartaCorrecao(),
                base.reforma()));

        assertTrue(xml.contains("<ValorCsll>35.00</ValorCsll>"), "10 + 20 + 5: " + xml);
        assertTrue(xml.contains("<ValorPis>7.50</ValorPis>"),
                "ValorPis recebe a reducao de base, nao a retencao: " + xml);
        assertTrue(xml.contains("<ValorCofins>3.25</ValorCofins>"), xml);
        // o ISSQN continua nos campos proprios, municipal
        assertTrue(xml.contains("<ValorBaseCalculoISSQN>1000.00</ValorBaseCalculoISSQN>"));
        assertTrue(xml.contains("<ValorISSQNCalculado>50.00</ValorISSQNCalculado>"));
        assertFalse(xml.contains("<ValorPis>10.00</ValorPis>"),
                "a retencao de PIS nao pode ir na tag de PIS: " + xml);
    }

    // ------------------------------------------------------------------
    // a URL e a homologacao
    // ------------------------------------------------------------------

    @Test
    @DisplayName("a URL e' a base mais o nome da operacao, como no exemplo da doc")
    void urlDasOperacoes() {
        NfseNacionalOperacoes op = new NfseNacionalOperacoes();
        assertEquals("https://nfse.rondonopolis.mt.gov.br/api/GerarNfse", op.urlGerarNfse());
        assertEquals("https://nfse.rondonopolis.mt.gov.br/api/CancelarNfse", op.urlCancelarNfse());
        assertEquals("https://nfse.rondonopolis.mt.gov.br/api/EnviarLoteRps", op.urlEnviarLoteRps());
        assertEquals("https://nfse.rondonopolis.mt.gov.br/api/SubstituirNfse", op.urlSubstituirNfse());
        assertEquals("https://nfse.rondonopolis.mt.gov.br/api/ConsultarNfseRps",
                op.urlConsultarNfseRps());
    }

    @Test
    @DisplayName("a homologacao e' parametro na request, nao outra URL")
    void homologacaoEParametro() {
        NfseNacionalOperacoes op = new NfseNacionalOperacoes();
        String comHomologacao = op.url("GerarNfse", true);
        String semHomologacao = op.url("GerarNfse", false);

        // mesma base: e' parametro, nao host diferente
        assertEquals(semHomologacao.split("\\?")[0], comHomologacao.split("\\?")[0],
                "homologacao nao muda o endereco, so acrescenta o parametro");
        assertTrue(comHomologacao.endsWith("?homologacao=true"),
                "o parametro da documentacao e' booleano: " + comHomologacao);
        assertEquals(semHomologacao, op.url("GerarNfse"),
                "sem o parametro, a prefeitura GRAVA a nota");
    }

    @Test
    @DisplayName("a barra duplicada da base da documentacao e' normalizada")
    void barraDuplicada() {
        NfseNacionalOperacoes comBarra = new NfseNacionalOperacoes(
                "https://nfse.rondonopolis.mt.gov.br//api/");
        assertEquals("https://nfse.rondonopolis.mt.gov.br/api/GerarNfse",
                comBarra.urlGerarNfse(),
                "a base da documentacao tem barra dupla; a URL nao deve herdar isso");
    }

    // ------------------------------------------------------------------
    // os exemplos do material
    // ------------------------------------------------------------------

    @Test
    @DisplayName("nos 10 exemplos, o unico erro do XSD e' o -1 de RegimeEspecialTributacao")
    void exemplosDoMaterialPassamNoXsd() throws IOException {
        List<Path> arquivos = exemplos();
        assertFalse(arquivos.isEmpty(), "os exemplos do material nao estao em src/test/resources");

        java.util.Map<String, List<String>> falhas = new java.util.LinkedHashMap<>();
        java.util.Map<String, List<String>> outrosErros = new java.util.LinkedHashMap<>();

        for (Path p : arquivos) {
            String xml = corrigir(Files.readAllBytes(p));
            List<String> erros = validador.validar(xml);
            if (erros.isEmpty()) {
                continue;
            }
            List<String> conhecidos = new java.util.ArrayList<>();
            List<String> novos = new java.util.ArrayList<>();
            for (String e : erros) {
                // o Xerces redige de duas formas dependendo do caminho: por
                // padrao (cvc-pattern-valid) ou por tipo simples
                if (e.contains("tsRegimeEspecialTributacao")
                        || e.contains("of element 'Codigo' is not valid")) {
                    conhecidos.add(e);
                } else {
                    novos.add(e);
                }
            }
            if (!conhecidos.isEmpty()) {
                falhas.put(p.getFileName().toString(), conhecidos);
            }
            if (!novos.isEmpty()) {
                outrosErros.put(p.getFileName().toString(), novos);
            }
        }

        assertTrue(outrosErros.isEmpty(),
                "apareceu um erro de XSD que nao e' o -1 conhecido. Ou o schema mudou, "
                        + "ou o reparo de encoding parou de dar conta:\\n"
                        + outrosErros.entrySet().stream()
                        .map(e -> "  " + e.getKey() + "\\n    "
                                + String.join("\\n    ", e.getValue()))
                        .reduce("", (a, b) -> a + b));

        // Oito exemplos (todos menos Simei e SN ME EPP) falham pelo -1. E o
        // defeito e' do XSD, nao dos exemplos: a descricao dos oito e' "Nenhum",
        // e so dois usam valor que o schema aceita.
        assertEquals(8, falhas.size(),
                "oito exemplos com -1 em RegimeEspecialTributacao, que o XSD recusa. "
                        + "Se este numero mudou, a prefeitura corrigiu o schema e a "
                        + "pergunta sobre o -1 pode ser encerrada. Recusados:\\n"
                        + String.join("\\n", falhas.keySet()));
    }

    /**
     * Repara o que impede os exemplos do material de serem lidos por um parser.
     *
     * <p>Tres defeitos, e todos dao a mesma falha boba:
     *
     * <ol>
     *   <li><b>BOM UTF-8 nos bytes</b> ({@code EF BB BF}) em parte dos arquivos.
     *       Lido como Latin-1 vira tres caracteres, e nao sai com {@code strip()}.</li>
     *   <li><b>{@code encoding="1.0"}</b> no Simei.xml. E' um nome de encoding,
     *       nao um: o parser nao reconhece e aborta.</li>
     *   <li><b>Encoding misto no conteudo.</b> A maioria dos acentos esta em UTF-8
     *       e alguns em Latin-1, num arquivo que se declara UTF-8.</li>
     * </ol>
     *
     * <p>A estrategia: Latin-1 mapeia byte por byte sem perder nada, entao
     * decodificar por la e redecodificar como UTF-8 recupera as sequencias UTF-8
     * intactas. Os bytes Latin-1 orfaos viram replacement, o que serve aqui — o
     * que se verifica e' a estrutura contra o XSD, e nao a acentuacao.
     */
    private static String corrigir(byte[] bytes) {
        String emLatin1 = new String(bytes, StandardCharsets.ISO_8859_1);

        // BOM: lido em Latin-1, o BOM sao tres caracteres, e nao um
        while (emLatin1.startsWith("\\u00EF\\u00BB\\u00BF")) {
            emLatin1 = emLatin1.substring(3);
        }
        emLatin1 = emLatin1.strip();

        // Latin-1 -> bytes -> UTF-8: recupera o que era UTF-8 de verdade
        String txt = new String(emLatin1.getBytes(StandardCharsets.ISO_8859_1),
                StandardCharsets.UTF_8);

        // o encoding declarado precisa ser um nome de encoding valido
        if (txt.startsWith("<?xml")) {
            int fim = txt.indexOf('>');
            txt = corrigirEncodingDeclarado(txt.substring(0, fim + 1)) + txt.substring(fim + 1);
        }
        return txt;
    }

    /**
     * Reescreve o {@code encoding} da declaracao para {@code utf-8}.
     *
     * <p>Sem regex de proposito: o Simei.xml traz {@code encoding="1.0"}, e
     * qualquer classe de caracteres com aspa dentro de um literal Java vira tres
     * niveis de escape. A declaracao e' curta e a posicao e' conhecida.
     */
    private static String corrigirEncodingDeclarado(String decl) {
        final String chave = "encoding";
        int k = decl.indexOf(chave);
        if (k < 0) {
            return decl;
        }
        int igual = decl.indexOf('=', k);
        if (igual < 0) {
            return decl;
        }
        int abre = igual + 1;
        while (abre < decl.length() && Character.isWhitespace(decl.charAt(abre))) {
            abre++;
        }
        if (abre >= decl.length()) {
            return decl;
        }
        char aspa = decl.charAt(abre);
        if (aspa != '"' && aspa != APOS) {
            return decl;
        }
        int fecha = decl.indexOf(aspA(), abre + 1);
        if (fecha < 0) {
            return decl;
        }
        return decl.substring(0, abre + 1) + "utf-8" + aspa + decl.substring(fecha + 1);
    }

    /** A aspa simples, por constante: o literal com escape duplo e' facil de errar. */
    private static final char APOS = 0x27;

    private static char aspA() {
        return '"';
    }

    private static List<Path> exemplos() throws IOException {
        Path pasta = Path.of("src/test/resources/exemplos-agilblue");
        if (!Files.isDirectory(pasta)) {
            return List.of();
        }
        try (var s = Files.list(pasta)) {
            return s.filter(p -> p.toString().endsWith(".xml")).sorted().toList();
        }
    }

    // ------------------------------------------------------------------

    private static List<String> tagsNivel0(String xml) throws Exception {
        Document doc = NfseNacionalClient.parse(xml);
        NodeList nl = doc.getElementsByTagNameNS("*", "DeclaracaoPrestacaoServico");
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
}
