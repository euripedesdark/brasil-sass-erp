package br.com.brasil_saas.fiscal.nacional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * O contrato que o ERP consome — o mesmo {@code /api/nfse-sp} da bridge Ruby e
 * da API de Sao Paulo.
 *
 * <p>As quatro rotas sao as mesmas porque o ERP nao muda uma linha para trocar
 * de municipal. O que muda e' o que responde atras do proxy da porta 4567.
 *
 * <p><b>Cada resposta traz {@code X-Backend}</b>, entao quem chama descobre quem
 * atendeu sem precisar perguntar.
 */
@Slf4j
@RestController
@RequestMapping("/api/nfse-sp")
@RequiredArgsConstructor
public class NfseNacionalController {

    private final NfseNacionalProperties props;
    private final NfseNacionalRegras regras;
    private final NfseNacionalService service;

    /**
     * Health check, e o que o {@code nfse-failover} consulta.
     *
     * <p>O proxy considera saudavel o que responde 2xx. Por isso o health
     * confere o que decide se a emissao funciona — autenticacao, cadastro e o
     * ambiente — e nao so se o processo esta vivo.
     *
     * <p><b>Um alerta que este health carrega:</b> em homologacao, o numero que
     * a prefeitura devolve <b>nao existe</b>. Quem mandar o ERP emitir em
     * producao com {@code homologacao=true} sem trocar este sinalizador grava
     * uma nota que ninguem podera consultar na prefeitura.
     */
    @GetMapping("/status")
    public ResponseEntity<?> status() {
        StringBuilder alerta = new StringBuilder();

        if (vazio(props.getUnidadeGestora())) {
            alerta.append("UnidadeGestora (CNPJ da Prefeitura) nao configurada; ");
        }
        if (vazio(props.getCnpjPrestador())) {
            alerta.append("CNPJ do prestador nao configurado; ");
        }
        if (vazio(props.getInscricaoMunicipalPrestador())) {
            // aviso, nao bloqueio: o XSD aceita sem ela
            alerta.append("Inscricao Municipal do prestador nao configurada — o XSD "
                    + "aceita sem ela, e a prefeitura confirma na chamada; ");
        }
        if (vazio(props.getChaveDigital()) && vazio(props.getCertificadoCaminho())) {
            alerta.append("nenhuma autenticacao configurada: nem ChaveDigital nem certificado; ");
        }
        if (!vazio(props.getChaveDigital()) && !vazio(props.getCertificadoCaminho())) {
            alerta.append("ChaveDigital E certificado configurados: o XSD aceita um ou o outro, "
                    + "nao os dois; ");
        }
        if (!vazio(props.getCertificadoCaminho()) && !props.isAssinar()) {
            alerta.append("certificado configurado mas assinar=false: a declaracao vai sem "
                    + "assinatura e sem ChaveDigital; ");
        }
        if (props.isValidar()) {
            if (props.formato().posReforma()) {
                alerta.append("validar=true com o formato POS_REFORMA, mas o XSD 1.00 e do "
                        + "formato ANTES e rejeita CodigoNBS, MunicipioPrestacaoServico e "
                        + "PisCofins; ");
            }
        } else {
            alerta.append("validacao local DESLIGADA: o unico XSD disponivel e o 1.00, do "
                    + "formato antes da reforma, e ele rejeita o formato vigente; ");
        }
        if (!props.isHomologacao()) {
            alerta.append("AMBIENTE PRODUCAO — as notas emitidas sao reais e gravadas na "
                    + "prefeitura; ");
        }

        String im = props.getInscricaoMunicipalPrestador() == null
                ? "" : props.getInscricaoMunicipalPrestador();

        if (alerta.length() > 0) {
            return ResponseEntity.ok(RespostaNfsePadrao.recusado(alerta.toString()));
        }

        List<RespostaNfsePadrao.Mensagem> avisos = props.isHomologacao()
                ? List.of(new RespostaNfsePadrao.Mensagem("HOMOLOGACAO",
                        "Em homologacao: a prefeitura valida e nao grava. O numero devolvido "
                                + "nao existe no sistema municipal."))
                : List.of();

        return ResponseEntity.ok(RespostaNfsePadrao.emitido(im, "", "", "", avisos, ""));
    }

