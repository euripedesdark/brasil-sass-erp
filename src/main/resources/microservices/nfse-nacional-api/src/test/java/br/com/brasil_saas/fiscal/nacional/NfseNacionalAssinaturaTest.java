package br.com.brasil_saas.fiscal.nacional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A declaracao assinada, do jeito que a prefeitura recebe.
 *
 * <p>Este teste <b>escreve o XML em disco</b> e o valida no XSD. E' o unico jeito
 * de conferir contra a prefeitura sem subir a API: o arquivo sai pronto para
 * o {@code curl}.
 *
 * <p><b>Por que o XML gerado precisa passar no XSD 1.00 no formato
 * {@code PRE_REFORMA}.</b> O XSD 1.00 e' do formato antes da reforma, e o
 * pos-reforma nao cabe nele. Mas a assinatura e' a mesma nos dois formatos e no
 * mesmo lugar — dentro de {@code IdentificacaoPrestador} — entao a assinatura e
 * verificada aqui vale para os dois.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "nfse.nacional.unidade-gestora=03347101000121",
        "nfse.nacional.cnpj-prestador=00000000000191",
        "nfse.nacional.formato=PRE_REFORMA",
        "nfse.nacional.homologacao=true",
        // <b>A ChaveDigital e' explicitamente vazia aqui.</b> Este teste existe
        // para verificar a assinatura; com a ChaveDigital o XSD aceita uma ou
        // outra, e o signer recusa — o que e' o outro teste, nao este.
        "nfse.nacional.chave-digital=",
        // <b>O certificado vem da application.yml, e nao de uma fonte paralela.</b>
        // O arquivo /etc/brasil-saas/cert.env e' 600 root: um teste que roda
        // como o usuario do projeto nao o le. E o teste tem que usar o mesmo
        // material que a aplicacao usa — se usasse outro, passaria com um
        // certificado e a emissao falharia com outro.
})
class NfseNacionalAssinaturaTest {

    @Autowired
    private NfseNacionalProperties props;

    @Autowired
    private NfseNacionalDeclaracaoBuilder builder;

    @Autowired
    private NfseNacionalCertificadoService certificado;

    @Autowired
    private NfseNacionalSigner signer;

    @Autowired
    private NfseNacionalValidadorXsd validador;

    /** Um corpo minimo no formato de antes, que o XSD 1.00 consegue validar. */
    private static final String CORPO = """
            {
              "discriminacao": "Consultoria em tecnologia da informacao",
              "itemLei116": "1.06",
              "codigoAtividadeEconomica": "62040.00",
              "codigoMunicipioIncidencia": "5107602",
              "descricaoMunicipioIncidencia": "RONDONOPOLIS",
              "ufIncidencia": "MT",
              "aliquota": "5.00",
              "valorServicos": "1000.00",
              "valorIssqnCalculado": "50.00",
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
              "telefone": "64999999999",
              "email": "cliente@exemplo.com.br",
              "quantidade": "1",
              "valorServico": "1000.00",
              "numeroRps": "1",
              "serieRps": "NFSe",
              "tipoRps": "-2",
              "descricaoRps": "Nota de teste em homologacao",
              "dataEmissaoRps": "2026-09-27"
            }
            """;

    /**
     * Zera a ChaveDigital antes de cada teste.
     *
     * <p><b>As properties sao um bean singleton do container Spring.</b> O teste
     * {@link #aChaveDigitalEAlternativaASignature} seta a chave para provar que a
     * assinatura e' alternativa; se ele rodar antes, os outros tres veem a chave
     * e falham com "A declaracao ja tem ChaveDigital" — sem ter setado nada. A
     * ordem dos testes nao e' garantida, entao isolar no inicio e' o unico jeito
     * confiavel.
     */
    @BeforeEach
    void semChaveDigital() {
        props.setChaveDigital("");
    }

