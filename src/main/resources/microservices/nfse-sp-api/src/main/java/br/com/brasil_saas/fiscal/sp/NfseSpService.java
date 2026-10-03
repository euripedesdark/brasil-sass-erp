package br.com.brasil_saas.fiscal.sp;

import br.com.brasil_saas.fiscal.sp.NfseSpRespostaParser.ChaveNfe;
import br.com.brasil_saas.fiscal.sp.NfseSpRespostaParser.DetalheCnpj;
import br.com.brasil_saas.fiscal.sp.NfseSpRespostaParser.Mensagem;
import br.com.brasil_saas.fiscal.sp.NfseSpRespostaParser.Resposta;
import br.com.brasil_saas.fiscal.sp.NfseSpSigner.DadosAssinaturaRps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * Orquestra uma operacao: monta, assina, envia e interpreta.
 *
 * <p>A ordem importa e nao e a ordem obvia:
 * <ol>
 *   <li>a <b>assinatura do RPS</b> sai da cadeia de 86 posicoes, que depende
 *       so dos campos do RPS — e calculada antes do XML;</li>
 *   <li>o XML e montado ja com a assinatura dentro;</li>
 *   <li>o XML inteiro e assinado no padrao XMLDSig, envelopado;</li>
 *   <li>so entao vai para a prefeitura.</li>
 * </ol>
 *
 * <p>As tres assinaturas/etapas usam algoritmos diferentes: SHA1withRSA para a
 * cadeia do RPS, XMLDSig com c14n exclusiva para a mensagem, e RSA-SHA1 do
 * certificado para o cancelamento. Misturar as regras produz o erro 1206, que
 * a prefeitura so explica ecoando a string que ela calculou.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NfseSpService {

    private final NfseSpProperties properties;
    private final NfseSpCertificadoService certificadoService;
    private final NfseSpSigner signer;
    private final NfseSpXmlBuilder builder;
    private final NfseSpClient client;
    private final NfseSpRespostaParser parser;

    // ------------------------------------------------------------------
    // Consulta de CNPJ
    // ------------------------------------------------------------------

    /**
     * Consulta quais Inscricoes Municipais estao vinculadas a um CNPJ e se
     * emitem NFS-e.
     *
     * <p>E a chamada mais barata para conferir o certificado: ela valida a
     * assinatura, o certificado e a comunicacao, sem emitir nada.
     */
    public ConsultaCnpjResponse consultarCnpj(String cnpj, String cnpjContribuinte) {
        exigirHabilitada();
        String alvo = cnpjContribuinte == null || cnpjContribuinte.isBlank() ? cnpj : cnpjContribuinte;

        var par = certificadoService.carregar();
        // a assinatura fica DENTRO do MensagemXML; o envelope vai por fora
        String xml = builder.consultaCnpj(cnpj, alvo);
        String assinado = signer.assinarXml(xml, par.chavePrivada(), par.certificado());

        String resposta = client.enviarPara(builder.enveloparConsultaCnpj(assinado), NfseSpOperacoes.CONSULTA_CNPJ);
        Resposta r = parser.parse(resposta, "consulta_cnpj_response");
        DetalheCnpj detalhe = parser.detalheCnpj(resposta);

        if (!r.sucesso()) {
            throw new NfseSpException("A prefeitura recusou a consulta de CNPJ: "
                    + juntar(r.erros()));
        }
        return new ConsultaCnpjResponse(
                detalhe == null ? null : detalhe.inscricaoMunicipal(),
                detalhe != null && detalhe.emiteNfe(),
                r.alertas());
    }

    public record ConsultaCnpjResponse(String inscricaoMunicipal, boolean emiteNfe,
                                       List<Mensagem> alertas) {
    }

    // ------------------------------------------------------------------
    // Emissao
    // ------------------------------------------------------------------

    public EmissaoResponse emitirRps(NfseSpXmlBuilder.Rps rps) {
        exigirHabilitada();
        var par = certificadoService.carregar();

        // 1) assinatura da cadeia de 86 posicoes
        String assinaturaRps = signer.assinarRps(dadosDaAssinatura(rps), par.chavePrivada());

        // 2) XML com a assinatura dentro
        String xml = builder.envioRps(rps, assinaturaRps);

        // 3) assinatura XMLDSig da MENSAGEM (nao do envelope SOAP)
        String assinado = signer.assinarXml(xml, par.chavePrivada(), par.certificado());

        // 4) embrulha e vai
        String resposta = client.enviarPara(builder.enveloparEnvioRps(assinado), NfseSpOperacoes.ENVIO_RPS);
        Resposta r = parser.parse(resposta, "envio_rps_response");

        if (!r.sucesso()) {
            throw new NfseSpException("A prefeitura recusou o RPS: " + juntar(r.erros()));
        }
        ChaveNfe chave = r.chaveNfe();
        if (chave == null) {
            throw new NfseSpException(
                    "A prefeitura confirmou a emissao mas nao devolveu a chave da NF-e. "
                            + "Verifique o XML de retorno antes de tentar de novo, para nao emitir em duplicidade.");
        }
        log.info("NFS-e {} emitida (RPS {}/{})", chave.numeroNfe(), rps.serieRps(), rps.numeroRps());
        // O XML assinado volta na resposta. Quem chama precisa arquivar em
        // silencio: ele tem o proprio banco de documentos, e o XML e a prova
        // da nota (prazo legal de 5 anos).
        return new EmissaoResponse(chave, r.alertas(), assinado);
    }

    public record EmissaoResponse(ChaveNfe chave, List<Mensagem> alertas, String xmlAssinado) {
    }

    // ------------------------------------------------------------------
    // Cancelamento
    // ------------------------------------------------------------------

    public CancelamentoResponse cancelar(List<CancelamentoRequest> pedidos) {
        exigirHabilitada();
        var par = certificadoService.carregar();

        var detalhes = pedidos.stream().map(p -> {
            String cadeia = builder.cadeiaCancelamento(p.inscricaoPrestador(), p.numeroNfe());
            String assinatura = signer.assinarTexto(cadeia, par.chavePrivada());
            return new NfseSpXmlBuilder.DetalheCancelamento(
                    p.inscricaoPrestador(), p.numeroNfe(),
                    p.codigoVerificacao(), p.chaveNotaNacional(), assinatura);
        }).toList();

        String xml = builder.cancelamento(properties.getCnpjRemetente(), detalhes);
        String assinado = signer.assinarXml(xml, par.chavePrivada(), par.certificado());

        String resposta = client.enviarPara(builder.enveloparCancelamento(assinado), NfseSpOperacoes.CANCELAMENTO_NFE);
        Resposta r = parser.parse(resposta, "cancelamento_n_fe_response");

        if (!r.sucesso()) {
            throw new NfseSpException("A prefeitura recusou o cancelamento: " + juntar(r.erros()));
        }
        log.info("NFS-e cancelada: {}", pedidos.stream().map(CancelamentoRequest::numeroNfe).toList());
        return new CancelamentoResponse(r.alertas());
    }

    public record CancelamentoResponse(List<Mensagem> alertas) {
    }

    public record CancelamentoRequest(String inscricaoPrestador, String numeroNfe,
                                      String codigoVerificacao, String chaveNotaNacional) {
    }

    // ------------------------------------------------------------------

    /**
     * Traduz o RPS para a cadeia de assinatura.
     *
     * <p>A posicao 8 da cadeia e o campo que muda de nome com o leiaute: no 1 e
     * Valor dos Servicos, no 2 e ValorFinalCobrado.
     */
    private DadosAssinaturaRps dadosDaAssinatura(NfseSpXmlBuilder.Rps rps) {
        boolean temCpf = rps.cpfTomador() != null && !rps.cpfTomador().isBlank();
        boolean temCnpj = rps.cnpjTomador() != null && !rps.cnpjTomador().isBlank();
        String documento = temCpf ? rps.cpfTomador() : (temCnpj ? rps.cnpjTomador() : null);

        boolean temIntermediarioCpf = rps.cpfIntermediario() != null && !rps.cpfIntermediario().isBlank();

        return new DadosAssinaturaRps(
                rps.imPrestador(),
                rps.serieRps(),
                rps.numeroRps(),
                rps.dataEmissao(),
                rps.tributacaoRps(),
                rps.statusRps(),
                rps.issRetido(),
                rps.valorServicos(),
                rps.valorFinalCobrado(),
                rps.valorDeducoes(),
                rps.codigoServico(),
                documento,
                temCpf,
                temIntermediarioCpf ? rps.cpfIntermediario() : rps.cnpjIntermediario(),
                temIntermediarioCpf,
                "true".equalsIgnoreCase(rps.issRetidoIntermediario()),
                properties.getXsdVersion() >= 2);
    }

    private void exigirHabilitada() {
        if (!properties.isEnabled()) {
            throw new NfseSpException(
                    "Integracao com a NFS-e de Sao Paulo desligada. "
                            + "Ligue em brasil-saas.fiscal.nfse-sp.enabled=true e configure o certificado A1.");
        }
    }

    private String juntar(List<Mensagem> mensagens) {
        if (mensagens == null || mensagens.isEmpty()) {
            return "a prefeitura nao informou o motivo";
        }
        return String.join("; ", mensagens.stream().map(Mensagem::toString).toList());
    }
}