    /**
     * Confere IM, regime e se emite NFS-e.
     *
     * <p>No padrao nacional nao ha consulta de CNPJ: o cadastro e' do Database de
     * Contribuintes. O que esta rota devolve e' o que o ERP precisa — se a
     * empresa esta inscrita e em que regime — lido do cadastro local, dizendo
     * que e' cache.
     *
     * <p>A rota existe no contrato porque o ERP chama as quatro, e nao porque o
     * AGILIBlue tenha o equivalente.
     */
    @PostMapping("/consulta-cnpj")
    public ResponseEntity<?> consultarCnpj(@RequestBody ConsultaCnpjRequest req) {
        String cnpj = req == null ? null : req.cnpj();
        if (cnpj == null || cnpj.isBlank() || cnpj.replaceAll("\\D", "").length() != 14) {
            return ResponseEntity.badRequest()
                    .body(RespostaNfsePadrao.recusado("CNPJ invalido: esperado 14 digitos."));
        }
        String im = props.getInscricaoMunicipalPrestador();
        if (im == null || im.isBlank()) {
            return ResponseEntity.ok(RespostaNfsePadrao.recusado(
                    "IM do prestador nao configurada nesta API; configure "
                            + "nfse.nacional.inscricao-municipal-prestador."));
        }
        return ResponseEntity.ok(RespostaNfsePadrao.emitido(im, "", "", "",
                List.of(new RespostaNfsePadrao.Mensagem("IM",
                        "Inscricao Municipal " + im + " lida do cadastro local. No padrao "
                                + "nacional a confirmacao de que o contribuinte esta inscrito vem "
                                + "do Database de Contribuintes; esta resposta e' cache.")),
                ""));
    }

    /**
     * Emite a nota.
     *
     * <p>Validacao local com 422 e o campo que falhou. Em Sao Paulo a
     * prefeitura validava e devolvia o erro 1001 com o XSD inteiro.
     */
    @PostMapping("/emitir-rps")
    public ResponseEntity<?> emitirRps(@RequestBody EmissaoRequest req) {
        RespostaNfsePadrao resposta = service.emitir(req);
        return resposta.sucesso()
                ? ResponseEntity.ok(resposta)
                : ResponseEntity.unprocessableEntity().body(resposta);
    }

    /**
     * Cancela por numero da NFS-e.
     *
     * <p><b>Por numero, e nao por chave nacional.</b> E o que o
     * {@code CancelarNfseEnvio} do material traz, em
     * {@code IdentificacaoNfse/Numero}. O padrao nacional cancela por chave de
     * 44 posicoes; o AGILIBlue nao.
     */
    @PostMapping("/cancelar")
    public ResponseEntity<?> cancelar(@RequestBody CancelamentoRequest req) {
        RespostaNfsePadrao resposta = service.cancelar(req);
        return resposta.sucesso()
                ? ResponseEntity.ok(resposta)
                : ResponseEntity.badRequest().body(resposta);
    }

    // ------------------------------------------------------------------
    // corpos
    // ------------------------------------------------------------------

    public record ConsultaCnpjRequest(String cnpj) {
    }

