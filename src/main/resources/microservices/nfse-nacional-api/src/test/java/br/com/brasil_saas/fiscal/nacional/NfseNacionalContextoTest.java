package br.com.brasil_saas.fiscal.nacional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * O contexto sobe, a configuracao chega, e o JSON do ERP monta a declaracao.
 *
 * <p><b>Por que este teste existe.</b> Todos os outros sao de unidade: chamam o
 * builder, o mapeamento e o validador direto. Nenhum sobe o container. Por isso
 * {@code NfseNacionalRegras}, que nao era bean, passou em 42 testes e derrubou
 * a aplicacao no primeiro start:
 *
 * <pre>
 *   Parameter 1 of constructor in NfseNacionalController required a bean of
 *   type 'NfseNacionalRegras' that could not be found.
 * </pre>
 *
 * <p>Teste unitario nao acha bean faltando. Subir o container acha.
 *
 * <p><b>E o request vem de JSON, nao de argumentos.</b> O record de emissao tem
 * uns 70 campos posicionais, e montar ele chamando o construtor e' a forma de
 * passar dois valores para o campo errado sem o compilador reclamar — a mesma
 * classe de bug que o aviso do AGILIBlue sobre o mapeamento de/para aponta. Ler
 * de JSON testa o contrato como o ERP manda, que e' o que importa.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "nfse.nacional.unidade-gestora=03347101000121",
        "nfse.nacional.cnpj-prestador=14375732000170",
        "nfse.nacional.inscricao-municipal-prestador=2033052",
        // <b>Sem ChaveDigital aqui.</b> A autenticacao deste contexto e' o A1 da
        // application.yml, e o XSD e' um xsd:choice: configurar os dois e'
        // recusado. O teste da alternativa esta em NfseNacionalAssinaturaTest.
        "nfse.nacional.chave-digital=",
        "nfse.nacional.homologacao=true",
})
class NfseNacionalContextoTest {

    /** Um corpo minimo, no formato que o ERP envia. */
    private static final String CORPO = """
            {
              "discriminacao": "Consultoria em tecnologia da informacao",
              "itemLei116": "1.06",
              "codigoCnae": "62040.00",
              "itemLei116AtividadeEconomica": "070901",
              "codigoNbs": "124033200",
              "codigoMunicipioIncidencia": "5107602",
              "descricaoMunicipioIncidencia": "RONDONOPOLIS",
              "ufIncidencia": "MT",
              "codigoMunicipioPrestacao": "5103403",
              "descricaoMunicipioPrestacao": "CUIABA",
              "ufPrestacao": "MT",
              "aliquota": "5.00",
              "valorServicos": "1000.00",
              "valorIssqnCalculado": "50.00",
              "valorIssqnRecolher": "0",
              "valorLiquido": "1000.00",
              "exigibilidadeIssqn": "-1",
              "descricaoExigibilidade": "Exigivel",
              "responsavelIssqn": "-3",
              "descricaoResponsavelIssqn": "Prestador do servico",
              "cnpjTomador": "77257895000179",
              "nomeTomador": "CLIENTE LTDA",
              "tipoLogradouro": "Rua",
              "logradouro": "Bandeira Azul",
              "numero": "737",
              "bairro": "Jardim Vitoria",
              "codigoMunicipioTomador": "5107602",
              "descricaoMunicipioTomador": "RONDONOPOLIS",
              "ufTomador": "MT",
              "cep": "79680000",
              "quantidade": "1",
              "valorServico": "1000.00",
              "codigoSituacaoTributaria": "-2",
              "codigoTipoRetencao": "-1"
            }
            """;

    @Autowired
    private NfseNacionalProperties props;

    @Autowired
    private NfseNacionalRegras regras;

    @Autowired
    private NfseNacionalService service;

    @Autowired
    private NfseNacionalDeclaracaoBuilder builder;

    @Autowired
    private NfseNacionalController controller;

    @Test
    void oContextoSobeETodosOsBeansExistem() {
        assertNotNull(props);
        assertNotNull(regras);
        assertNotNull(service);
        assertNotNull(builder);
        assertNotNull(controller);
    }

