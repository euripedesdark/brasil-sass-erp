package br.com.brasil_saas.fiscal.sp;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Gera o XML assinado de verdade, com o certificado A1, e grava em disco para
 * comparar com o que a gem Ruby produz.
 *
 * <p>E o teste que substitui a tentativa cega: as duas implementacoes tem de
 * gerar o mesmo XML, senao a prefeitura aceita uma e recusa a outra.
 *
 * <p>Precisa do certificado. Aponta o caminho e a senha por variavel de
 * ambiente; sem elas o teste e pulado, e nao falha.
 *
 * <pre>
 * NFSE_SP_CERT_PATH=/caminho/cert.pfx NFSE_SP_CERT_PASS=... mvn test -Dtest=NfseSpXmlAssinadoDump
 * </pre>
 */
class NfseSpXmlAssinadoDumpTest {

    @Test
    void geraXmlAssinadoParaCompararComOGemRuby() throws Exception {
        String caminho = System.getenv("NFSE_SP_CERT_PATH");
        String senha = System.getenv("NFSE_SP_CERT_PASS");
        Assumptions.assumeTrue(caminho != null && !caminho.isBlank(),
                "Defina NFSE_SP_CERT_PATH para gerar o XML assinado");

        var props = new NfseSpProperties();
        props.setXsdVersion(1);
        props.setCnpjRemetente("00000000000191");

        var certService = new NfseSpCertificadoService(props);
        var signer = new NfseSpSigner(certService);
        var builder = new NfseSpXmlBuilder(props);

        var par = certService.carregarDoCaminho(caminho, senha);

        // ---- RPS (mesmos dados do teste emitido contra a prefeitura) ----
        var rps = NfseSpXmlBuilder.Rps.minimo(
                "00000000000191",
                "2130033", "BC", "1",
                "2026-09-25",
                "0101", "0.029",     // codigo de servico e aliquota
                "1.00", "0.00",      // valor dos servicos e deducoes
                "86946749120", true, // CPF do tomador
                "EURIPEDES BATISTA DE PAIVA JUNIOR",
                "euripededark@gmail.com",
                "TESTE DE INTEGRACAO - NOTA DE R$ 1,00 A SER CANCELADA",
                "1");

        String assinaturaRps = signer.assinarRps(
                new NfseSpSigner.DadosAssinaturaRps(
                        rps.imPrestador(), rps.serieRps(), rps.numeroRps(),
                        rps.dataEmissao(), rps.tributacaoRps(), rps.statusRps(),
                        rps.issRetido(), rps.valorServicos(), null, rps.valorDeducoes(),
                        rps.codigoServico(), "86946749120", true, null, false, false, false),
                par.chavePrivada());

        String xmlRps = builder.envioRps(rps, assinaturaRps);
        String assinadoRps = signer.assinarXml(xmlRps, par.chavePrivada(), par.certificado());
        // o envelope so existe depois da assinatura
        String envelopeRps = builder.enveloparEnvioRps(assinadoRps);

        Path destino = Path.of("/tmp/opencode/comparacao");
        Files.createDirectories(destino);
        Files.writeString(destino.resolve("java-rps.xml"), envelopeRps);
        // e tambem a mensagem nua, que e o que se compara com o Ruby
        Files.writeString(destino.resolve("java-rps-mensagem.xml"), assinadoRps);

        // ---- Consulta de CNPJ ----
        String xmlCnpj = builder.consultaCnpj("00000000000191", "00000000000191");
        String assinadoCnpj = signer.assinarXml(xmlCnpj, par.chavePrivada(), par.certificado());
        Files.writeString(destino.resolve("java-consulta-cnpj.xml"),
                builder.enveloparConsultaCnpj(assinadoCnpj));
        Files.writeString(destino.resolve("java-consulta-cnpj-mensagem.xml"), assinadoCnpj);

        assertTrue(assinadoRps.contains("SignatureValue"), "o XML tem de sair assinado");
        assertTrue(assinadoRps.contains("<Assinatura>"), "o RPS tem de trazer o campo Assinatura");
        System.out.println("XML do RPS gravado em " + destino.resolve("java-rps.xml")
                + " (" + assinadoRps.length() + " bytes)");
        System.out.println("cadeia assinada: " + assinaturaRps.substring(0, Math.min(60, assinaturaRps.length())) + "...");
    }
}