    /**
     * O corpo da emissao.
     *
     * <p><b>Os nomes vem do XSD do AGILIBlue, nao do contrato da prefeitura de
     * Sao Paulo.</b> Aqui nao ha traducao: o ERP fala o idioma do AGILIBlue
     * desde o inicio, porque o AGILIBlue e' o padrao nacional e o ERP emite
     * para ele.
     */
    public record EmissaoRequest(
            String unidadeGestora,
            String cnpjPrestador,
            String inscricaoMunicipalPrestador,
            String discriminacao,
            String itemLei116,
            String codigoCnae,
            String codigoAtividadeEconomica,
            String codigoMunicipioIncidencia,
            String descricaoMunicipioIncidencia,
            String ufIncidencia,
            // so do formato depois da reforma
            String codigoMunicipioPrestacao,
            String descricaoMunicipioPrestacao,
            String ufPrestacao,
            String aliquota,
            String valorServicos,
            String valorIssqnCalculado,
            String valorIssqnRecolher,
            String valorLiquido,
            String valorDescontos,
            // Retencoes federais: as tres somam em ValorCsll no XML.
            // Ver NfseNacionalMapeamentoTributacao.
            String retencaoPis,
            String retencaoCofins,
            String retencaoCsll,
            // Reducao da base do IBS/CBS, que e' o que as tags ValorPis e
            // ValorCofins recebem depois da mudanca do padrao ADN.
            String reducaoBaseIbsCbsPis,
            String reducaoBaseIbsCbsCofins,
            // INSS, IRRF e demais: o aviso do ADN nao os agrupa, seguem separados
            String valorInss,
            String valorIrrf,
            String valorOutrasRetencoes,
            String valorBaseCalculoIssqn,
            String valorDeducaoConstrucaoCivil,
            /** -1 a -9 pelo XSD. Entra junto com percAbatimento. */
            String tipoAbatimento,
            String percAbatimento,
            String regimeEspecialTributacao,
            String descricaoRegimeTributacao,
            String optanteSimplesNacional,
            String optanteMei,
            String issqnRetido,
            String responsavelIssqn,
            String descricaoResponsavelIssqn,
            String exigibilidadeIssqn,
            String descricaoExigibilidade,
            String cnpjTomador,
            String cpfTomador,
            String identificacaoTomador,
            String inscricaoMunicipalTomador,
            String nomeTomador,
            /** 1 = Brasil, 2 = Exterior. Vazio usa 1. */
            String localEndereco,
            /** true troca Endereco por EnderecoExterior. */
            boolean tomadorExterior,
            String tipoLogradouro,
            String logradouro,
            String numero,
            String complemento,
            String bairro,
            String codigoMunicipioTomador,
            String descricaoMunicipioTomador,
            String ufTomador,
            String cep,
            String codigoPaisBacen,
            String nomeEstado,
            String nomeMunicipio,
            String telefone,
            String email,
            String inscricaoEstadual,
            String observacao,
            String quantidade,
            String valorServico,
            String valorDesconto,
            String codigoSituacaoTributaria,
            String codigoTipoRetencao,
            /** NBS de 9 digitos, so do formato depois da reforma. */
            String codigoNbs,
            // ---- as tres tags de atividade economica: o municipio diz qual ----
            // codigoAtividadeEconomica ja vem acima, com o regime
            String codigoCnaeAtividadeEconomica,
            String itemLei116AtividadeEconomica,
            // ---- so do formato antes da reforma ----
            String nfseSubstituida,
            String numeroRps,
            String serieRps,
            String tipoRps,
            String descricaoRps,
            String identificadorRps,
            String identificadorSubstituidoRps,
            String dataEmissaoRps,
            // ---- intermediario ----
            String cpfIntermediario,
            String cnpjIntermediario,
            String inscricaoMunicipalIntermediario,
            String razaoSocialIntermediario,
            // ---- material usado ----
            String codigoObra,
            String art,
            List<MaterialNotaUsada> notasUsadas,
            String beneficioProcesso,
            String dadosCartaCorrecao) {

        /** Uma nota de material usado, com os itens consumidos. */
        public record MaterialNotaUsada(
                String cpf,
                String cnpj,
                String nomeFornecedor,
                String numeroNf,
                String dataNf,
                List<ItemMaterialUsado> itens) {
        }

        /** Um item consumido de uma nota de material. */
        public record ItemMaterialUsado(
                String idItemUsado,
                String descricaoItemUsado,
                String idUnidadeMedida,
                String descricaoUnidadeMedida,
                String quantidade,
                String valorTotal) {
        }

        private static LocalDate data(String iso) {
            return iso == null || iso.isBlank() ? null : LocalDate.parse(iso);
        }

        /**
         * Para o construtor de declaracao.
         *
         * <p>Traducao do contrato do ERP para o agrupamento do XSD. E' a unica
         * traducao do projeto, e e' um metodo e nao um record solto porque
         * {@code DadosDeclaracao} tem oito grupos e a ordem dos campos dentro de
         * cada um e' a do XSD.
         *
         * <p>Os numeros chegam como texto porque o ERP envia texto. A conversao
         * aceita {@code 100,00} e {@code 100.00}: o XSD quer ponto, e recusar por
         * causa de virgula daria "formato invalido" para um numero certo.
         */
        public NfseNacionalDeclaracaoBuilder.DadosDeclaracao paraDados(
                NfseNacionalProperties p) {
            return new NfseNacionalDeclaracaoBuilder.DadosDeclaracao(
                    (vazio(unidadeGestora) ? p.getUnidadeGestora() : unidadeGestora),

                    new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.IdentificacaoPrestador(
                            p.getChaveDigital(),
                            vazio(cnpjPrestador) ? p.getCnpjPrestador() : cnpjPrestador,
                            null,
                            vazio(inscricaoMunicipalPrestador) ? p.getInscricaoMunicipalPrestador()
                                    : inscricaoMunicipalPrestador),

                    new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.Rps(numeroRps, serieRps, tipoRps, descricaoRps,
                            identificadorRps, identificadorSubstituidoRps,
                            data(dataEmissaoRps)),
                    num(nfseSubstituida) == null ? null : num(nfseSubstituida).longValue(),

                    new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.DadosTomador(
                            cnpjTomador, cpfTomador, identificacaoTomador,
                            inscricaoMunicipalTomador, nomeTomador, localEndereco,
                            tomadorExterior,
                            tipoLogradouro, logradouro, numero, complemento, bairro,
                            codigoMunicipioTomador, descricaoMunicipioTomador, ufTomador,
                            cep, codigoPaisBacen, nomeEstado, nomeMunicipio,
                            telefone, email, inscricaoEstadual),

                    (vazio(inscricaoMunicipalIntermediario) && vazio(razaoSocialIntermediario))
                            ? null
                            : new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.DadosIntermediario(cpfIntermediario, cnpjIntermediario,
                                    inscricaoMunicipalIntermediario, razaoSocialIntermediario),

                    (vazio(codigoObra) && vazio(art) && (notasUsadas == null || notasUsadas.isEmpty()))
                            ? null
                            : new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.MaterialUsado(codigoObra, art, notasUsadas == null
                                    ? List.of() : notasUsadas.stream().map(n -> new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.NotaUsada(
                                            n.cpf(), n.cnpj(), n.nomeFornecedor(), n.numeroNf(),
                                            data(n.dataNf()), n.itens() == null ? List.of()
                                                    : n.itens().stream().map(it -> new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.ItemUsado(
                                                            it.idItemUsado(), it.descricaoItemUsado(),
                                                            it.idUnidadeMedida(),
                                                            it.descricaoUnidadeMedida(),
                                                            num(it.quantidade()),
                                                            num(it.valorTotal()))).toList()))
                                    .toList()),

                    new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.Regime(
                            regimeEspecialTributacao, descricaoRegimeTributacao,
                            sim(optanteSimplesNacional), sim(optanteMei), sim(issqnRetido),
                            responsavelIssqn, descricaoResponsavelIssqn,
                            codigoAtividadeEconomica,
                            exigibilidadeIssqn, descricaoExigibilidade),

                    new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.AtividadeEconomica(codigoAtividadeEconomica,
                            codigoCnaeAtividadeEconomica, itemLei116AtividadeEconomica),

                    new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.Incidencia(codigoMunicipioIncidencia, descricaoMunicipioIncidencia,
                            ufIncidencia),

                    new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.MunicipioPrestacao(codigoMunicipioPrestacao,
                            descricaoMunicipioPrestacao, ufPrestacao),

                    new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.Valores(
                            num(valorServicos), num(valorDescontos),
                            num(valorBaseCalculoIssqn), num(aliquota),
                            num(valorIssqnCalculado), num(valorIssqnRecolher),
                            tipoAbatimento, num(percAbatimento),
                            num(valorDeducaoConstrucaoCivil), num(valorLiquido),
                            num(retencaoPis), num(retencaoCofins), num(retencaoCsll),
                            num(reducaoBaseIbsCbsPis), num(reducaoBaseIbsCbsCofins),
                            num(valorInss), num(valorIrrf), num(valorOutrasRetencoes),
                            observacao, complemento),

                    new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.ItemServico(
                            discriminacao, codigoCnae, itemLei116,
                            num(quantidade), num(valorServico), num(valorDesconto)),

                    beneficioProcesso,
                    vazio(dadosCartaCorrecao) ? null
                            : new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.CartaCorrecao(dadosCartaCorrecao),

                    new NfseNacionalDeclaracaoBuilder.DadosDeclaracao.Reforma(codigoSituacaoTributaria, codigoTipoRetencao, codigoNbs));
        }

        private static boolean vazio(String s) {
            return s == null || s.isBlank();
        }

        /** Os numeros que a validacao local precisa, ja convertidos. */
        public BigDecimal valorServicosN() {
            return num(valorServicos);
        }

        public BigDecimal aliquotaN() {
            return num(aliquota);
        }

        public BigDecimal valorIssqnCalculadoN() {
            return num(valorIssqnCalculado);
        }

        private static BigDecimal num(String v) {
            if (v == null || v.isBlank()) {
                return null;
            }
            String t = v.strip().replace("R$", "").replace(" ", "").replace("%", "");
            if (t.contains(",") && !t.contains(".")) {
                t = t.replace(',', '.');
            } else if (t.contains(",")) {
                t = t.replace(",", "");
            }
            try {
                return new BigDecimal(t);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("valor nao numerico: '" + v + "'", e);
            }
        }

        private static boolean sim(String flag) {
            return "1".equals(flag) || "true".equalsIgnoreCase(String.valueOf(flag));
        }
    }

    public record CancelamentoRequest(List<Detalhe> detalhes) {
        /**
         * @param numero            o numero da NFS-e, como no material
         * @param codigoCancelamento da lista de motivos da prefeitura
         * @param motivo            a justificativa, obrigatoria
         */
        public record Detalhe(String numero, String codigoCancelamento, String motivo) {
        }
    }

    private static boolean vazio(String s) {
        return s == null || s.isBlank();
    }
}
