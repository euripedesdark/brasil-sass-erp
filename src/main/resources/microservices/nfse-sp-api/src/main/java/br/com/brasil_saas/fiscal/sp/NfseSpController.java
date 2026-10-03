package br.com.brasil_saas.fiscal.sp;

import br.com.brasil_saas.fiscal.sp.NfseSpRespostaParser.Mensagem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Endpoints da NFS-e de Sao Paulo.
 *
 * <p>Substitui a API do bridge Ruby, com os mesmos caminhos, para quem ja usa
 * o outro trocar a URL sem mudar o codigo.
 *
 * <p>Sobre autenticação: esta API assina nota fiscal. Antes de expor em rede,
 * coloque-a atrás de HTTPS com autenticacao — o padrão do Spring Security nao se
 * aplica aqui de proposito, porque esta e uma API de servico, nao um modulo do
 * ERP. Ver o README.
 */
@RestController
@RequestMapping("/api/nfse-sp")
@RequiredArgsConstructor
public class NfseSpController {

    private final NfseSpService service;
    private final NfseSpProperties properties;

    /**
     * Consulta os vinculos de um CNPJ e se ele emite NFS-e.
     *
     * <p>Use para validar certificado e comunicacao antes de emitir: nao cria
     * nada na prefeitura.
     */
    @PostMapping("/consulta-cnpj")
    public ResponseEntity<?> consultarCnpj(@RequestBody ConsultaCnpjRequest req) {
        var r = service.consultarCnpj(req.cnpj(), req.cnpjContribuinte());
        // Mesmo contrato das demais. A inscricao municipal vem em inscricao_municial
        // no Ruby e em inscricao_municipal aqui; o contrato padroniza o nome para
        // inscricao_municipal e o watchdog le sempre o mesmo.
        java.util.Map<String, Object> corpo = new java.util.LinkedHashMap<>(
                contrato_nfse(true, r.inscricaoMunicipal(), "", "", "", r.alertas(), "", ""));
        corpo.put("emite_nfse", r.emiteNfe());
        return ResponseEntity.ok(corpo);
    }

    /** Emite a NFS-e a partir de um RPS. */
    @PostMapping("/emitir-rps")
    public ResponseEntity<?> emitirRps(@RequestBody @Valid RpsRequest req) {
        var r = service.emitirRps(req.paraRps(properties.getCnpjRemetente()));
        var chave = r.chave();
        return ResponseEntity.ok(contrato_nfse(
                true,
                chave.inscricaoPrestador(),
                chave.numeroNfe(),
                chave.codigoVerificacao(),
                chave.chaveNotaNacional(),
                r.alertas(),
                r.xmlAssinado(),
                ""));
    }

    /**
     * Monta a resposta no contrato, com os mesmos nomes de campo do Ruby.
     *
     * <p>O nome e a assinatura sao os mesmos de {@code contrato_nfse} em
     * {@code nfse-sp-bridge/app.rb}, de proposito. Duas implementacoes do mesmo
     * contrato com dois metodos diferentes e o mesmo defeito das notas 29 e 30:
     * um lado muda o nome do campo e o outro nao percebe, porque ninguem compara
     * os dois. Mesma assinatura aqui significa que a leitura do contrato e uma
     * diff, nao um-temporal-de-conhecimento.
     *
     * <p>Os nomes sao snake_case porque o bridge ja usava snake_case, e nao o
     * contrario. Ver {@link RespostaNfsePadrao}.
     */
    static java.util.Map<String, Object> contrato_nfse(boolean sucesso, String inscricao,
                                                        String numero, String verificacao,
                                                        String chave_nacional,
                                                        java.util.List<NfseSpRespostaParser.Mensagem> alertas,
                                                        String xml, String erro) {
        var resposta = new RespostaNfsePadrao(sucesso, inscricao, numero, verificacao,
                chave_nacional, alertas, erro, xml);
        java.util.Map<String, Object> corpo = new java.util.LinkedHashMap<>();
        corpo.put("sucesso", resposta.sucesso());
        corpo.put("chave_nfse", resposta.chaveNfse());
        corpo.put("inscricao_municipal", resposta.chaveNfse());
        corpo.put("numero_nfse", resposta.numeroNfse());
        corpo.put("codigo_verificacao", resposta.codigoVerificacao());
        corpo.put("chave_nota_nacional", resposta.chaveNotaNacional());
        corpo.put("alertas", resposta.alertas());
        corpo.put("erro", resposta.erro());
        corpo.put("xml_assinado", resposta.xmlAssinado());
        return corpo;
    }

    /** Cancela uma ou mais NFS-e. */
    @PostMapping("/cancelar")
    public ResponseEntity<?> cancelar(@RequestBody @Valid CancelamentoRequest req) {
        var r = service.cancelar(req.detalhes());
        return ResponseEntity.ok(contrato_nfse(true, "", "", "", "", r.alertas(), "", ""));
    }