    @Test
    void aConfiguracaoChegaNoContainer() {
        // A properties e' lida de nfse.nacional.*. Se a
        // @ConfigurationPropertiesScan sumir da aplicacao, isto fica null e a
        // emissao quebra em producao sem nenhum teste unitario ver.
        assertTrue(props.isHomologacao());
        assertTrue(props.formato().posReforma());
        assertNotNull(props.getUnidadeGestora());
        assertNotNull(props.getCnpjPrestador());
    }

    @Test
    void oJsonDoErpMontaADeclaracao() throws Exception {
        NfseNacionalController.EmissaoRequest req =
                new ObjectMapper().readValue(CORPO, NfseNacionalController.EmissaoRequest.class);
        String xml = builder.montar(req.paraDados(props));

        // O que o ERP manda tem de chegar no XML, com o valor no campo certo.
        assertTrue(xml.contains("<UnidadeGestora>03347101000121</UnidadeGestora>"), xml);
        assertTrue(xml.contains("<Cnpj>14375732000170</Cnpj>"), xml);
        assertTrue(xml.contains("<Discriminacao>Consultoria em tecnologia da informacao"
                + "</Discriminacao>"), xml);
        assertTrue(xml.contains("<ValorServicos>1000.00</ValorServicos>"), xml);
        // o formato novo: a atividade pelo codigo da LC 116 e o NBS ao lado
        assertTrue(xml.contains("<ItemLei116AtividadeEconomica>070901"
                + "</ItemLei116AtividadeEconomica>"), xml);
        assertTrue(xml.contains("<CodigoNBS>124033200</CodigoNBS>"), xml);
        assertTrue(xml.contains("<PisCofins>"), xml);
        assertTrue(xml.contains("<CodigoSituacaoTributaria>-2</CodigoSituacaoTributaria>"),
                xml);
        assertTrue(xml.contains("<MunicipioPrestacaoServico>"), xml);
        // e o que e' do formato antigo nao aparece
        assertTrue(!xml.contains("CodigoAtividadeEconomica>62040.00"),
                "no formato novo a tag antiga nao sai: " + xml);
        assertTrue(!xml.contains("<ItemLei116>"), "o ItemLei116 sai do item: " + xml);
    }

    @Test
    void oHealthApontaOQueFaltaParaEmitir() {
        // O health e' o que o nfse-failover consulta, e a unica chance de ver a
        // configuracao errada antes de chamar a prefeitura.
        var resposta = controller.status();
        assertNotNull(resposta);
        // com os dados de teste ele avisa que a validacao local esta desligada
        assertTrue(resposta.getStatusCode().is2xxSuccessful(),
                "o health tem de responder 2xx para o proxy aceitar: "
                        + resposta.getStatusCode());
    }

    @Test
    void aValidacaoLocalPegaAAtividadeDupla() throws Exception {
        NfseNacionalController.EmissaoRequest req =
                new ObjectMapper().readValue(CORPO, NfseNacionalController.EmissaoRequest.class);

        // As duas atividades ao mesmo tempo sao o erro de de/para: sao
        // alternativas no XSD, e a prefeitura parametriza uma so.
        String comAsDuas = CORPO.replace(
                "\"itemLei116AtividadeEconomica\": \"070901\"",
                "\"itemLei116AtividadeEconomica\": \"070901\","
                        + "\n  \"codigoAtividadeEconomica\": \"62040.00\"");
        NfseNacionalController.EmissaoRequest ambiguo = new ObjectMapper()
                .readValue(comAsDuas, NfseNacionalController.EmissaoRequest.class);

        var problemas = regras.validar(ambiguo, props);
        assertTrue(problemas.stream().anyMatch(p -> p.contains("Atividade economica")),
                "a validacao tem que apontar a atividade ambigua: " + problemas);
        assertTrue(problemas.stream().anyMatch(p -> p.contains("alternativas")),
                "e dizer que sao alternativas: " + problemas);

        assertTrue(regras.validar(req, props).isEmpty(),
                "sem duplicidade a validacao tem que passar: "
                        + regras.validar(req, props));
    }
}