    @Test
    void aDeclaracaoAssinadaValidaNoXsd() throws Exception {
        var req = new ObjectMapper().readValue(
                CORPO, NfseNacionalController.EmissaoRequest.class);

        String xml = builder.montar(req.paraDados(props));
        var par = certificado.carregar();
        String assinado = signer.assinarDeclaracao(xml,
                par.chavePrivada(), par.certificado());

        // o arquivo, para conferir contra a prefeitura
        Path destino = Path.of("/tmp/opencode/declaracao-assinada.xml");
        Files.writeString(destino, assinado);
        System.out.println("XML assinado em: " + destino.toAbsolutePath());

        // A assinatura tem que estar DENTRO de IdentificacaoPrestador, e nao na
        // raiz: e' ali que o xsd:choice a declara.
        int inicioPrest = assinado.indexOf("<IdentificacaoPrestador");
        int fimPrest = assinado.indexOf("</IdentificacaoPrestador>");
        int assinatura = assinado.indexOf("Signature");
        assertTrue(inicioPrest >= 0 && fimPrest > inicioPrest, "o bloco existe");
        assertTrue(assinatura > inicioPrest && assinatura < fimPrest,
                "a Signature tem que estar dentro de IdentificacaoPrestador: "
                        + "inicio=" + inicioPrest + " assinatura=" + assinatura
                        + " fim=" + fimPrest);

        // e o XML inteiro tem que passar no XSD
        var erros = validador.validar(assinado);
        assertTrue(erros.isEmpty(), "a declaracao assinada tem que passar no XSD:\n"
                + String.join("\n", erros));
    }

    @Test
    void aChaveDigitalEAlternativaASignature() throws Exception {
        // O XSD e' um xsd:choice. Com a ChaveDigital, a assinatura nao pode ser
        // acrescentada — e o que NfseNacionalSigner recusa.
        props.setChaveDigital("e3573c9c92992c02042b3b3726916b36");
        var req = new ObjectMapper().readValue(
                CORPO, NfseNacionalController.EmissaoRequest.class);
        String xml = builder.montar(req.paraDados(props));
        var par = certificado.carregar();

        NfseNacionalException e = org.junit.jupiter.api.Assertions.assertThrows(
                NfseNacionalException.class,
                () -> signer.assinarDeclaracao(xml, par.chavePrivada(), par.certificado()));
        assertTrue(e.getMessage().contains("uma ou outra"),
                "a recusa tem que explicar o choice: " + e.getMessage());
    }

    @Test
    void aAssinaturaUsaSHA256EExclusive() throws Exception {
        // A diferenca que custaria a nota: canonilizacao exclusiva produz digest
        // diferente da inclusiva sobre o mesmo conteudo. A AGILIBlue espera
        // exclusiva, que e' o mesmo padrao da Prefeitura de Sao Paulo.
        var req = new ObjectMapper().readValue(
                CORPO, NfseNacionalController.EmissaoRequest.class);
        var par = certificado.carregar();
        String assinado = signer.assinarDeclaracao(builder.montar(req.paraDados(props)),
                par.chavePrivada(), par.certificado());

        // <b>Exclusiva, e nao inclusiva.</b> Sobre o mesmo conteudo as duas
        // produzem digests diferentes, entao a assinatura passa na validacao
        // local e e' recusada pela prefeitura.
        //
        // <p>A URI da exclusiva e' http://www.w3.org/2001/10/xml-exc-c14n#, e a
        // inclusiva e' http://www.w3.org/TR/2001/REC-xml-c14n-20010315. O
        // provider tambem pode emitir a forma curta que o XMLDSig define, e
        // ai aparece 'xmldsig' no nome do algoritmo de ASSINATURA, nao da
        // canonilizacao — as duas coisas sao independentes.
        assertTrue(assinado.contains("xml-exc-c14n"),
                "canonilizacao exclusiva, nao inclusiva: " + assinado);
        assertTrue(!assinado.contains("REC-xml-c14n-20010315"),
                "a inclusiva nao pode aparecer: " + assinado);
        // rsa-sha256: o URI do XMLDSig 1.1, que e' o mesmo algoritmo
        assertTrue(assinado.contains("rsa-sha256"), "assinatura em RSA-SHA256");
    }
}