    /**
     * Estado da configuracao, sem revelar senha.
     *
     * <p>Fala o mesmo contrato das demais rotas, porque e este endpoint que o
     * watchdog consulta para decidir se a implementacao serve. Se o status
     * falasse outro idioma, o watchdog nao veria a implementacao estragada — que
     * e como a nota 29 passou.
     */
    @GetMapping("/status")
    public ResponseEntity<?> status() {
        java.util.Map<String, Object> corpo =
                new java.util.LinkedHashMap<>(contrato_nfse(true, "", "", "", "", java.util.List.of(), "", ""));
        corpo.put("habilitada", properties.isEnabled());
        corpo.put("url", properties.getUrl());
        corpo.put("leiaute_xsd", properties.getXsdVersion());
        corpo.put("cnpj_remetente", properties.getCnpjRemetente() == null ? "" : properties.getCnpjRemetente());
        corpo.put("certificado_configurado",
                properties.getCertificadoCaminho() != null && !properties.getCertificadoCaminho().isBlank());
        corpo.put("senha_configurada",
                properties.getCertificadoSenha() != null && !properties.getCertificadoSenha().isBlank());
        return ResponseEntity.ok(corpo);
    }

    // ------------------------------------------------------------------
    // requests
    // ------------------------------------------------------------------

    public record ConsultaCnpjRequest(@NotBlank String cnpj, String cnpjContribuinte) {
    }

    public record CancelamentoRequest(List<NfseSpService.CancelamentoRequest> detalhes) {
    }

    /** Corpo do {@code /emitir-rps}. */
    public record RpsRequest(
            String imPrestador, @NotBlank String serieRps, @NotBlank String numeroRps,
            String tipoRps, @NotBlank String dataEmissao, String statusRps, @NotBlank String tributacaoRps,
            @NotBlank String valorServicos, String valorDeducoes,
            String valorPis, String valorCofins, String valorInss, String valorIr, String valorCsll,
            @NotBlank String codigoServico, String aliquotaServicos, Boolean issRetido,
            String cpfTomador, String cnpjTomador, String nifTomador,
            String inscricaoMunicipalTomador, String inscricaoEstadualTomador,
            String razaoSocialTomador, EnderecoRequest enderecoTomador, String emailTomador,
            String cpfIntermediario, String cnpjIntermediario,
            String inscricaoMunicipalIntermediario, String issRetidoIntermediario,
            String emailIntermediario,
            @NotBlank String discriminacao,
            String valorCargaTributaria, String percentualCargaTributaria,
            String fonteCargaTributaria, String codigoCei, String matriculaObra,
            String municipioPrestacao,
            String numeroEncapsulamento, String valorTotalRecebido, String retencaoPisCofins,
            String valorFinalCobrado, String valorMulta, String valorJuros, String valorIpi,
            String exigibilidadeSuspensa, String ncm, String nbs,
            String cLocPrestacao, String cPaisPrestacao, IbsCbsRequest ibsCbs) {

        /**
         * @param cnpjRemetente vem da configuracao da API; um record nao alcanca
         *                      o bean, entao quem chama injeta
         */
        public NfseSpXmlBuilder.Rps paraRps(String cnpjRemetente) {
            return new NfseSpXmlBuilder.Rps(
                    cnpjRemetente,
                    imPrestador, serieRps, numeroRps,
                    tipoRps == null || tipoRps.isBlank() ? "RPS" : tipoRps,
                    dataEmissao,
                    statusRps == null || statusRps.isBlank() ? "N" : statusRps,
                    tributacaoRps,
                    valorServicos, valorDeducoes,
                    valorPis, valorCofins, valorInss, valorIr, valorCsll,
                    codigoServico, aliquotaServicos, Boolean.TRUE.equals(issRetido),
                    cpfTomador, cnpjTomador, nifTomador,
                    inscricaoMunicipalTomador, inscricaoEstadualTomador,
                    razaoSocialTomador,
                    enderecoTomador == null ? null : new NfseSpXmlBuilder.Endereco(
                            enderecoTomador.tipoLogradouro(), enderecoTomador.logradouro(),
                            enderecoTomador.numero(), enderecoTomador.complemento(),
                            enderecoTomador.bairro(), enderecoTomador.cidade(),
                            enderecoTomador.uf(), enderecoTomador.cep()),
                    emailTomador,
                    cpfIntermediario, cnpjIntermediario,
                    inscricaoMunicipalIntermediario, issRetidoIntermediario,
                    emailIntermediario,
                    discriminacao,
                    valorCargaTributaria, percentualCargaTributaria,
                    fonteCargaTributaria, codigoCei, matriculaObra,
                    municipioPrestacao,
                    numeroEncapsulamento, valorTotalRecebido, retencaoPisCofins,
                    valorFinalCobrado, valorMulta, valorJuros, valorIpi,
                    exigibilidadeSuspensa, ncm, nbs,
                    cLocPrestacao, cPaisPrestacao,
                    ibsCbs == null ? null : new NfseSpXmlBuilder.IbsCbs(
                            ibsCbs.finNfse(), ibsCbs.indFinal(), ibsCbs.cIndOp(),
                            ibsCbs.indDest(), ibsCbs.cClassTrib()));
        }
    }

    public record EnderecoRequest(String tipoLogradouro, String logradouro, String numero,
                                  String complemento, String bairro, String cidade,
                                  String uf, String cep) {
    }

    public record IbsCbsRequest(String finNfse, String indFinal, String cIndOp,
                                String indDest, String cClassTrib) {
    }
}
