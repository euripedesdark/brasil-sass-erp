package br.com.brasil_saas.fiscal.nacional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Monta o {@code GerarNfseEnvio} do AGILIBlue.
 *
 * <h2>A ordem das tags e' do XSD, e o XSD e' o do dono</h2>
 *
 * <p>O schema esta em {@code src/main/resources/schemas/nfse-v-100.xsd}, copia
 * do arquivo que veio no material da prefeitura. A sequencia de
 * {@code tcDeclaracaoPrestacaoServico} e' fixa, e inverter duas tags produz um
 * XML bem formado que a prefeitura recusa com mensagem de semantica.
 *
 * <h2>O que difere da Prefeitura de Sao Paulo, e nao se adivinha</h2>
 *
 * <table border="1">
 *   <caption>AGILIBlue x Sao Paulo</caption>
 *   <tr><th></th><th>AGILIBlue</th><th>Sao Paulo</th></tr>
 *   <tr><td>autenticacao</td><td>{@code ChaveDigital} ou {@code dsig:Signature}</td>
 *       <td>certificado A1 na mensagem</td></tr>
 *   <tr><td>documento</td><td>declaracao com o RPS embutido</td>
 *       <td>RPS separado, em envelope SOAP</td></tr>
 *   <tr><td>codigo da LC 116</td><td>{@code 1.06}, com ponto</td>
 *       <td>5 posicoes, sem ponto</td></tr>
 *   <tr><td>CNAE</td><td>{@code 62040.00}, com ponto</td><td>nao existe</td></tr>
 *   <tr><td>regime</td><td>tres tags separadas</td><td>uma flag de ISS retido</td></tr>
 *   <tr><td>resposta</td><td>a nota inteira, sincrona</td>
 *       <td>somente numero e codigo</td></tr>
 * </table>
 */
@Component
public class NfseNacionalDeclaracaoBuilder {

    /** O namespace do AGILIBlue. Nao e' o da nacional nem o da SP. */
    public static final String NS = "http://www.agili.com.br/nfse_v_1.00.xsd";

    /** A versao do schema. E' o que vai em {@code <Versao>}. */
    public static final String VERSAO = "1.00";

    private final NfseNacionalProperties props;
    private final NfseNacionalMapeamentoTributacao mapeamento;

    public NfseNacionalDeclaracaoBuilder(NfseNacionalProperties props,
                                        NfseNacionalMapeamentoTributacao mapeamento) {
        this.props = props;
        this.mapeamento = mapeamento;
    }

    /**
     * O que o ERP manda.
     *
     * <p>Os nomes sao em portugues e nao os do XSD: este record e' a traducao,
     * e e' o unico lugar onde os dois vocabularios se encontram.
     */
    /**
     * O que o ERP manda, agrupado como o XSD agrupa.
     *
     * <p><b>Por que records aninhados e nao 68 parametros soltos.</b> Com uma
     * lista so, dois campos de mesmo tipo em posicoes consecutivas sao
     * intercambiaveis e o compilador aceita a troca em silencio: {@code
     * retencaoPis} no lugar de {@code retencaoCofins} compila, roda, e produz
     * nota com tributacao errada. E' a classe de bug que o aviso do AGILIBlue
     * sobre a adequacao ao padrao ADN aponta — "revisao imediata do mapeamento
     * (de/para) de envio das tags de impostos e retencoes no seu ERP para evitar
     * rejeicoes ou calculos incorretos na base do IBS/CBS" — e o agrupamento e'
     * o que impede o erro de mapeamento, e nao um cuidado de quem escreve.
     */
    public record DadosDeclaracao(
            String unidadeGestora,
            IdentificacaoPrestador prestador,
            Rps rps,
            Long nfseSubstituida,
            DadosTomador tomador,
            DadosIntermediario intermediario,
            MaterialUsado materialUsado,
            Regime regime,
            AtividadeEconomica atividade,
            Incidencia incidencia,
            MunicipioPrestacao municipioPrestacao,
            Valores valores,
            ItemServico servico,
            String beneficioProcesso,
            CartaCorrecao cartaCorrecao,
            Reforma reforma) {

        /**
         * {@code Rps/IdentificacaoRps} e a data de emissao.
         *
         * <p>Sai do padrao novo: no exemplo de 21/05/2026 nao ha bloco
         * {@code Rps}. No {@code nfse-v-100.xsd} ele existe, e o
         * {@code IdentificacaoRps/Identificador} e' o substituto do numero.
         *
         * <p>{@code Tipo} e' {@code -2|-4|-5} pelo XSD, e o exemplo de exterior
         * traz {@code -2}. {@code Serie} e' texto livre — o exemplo traz
         * {@code NFSe}.
         */
        public record Rps(
                String numero,
                String serie,
                String tipo,
                String descricao,
                String identificador,
                String identificadorSubstituido,
                LocalDate dataEmissao) {
        }

        /**
         * {@code DadosIntermediario}.
         *
         * <p>Quem retem o ISSQN e nao o prestador. Quando existe, o
         * {@code ResponsavelISSQN/Codigo} muda de valor — e o
         * {@code ValorISSQNRecolher} passa a ter valor.
         */
        public record DadosIntermediario(
                String cpf,
                String cnpj,
                String inscricaoMunicipal,
                String razaoSocial) {
        }

        /**
         * {@code DadosMaterialUsado}: material de construção civil.
         *
         * <p>Sai para a obra, com a lista de notas de material e os itens de
         * cada nota. E' o que sustenta o perfil de construção civil dos exemplos.
         */
        public record MaterialUsado(
                String codigoObra,
                String art,
                List<NotaUsada> notasUsadas) {
        }

        public record NotaUsada(
                String cpf,
                String cnpj,
                String nomeFornecedor,
                String numeroNf,
                LocalDate dataNf,
                List<ItemUsado> itens) {
        }

        public record ItemUsado(
                String idItemUsado,
                String descricaoItemUsado,
                String idUnidadeMedida,
                String descricaoUnidadeMedida,
                BigDecimal quantidade,
                BigDecimal valorTotal) {
        }

        /**
         * A atividade economica, que <b>muda de tag conforme a parametrizacao do
         * municipio</b>.
         *
         * <p>O XSD tem tres tags — {@code CodigoAtividadeEconomica},
         * {@code CodigoCnaeAtividadeEconomica} e
         * {@code ItemLei116AtividadeEconomica} — e a documentacao diz que o
         * municipio parametriza "tipo de estrutura de atividade economica". Os
         * exemplos mostram as duas pontas: o perfil {@code Normal} usa
         * {@code CodigoAtividadeEconomica=62040.00} e o de 21/05/2026 usa
         * {@code ItemLei116AtividadeEconomica=070901}.
         *
         * <p>E por isso que o registro tem os tres campos: <b>qualquer um dos
         * tres, conforme o que o municipio configurou.</b> Preencher mais de um
         * produz XML invalido.
         */
        public record AtividadeEconomica(
                String codigoAtividadeEconomica,
                String codigoCnaeAtividadeEconomica,
                String itemLei116AtividadeEconomica) {

            /** Quantos dos tres foram preenchidos. */
            public int preenchidos() {
                return (vazio(codigoAtividadeEconomica) ? 0 : 1)
                        + (vazio(codigoCnaeAtividadeEconomica) ? 0 : 1)
                        + (vazio(itemLei116AtividadeEconomica) ? 0 : 1);
            }
        }

        /**
         * {@code MunicipioPrestacaoServico}, que so existe no formato depois da
         * reforma.
         *
         * <p>E' separado do {@code MunicipioIncidencia}: o municipio onde a
         * prestacao acontece e' o municipio de incidencia so quando a operacao e'
         * interna. No exemplo de 21/05/2026 os dois vem com o mesmo codigo, mas
         * sao campos distintos porque podem divergir.
         */
        public record MunicipioPrestacao(String codigoMunicipio, String descricao, String uf) {
        }

        /** {@code ListaCartaCorrecao/DadosCartaCorrecao}. */
        public record CartaCorrecao(String dados) {
        }

        /** {@code IdentificacaoPrestador}: onde esta a autenticacao. */
        public record IdentificacaoPrestador(
                String chaveDigital,
                String cnpj,
                String cpf,
                String inscricaoMunicipal) {
        }

        /** {@code DadosTomador}. Tudo opcional: no padrao nacional o tomador pode faltar. */
        public record DadosTomador(
                String cnpj,
                String cpf,
                String identificacao,
                String inscricaoMunicipal,
                String razaoSocial,
                /** 1 = Brasil, 2 = Exterior. Vazio usa 1. */
                String localEndereco,
                /**
                 * Tomador no exterior.
                 *
                 * <p>{@code true} troca {@code Endereco} por
                 * {@code EnderecoExterior}, que tem {@code Descricao},
                 * {@code Pais/CodigoPaisBacen}, {@code NomeEstado} e
                 * {@code NomeMunicipio} — em vez de logradouro, numero, bairro
                 * e municipio IBGE. O exemplo de exterior do material mostra
                 * {@code LocalEnderejo=2} com {@code CodigoPaisBacen=2496}
                 * (Estados Unidos).
                 */
                boolean exterior,
                String tipoLogradouro,
                String logradouro,
                String numero,
                String complemento,
                String bairro,
                String codigoMunicipio,
                String descricaoMunicipio,
                String uf,
                String cep,
                String codigoPaisBacen,
                String nomeEstado,
                String nomeMunicipio,
                String telefone,
                String email,
                String inscricaoEstadual) {

            public String localEndereco() {
                return localEndereco == null || localEndereco.isBlank() ? "1" : localEndereco;
            }
        }

        /**
         * As tres tags de regime, que sao independentes de proposito.
         *
         * <p>Um prestador pode ser simples <b>e</b> optante do MEI <b>e</b> estar
         * em tributacao especial. Colapsar em um unico "regime" perde uma das tres,
         * e a prefeitura valida cada tag.
         */
        public record Regime(
                String tributacaoEspecialCodigo,
                String tributacaoEspecialDescricao,
                boolean optanteSimplesNacional,
                boolean optanteMei,
                boolean issqnRetido,
                String responsavelIssqnCodigo,
                String responsavelIssqnDescricao,
                String codigoAtividadeEconomica,
                String exigibilidadeCodigo,
                String exigibilidadeDescricao) {
        }

        /** {@code MunicipioIncidencia}. */
        public record Incidencia(String codigoMunicipio, String descricao, String uf) {
        }

        /**
         * Os valores.
         *
         * <p>As tres retencoes federais sao do ERP; a reducao de base do IBS/CBS
         * e' outra coisa e vai para as tags {@code ValorPis} e {@code ValorCofins}.
         * O ISSQN e' municipal e nao entra no agrupamento federal — ver
         * {@link NfseNacionalMapeamentoTributacao}.
         *
         * <p>{@code valorLiquido} vem do ERP e <b>nao e' recalculado</b>: os dez
         * exemplos do material nao exercitam nenhuma retencao, entao nao dizem
         * qual e' a conta.
         */
        public record Valores(
                BigDecimal servicos,
                BigDecimal descontos,
                BigDecimal baseCalculoIssqn,
                BigDecimal aliquotaIssqn,
                BigDecimal issqnCalculado,
                BigDecimal issqnRecolher,
                /**
                 * {@code TipoAbatimento}: {@code -1} a {@code -9} pelo XSD.
                 * So entra junto com {@code PercAbatimento}, e o par existe para
                 * o abatimento de materia-prima.
                 */
                String tipoAbatimento,
                BigDecimal percAbatimento,
                BigDecimal deducaoConstrucaoCivil,
                BigDecimal liquido,
                // ---- retencoes federais: somam em ValorCsll ----
                BigDecimal retencaoPis,
                BigDecimal retencaoCofins,
                BigDecimal retencaoCsll,
                // ---- reducao da base do IBS/CBS: vai em ValorPis/ValorCofins ----
                BigDecimal reducaoBaseIbsCbsPis,
                BigDecimal reducaoBaseIbsCbsCofins,
                // ---- demais, que o aviso do ADN nao agrupa ----
                BigDecimal inss,
                BigDecimal irrf,
                BigDecimal outrasRetencoes,
                String observacao,
                /** So no formato antes da reforma. Vazio usa "Gerado via WebService". */
                String complemento) {
        }

        /** {@code ListaServico/DadosServico}. */
        public record ItemServico(
                String discriminacao,
                String codigoCnae,
                String itemLei116,
                BigDecimal quantidade,
                BigDecimal valorServico,
                BigDecimal valorDesconto) {
        }

        /**
         * As duas tags da NT 007/2026, dentro de {@code <PisCofins>}.
         *
         * <p>Estrutura lida do PDF, pagina 13:
         *
         * <pre>
         *   &lt;PisCofins&gt;
         *     &lt;CodigoSituacaoTributaria&gt;-2&lt;/CodigoSituacaoTributaria&gt;
         *     &lt;CodigoTipoRetencao&gt;-5&lt;/CodigoTipoRetencao&gt;
         *   &lt;/PisCofins&gt;
         * </pre>
         *
         * <p><b>Nao e' um par de tags soltas na declaracao.</b> O PDF mostra as
         * duas dentro de um wrapper {@code <PisCofins>}, e o exemplo da
         * documentacao usa -2 (aliquota basica) com -5 (PIS/COFINS retidos, CSLL
         * nao retido).
         */
        public record Reforma(
                String codigoSituacaoTributaria,
                String codigoTipoRetencao,
                /**
                 * {@code CodigoNBS}, de 9 digitos, e so do formato depois da
                 * reforma. O exemplo de 21/05/2026 traz {@code 124033200}.
                 *
                 * <p>Nao esta no {@code nfse-v-100.xsd}, que e' do formato
                 * antigo. E' o codigo da NBS da reforma tributaria, e sai no
                 * lugar do {@code ItemLei116} que ia dentro de
                 * {@code DadosServico}.
                 */
                String codigoNbs) {
        }
    }

    /**
     * Gera o {@code GerarNfseEnvio}.
     *
     * @return o XML sem assinatura; a assinatura entra em
     *         {@link NfseNacionalSigner}
     */
    public String montar(DadosDeclaracao dados) {
        Document doc = novoDocumento();
        Element raiz = doc.getDocumentElement();

        txt(raiz, "UnidadeGestora", dados.unidadeGestora());

        // <b>A ordem das tags depende do formato.</b> Os dois tem 28 tags no mesmo
        // nivel, e quatro em cada lado sao distintas — RegimeEspecialTributacao,
        // CodigoAtividadeEconomica, ValorDeducaoConstCivil e Complemento no
        // antigo; ItemLei116AtividadeEconomica, CodigoNBS,
        // MunicipioPrestacaoServico e PisCofins no novo. A sequencia do XSD e'
        // lida posicao a posicao, e montar no formato errado produz XML invalido
        // sem pista de qual tag estava fora de lugar.
        boolean pos = props.formato().posReforma();

        Element dps = sub(raiz, "DeclaracaoPrestacaoServico");
        identificacaoPrestador(dps, dados);
        rpsENumeroDeclaracao(dps, dados, pos);
        dadosTomador(dps, dados, pos);
        intermediario(dps, dados);
        materialUsado(dps, dados);
        regime(dps, dados, pos);
        // No formato novo a atividade vem ANTES da exigibilidade — e' assim no
        // exemplo de 21/05/2026, e o contrario da sequencia do XSD 1.00.
        atividadeEconomica(dps, dados, pos);
        exigibilidade(dps, dados, pos);
        // MunicipioPrestacaoServico ANTES de MunicipioIncidencia, como no
        // exemplo de 21/05/2026. Sao campos distintos porque o municipio da
        // prestacao e o de incidencia so coincidem em operacao interna.
        municipioPrestacao(dps, dados, pos);
        incidencia(dps, dados);
        valores(dps, dados, pos);
        listaServico(dps, dados, pos);
        cartaCorrecao(dps, dados);

        txt(dps, "Versao", VERSAO);

        return NfseNacionalClient.serializar(doc);
    }

    // ------------------------------------------------------------------
    // pedacos
    // ------------------------------------------------------------------

    /**
     * {@code IdentificacaoPrestador}, onde esta a autenticacao.
     *
     * <p><b>O XSD e' um {@code xsd:choice}, e sem {@code minOccurs="0"} ele e'
     * obrigatorio:</b>
     *
     * <pre>
     *   &lt;xsd:choice&gt;
     *     &lt;xsd:element name="ChaveDigital" type="tsChaveDigital"/&gt;
     *     &lt;xsd:element ref="dsig:Signature"/&gt;
     *   &lt;/xsd:choice&gt;
     * </pre>
     *
     * <p>A {@code ChaveDigital} e a assinatura sao <b>alternativas</b> — mandar as
     * duas viola o {@code choice}. A assinatura e' XMLDSig padrao
     * ({@code http://www.w3.org/2000/09/xmldsig#}), a mesma que a API de Sao Paulo
     * produz. E' o reaproveitamento real do projeto.
     *
     * <p><b>Sem atributo {@code Id}.</b> O XSD nao declara atributo neste tipo, e
     * {@code attributeFormDefault="unqualified"}, entao um {@code Id} aqui e'
     * recusado. Efeito no assinante: a {@code Reference} aponta para o documento
     * inteiro.
     */
    private void identificacaoPrestador(Element pai, DadosDeclaracao d) {
        Element prest = sub(pai, "IdentificacaoPrestador");
        var p = d.prestador();

        if (naoVazio(p.chaveDigital())) {
            txt(prest, "ChaveDigital", p.chaveDigital());
        }

        Element cpfCnpj = sub(prest, "CpfCnpj");
        if (naoVazio(p.cnpj())) {
            txt(cpfCnpj, "Cnpj", digitos(p.cnpj()));
        } else {
            txt(cpfCnpj, "Cpf", digitos(p.cpf()));
        }
        txt(prest, "InscricaoMunicipal", p.inscricaoMunicipal());
    }

    /**
     * {@code Rps} e {@code NfseSubstituida}.
     *
     * <p>Sao do padrao antigo, e <b>nao sao emitidos no formato depois da
     * reforma</b>: o exemplo de 21/05/2026 nao tem nenhum dos dois. A numeracao
     * passa a ser da prefeitura.
     */
    private void rpsENumeroDeclaracao(Element pai, DadosDeclaracao d, boolean posReforma) {
        if (posReforma) {
            return;
        }
        if (d.nfseSubstituida() != null) {
            txt(pai, "NfseSubstituida", d.nfseSubstituida().toString());
        }
        var r = d.rps();
        if (r == null) {
            return;
        }
        // <b>IdentificacaoRps e' um xsd:CHOICE de duas sequencias, nao uma
        // sequencia so:</b>
        //
        // <pre>
        //   choice(
        //     sequence( Numero, Serie, Tipo, Descricao? ),        &lt;-- a antiga
        //     sequence( Identificador, IdentificadorSubstituido? )  &lt;-- a nova
        //   )
        // </pre>
        //
        // <p>Ou a antiga, ou a nova — <b>mandar as duas e' o erro</b>, e o XSD
        // recusa com "Invalid content was found starting with element
        // 'Identificador'. No child element is expected at this point".
        //
        // <p>A escolha do par e' da prefeitura. Os exemplos do material usam o
        // par antigo; a API aceita os dois e nao decide — quem decide e' o
        // cadastro do prestador.
        List<String> faltando = new java.util.ArrayList<>();
        boolean parNovo = preenchido(r.identificador())
                || preenchido(r.identificadorSubstituido());
        boolean parAntigo = preenchido(r.numero()) || preenchido(r.serie())
                || preenchido(r.tipo()) || preenchido(r.descricao());

        if (parNovo && parAntigo) {
            throw new NfseNacionalException(
                    "IdentificacaoRps tem dois pares no XSD e sao alternativas: "
                            + "Numero/Serie/Tipo/Descricao (a antiga) ou "
                            + "Identificador/IdentificadorSubstituido (a nova). "
                            + "Preenchidos os dois. A prefeitura escolhe o par pelo "
                            + "cadastro do prestador.");
        }
        if (!parNovo && !parAntigo) {
            throw new NfseNacionalException(
                    "IdentificacaoRps vazio: o XSD exige Numero com Serie e Tipo, "
                            + "ou Identificador.");
        }

        Element e = sub(pai, "Rps");
        Element id = sub(e, "IdentificacaoRps");
        if (parNovo) {
            if (vazio(r.identificador())) {
                faltando.add("Identificador");
            }
            txt(id, "Identificador", r.identificador());
            txt(id, "IdentificadorSubstituido", r.identificadorSubstituido());
        } else {
            if (vazio(r.numero())) faltando.add("Numero");
            if (vazio(r.serie())) faltando.add("Serie");
            if (vazio(r.tipo())) faltando.add("Tipo");
            txt(id, "Numero", r.numero());
            txt(id, "Serie", r.serie());
            txt(id, "Tipo", r.tipo());
            txt(id, "Descricao", r.descricao());
        }

        // DataEmissao e' obrigatoria em tcRps, e o txt() pula valor vazio — entao
        // sem esta checagem o Rps saia pela metade e o XSD recusava com "The
        // content of element 'Rps' is not complete", sem dizer o que faltava.
        if (r.dataEmissao() == null) {
            faltando.add("DataEmissao");
        }

        if (!faltando.isEmpty()) {
            throw new NfseNacionalException(
                    "O bloco Rps exige estes campos, e faltaram: "
                            + String.join(", ", faltando)
                            + ". Sem o Tipo, a prefeitura nao sabe se e' RPS "
                            + "proprio ou substituto.");
        }
        txt(e, "DataEmissao", r.dataEmissao().toString());
    }

    private void intermediario(Element pai, DadosDeclaracao d) {
        var i = d.intermediario();
        if (i == null) {
            return;
        }
        Element e = sub(pai, "DadosIntermediario");
        Element id = sub(e, "IdentificacaoIntermediario");
        Element cpfCnpj = sub(id, "CpfCnpj");
        if (naoVazio(i.cnpj())) {
            txt(cpfCnpj, "Cnpj", digitos(i.cnpj()));
        } else {
            txt(cpfCnpj, "Cpf", digitos(i.cpf()));
        }
        txt(id, "InscricaoMunicipal", i.inscricaoMunicipal());
        txt(e, "RazaoSocial", i.razaoSocial());
    }

    private void materialUsado(Element pai, DadosDeclaracao d) {
        var m = d.materialUsado();
        if (m == null) {
            return;
        }
        Element e = sub(pai, "DadosMaterialUsado");
        txt(e, "CodigoObra", m.codigoObra());
        txt(e, "Art", m.art());
        if (m.notasUsadas() == null || m.notasUsadas().isEmpty()) {
            return;
        }
        Element lista = sub(e, "ListaNotaUsada");
        for (var nota : m.notasUsadas()) {
            Element n = sub(lista, "NotaUsada");
            Element cpfCnpj = sub(n, "CpfCnpj");
            if (naoVazio(nota.cnpj())) {
                txt(cpfCnpj, "Cnpj", digitos(nota.cnpj()));
            } else {
                txt(cpfCnpj, "Cpf", digitos(nota.cpf()));
            }
            txt(n, "NomeFornecedor", nota.nomeFornecedor());
            txt(n, "NumeroNF", nota.numeroNf());
            if (nota.dataNf() != null) {
                txt(n, "DataNF", nota.dataNf().toString());
            }
            if (nota.itens() == null || nota.itens().isEmpty()) {
                continue;
            }
            Element li = sub(n, "ListaItemUsado");
            for (var item : nota.itens()) {
                Element it = sub(li, "ItemUsado");
                txt(it, "IdItemUsado", item.idItemUsado());
                txt(it, "DescricaoItemUsado", item.descricaoItemUsado());
                txt(it, "IdUnidadeMedida", item.idUnidadeMedida());
                txt(it, "DescricaoUnidadeMedida", item.descricaoUnidadeMedida());
                txt(it, "Quantidade", dec(item.quantidade()));
                txt(it, "ValorTotal", dec(item.valorTotal()));
            }
        }
    }

    private void dadosTomador(Element pai, DadosDeclaracao d, boolean posReforma) {
        var t = d.tomador();
        if (todosVazios(t.cnpj(), t.cpf(), t.identificacao(), t.razaoSocial())) {
            // O tomador e' opcional no padrao nacional: emitir para pessoa fisica
            // sem CPF e' valido, e mandar um bloco vazio nao e'.
            return;
        }

        Element toma = sub(pai, "DadosTomador");

        Element ident = sub(toma, "IdentificacaoTomador");
        Element cpfCnpj = sub(ident, "CpfCnpj");
        if (naoVazio(t.cnpj())) {
            txt(cpfCnpj, "Cnpj", digitos(t.cnpj()));
        } else if (naoVazio(t.cpf())) {
            txt(cpfCnpj, "Cpf", digitos(t.cpf()));
        } else {
            txt(cpfCnpj, "Identificacao", t.identificacao());
        }
        if (naoVazio(t.inscricaoMunicipal())) {
            txt(ident, "InscricaoMunicipal", t.inscricaoMunicipal());
        }

        txt(toma, "RazaoSocial", t.razaoSocial());
        // 1 = Brasil, 2 = Exterior. O exemplo de exterior traz LocalEndereco 2 e
        // um <EnderecoExterior> no lugar de <Endereco> — ver endereco().
        txt(toma, "LocalEndereco", t.localEndereco());

        if (t.exterior()) {
            Element ext = sub(toma, "EnderecoExterior");
            txt(ext, "Descricao", t.logradouro());
            Element pais = sub(ext, "Pais");
            txt(pais, "CodigoPaisBacen", t.codigoPaisBacen());
            txt(ext, "NomeEstado", t.nomeEstado());
            txt(ext, "NomeMunicipio", t.nomeMunicipio());
        } else if (naoVazio(t.logradouro())) {
            Element end = sub(toma, "Endereco");
            txt(end, "TipoLogradouro", t.tipoLogradouro());
            txt(end, "Logradouro", t.logradouro());
            txt(end, "Numero", t.numero());
            txt(end, "Complemento", t.complemento());
            txt(end, "Bairro", t.bairro());

            if (naoVazio(t.codigoMunicipio())) {
                Element mun = sub(end, "Municipio");
                txt(mun, "CodigoMunicipioIBGE", t.codigoMunicipio());
                txt(mun, "Descricao", t.descricaoMunicipio());
                txt(mun, "Uf", t.uf());
            }
            Element pais = sub(end, "Pais");
            txt(pais, "CodigoPaisBacen", naoVazio(t.codigoPaisBacen()) ? t.codigoPaisBacen() : "01058");
            txt(pais, "Descricao", "Brasil");
            txt(end, "Cep", soDigitos(t.cep()));
        }

        if (naoVazio(t.telefone()) || naoVazio(t.email())) {
            Element contato = sub(toma, "Contato");
            txt(contato, "Telefone", soDigitos(t.telefone()));
            txt(contato, "Email", t.email());
        }
        txt(toma, "InscricaoEstadual", t.inscricaoEstadual());
    }

    /**
     * O regime, com a bifurcacao do formato.
     *
     * <p>{@code RegimeEspecialTributacao} so existe <b>antes</b> da reforma. O
     * exemplo de 21/05/2026 nao tem a tag, mesmo sendo prestador de servico
     * normal. As tres flags e o {@code ResponsavelISSQN} existem nos dois.
     */
    private void regime(Element pai, DadosDeclaracao d, boolean posReforma) {
        var r = d.regime();
        // o codigoAtividadeEconomica continua no record do regime porque e' o
        // campo que o ERP ja tem; quem o escreve e' atividadeEconomica()

        if (!posReforma && naoVazio(r.tributacaoEspecialCodigo())) {
            Element ret = sub(pai, "RegimeEspecialTributacao");
            txt(ret, "Codigo", r.tributacaoEspecialCodigo());
            txt(ret, "Descricao", r.tributacaoEspecialDescricao());
        }
        txt(pai, "OptanteSimplesNacional", r.optanteSimplesNacional() ? "1" : "0");
        txt(pai, "OptanteMEISimei", r.optanteMei() ? "1" : "0");
        txt(pai, "ISSQNRetido", r.issqnRetido() ? "1" : "0");

        if (naoVazio(r.responsavelIssqnCodigo())) {
            Element resp = sub(pai, "ResponsavelISSQN");
            txt(resp, "Codigo", r.responsavelIssqnCodigo());
            txt(resp, "Descricao", r.responsavelIssqnDescricao());
        }

        // <b>CodigoAtividadeEconomica nao sai aqui.</b> Quem emite e'
        // atividadeEconomica(), que escolhe entre as tres tags conforme o formato
        // e conforme a parametrizacao do municipio. Emitir aqui produzia a tag
        // duas vezes, e o XSD respondia "Invalid content was found starting with
        // element CodigoAtividadeEconomica. One of BeneficioProcesso,
        // MunicipioIncidencia, ValorServicos is expected".
    }

    /**
     * {@code ExigibilidadeISSQN} e {@code BeneficioProcesso}.
     *
     * <p><b>{@code BeneficioProcesso} e' so do formato antes da reforma.</b> O
     * exemplo de 21/05/2026 nao tem a tag, e ela existe no {@code nfse-v-100.xsd}.
     * Esta e' a segunda razao pela qual o XSD 1.00 nao serve para o formato novo:
     * alem das tags que faltam, a <b>ordem</b> tambem muda. No XSD 1.00 a
     * sequencia e'
     *
     * <pre>
     *   CodigoAtividadeEconomica, CodigoCnaeAtividadeEconomica,
     *   ItemLei116AtividadeEconomica, ExigibilidadeISSQN, BeneficioProcesso
     * </pre>
     *
     * <p>e no exemplo de 2026 o {@code ItemLei116AtividadeEconomica} vem
     * <b>antes</b> do {@code ExigibilidadeISSQN}, e nao depois. Validar o formato
     * novo contra o XSD 1.00 nao da so erro de tag: da erro de posicao.
     */
    private void exigibilidade(Element pai, DadosDeclaracao d, boolean posReforma) {
        var r = d.regime();
        if (naoVazio(r.exigibilidadeCodigo())) {
            Element exig = sub(pai, "ExigibilidadeISSQN");
            txt(exig, "Codigo", r.exigibilidadeCodigo());
            txt(exig, "Descricao", r.exigibilidadeDescricao());
        }
        if (!posReforma) {
            txt(pai, "BeneficioProcesso", d.beneficioProcesso());
        }
    }

    /**
     * A atividade economica, e a tag certa para cada formato.
     *
     * <p><b>A escolha e' do municipio, nao do ERP.</b> O XSD tem tres tags e a
     * documentacao diz que a prefeitura parametriza "tipo de estrutura de
     * atividade economica". O perfil {@code Normal} usa
     * {@code CodigoAtividadeEconomica=62040.00}; o exemplo de 21/05/2026 usa
     * {@code ItemLei116AtividadeEconomica=070901}.
     *
     * <p>No formato depois da reforma, os dois ultimos campos ocupam o lugar do
     * {@code CodigoAtividadeEconomica}, e o {@code CodigoNBS} vem logo depois.
     */
    private void atividadeEconomica(Element pai, DadosDeclaracao d, boolean posReforma) {
        var a = d.atividade();
        if (a == null) {
            return;
        }
        if (a.preenchidos() > 1) {
            throw new NfseNacionalException(
                    "A atividade economica tem tres tags no XSD e a prefeitura parametriza "
                            + "qual delas usar. Preenchidas " + a.preenchidos() + ": "
                            + "CodigoAtividadeEconomica, CodigoCnaeAtividadeEconomica e "
                            + "ItemLei116AtividadeEconomica sao alternativa, e o XML nao aceita "
                            + "mais de uma.");
        }
        if (posReforma) {
            txt(pai, "ItemLei116AtividadeEconomica", a.itemLei116AtividadeEconomica());
            txt(pai, "CodigoNBS", d.reforma() == null ? null : d.reforma().codigoNbs());
        } else {
            txt(pai, "CodigoAtividadeEconomica", a.codigoAtividadeEconomica());
        }
        if (a.codigoCnaeAtividadeEconomica() != null) {
            txt(pai, "CodigoCnaeAtividadeEconomica", a.codigoCnaeAtividadeEconomica());
        }
    }

    /** {@code MunicipioPrestacaoServico}: so no formato depois da reforma. */
    private void municipioPrestacao(Element pai, DadosDeclaracao d, boolean posReforma) {
        if (!posReforma) {
            return;
        }
        var mp = d.municipioPrestacao();
        if (mp == null || vazio(mp.codigoMunicipio())) {
            return;
        }
        Element e = sub(pai, "MunicipioPrestacaoServico");
        txt(e, "CodigoMunicipioIBGE", mp.codigoMunicipio());
        txt(e, "Descricao", mp.descricao());
        txt(e, "Uf", mp.uf());
    }

    /** {@code MunicipioIncidencia}, nos dois formatos. */
    private void incidencia(Element pai, DadosDeclaracao d) {
        var inc = d.incidencia();
        if (inc == null || vazio(inc.codigoMunicipio())) {
            return;
        }
        Element e = sub(pai, "MunicipioIncidencia");
        txt(e, "CodigoMunicipioIBGE", inc.codigoMunicipio());
        txt(e, "Descricao", inc.descricao());
        txt(e, "Uf", inc.uf());
    }

    /**
     * {@code PisCofins}: so no formato depois da reforma, e <b>entre
     * {@code ValorDescontos} e {@code ValorPis}</b>.
     *
     * <p>E' essa posicao que a documentacao mostra, e a ordem importa porque o
     * XSD le sequencia.
     */
    private void pisCofins(Element pai, DadosDeclaracao d) {
        var r = d.reforma();
        if (r == null || (vazio(r.codigoSituacaoTributaria()) && vazio(r.codigoTipoRetencao()))) {
            return;
        }
        Element pc = sub(pai, "PisCofins");
        txt(pc, "CodigoSituacaoTributaria", r.codigoSituacaoTributaria());
        txt(pc, "CodigoTipoRetencao", r.codigoTipoRetencao());
    }

    /**
     * Os valores, com o mapeamento federal aplicado.
     *
     * <p><b>O agrupamento vem antes de escrever qualquer tag de tributo.</b> As
     * retencoes de PIS, COFINS e CSLL somam em {@code ValorCsll}, e as tags
     * {@code ValorPis} e {@code ValorCofins} recebem a reducao de base do IBS/CBS
     * — que e' outra coisa. Ver {@link NfseNacionalMapeamentoTributacao}.
     */
    private void valores(Element pai, DadosDeclaracao d, boolean posReforma) {
        var v = d.valores();

        NfseNacionalMapeamentoTributacao.ParaXml federal =
                mapeamento.mapear(new NfseNacionalMapeamentoTributacao.Impostos(
                        v.retencaoPis(), v.retencaoCofins(), v.retencaoCsll(),
                        v.reducaoBaseIbsCbsPis(), v.reducaoBaseIbsCbsCofins()));

        txt(pai, "ValorServicos", dec(v.servicos()));
        txt(pai, "ValorDescontos", dec(v.descontos()));

        // so no formato novo, e antes de ValorPis
        if (posReforma) {
            pisCofins(pai, d);
        }

        txt(pai, "ValorPis", dec(federal.valorPis()));
        txt(pai, "ValorCofins", dec(federal.valorCofins()));
        txt(pai, "ValorInss", dec(v.inss()));
        txt(pai, "ValorIrrf", dec(v.irrf()));
        txt(pai, "ValorCsll", dec(federal.valorCsll()));
        txt(pai, "ValorOutrasRetencoes", dec(v.outrasRetencoes()));

        // O ISSQN e' municipal: a NT 007/2026 nao o toca e ele fica nos campos
        // proprios, nos dois formatos.
        txt(pai, "ValorBaseCalculoISSQN", dec(v.baseCalculoIssqn()));
        txt(pai, "AliquotaISSQN", dec(v.aliquotaIssqn()));
        txt(pai, "ValorISSQNCalculado", dec(v.issqnCalculado()));
        if (v.tipoAbatimento() != null) {
            txt(pai, "TipoAbatimento", v.tipoAbatimento());
            txt(pai, "PercAbatimento", dec(v.percAbatimento()));
        }
        txt(pai, "ValorISSQNRecolher", dec(v.issqnRecolher()));
        if (!posReforma) {
            txt(pai, "ValorDeducaoConstCivil", dec(v.deducaoConstrucaoCivil()));
        }
        // O ERP manda o liquido que ele ja tem. Nao recalculado: ver
        // NfseNacionalMapeamentoTributacao.porQueNaoRecalculaLiquido().
        txt(pai, "ValorLiquido", dec(v.liquido()));
        txt(pai, "Observacao", v.observacao());
        if (!posReforma) {
            txt(pai, "Complemento", naoVazio(v.complemento())
                    ? v.complemento() : "Gerado via WebService");
        }
    }

    private void listaServico(Element pai, DadosDeclaracao d, boolean posReforma) {
        var it = d.servico();
        Element lista = sub(pai, "ListaServico");
        Element item = sub(lista, "DadosServico");
        txt(item, "Discriminacao", it.discriminacao());
        txt(item, "CodigoCnae", it.codigoCnae());
        // <b>ItemLei116 so no formato antigo.</b> No novo, o codigo do servico foi
        // para o nivel da declaracao: ItemLei116AtividadeEconomica + CodigoNBS.
        if (!posReforma) {
            txt(item, "ItemLei116", it.itemLei116());
        }
        txt(item, "Quantidade", dec(it.quantidade()));
        txt(item, "ValorServico", dec(it.valorServico()));
        txt(item, "ValorDesconto", dec(it.valorDesconto()));
    }

    private void cartaCorrecao(Element pai, DadosDeclaracao d) {
        var c = d.cartaCorrecao();
        if (c == null || vazio(c.dados())) {
            return;
        }
        Element lista = sub(pai, "ListaCartaCorrecao");
        Element item = sub(lista, "DadosCartaCorrecao");
        item.setTextContent(c.dados());
    }

    // ------------------------------------------------------------------
    // DOM
    // ------------------------------------------------------------------

    private static Document novoDocumento() {
        try {
            Document doc = javax.xml.parsers.DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().newDocument();
            doc.appendChild(doc.createElementNS(NS, "GerarNfseEnvio"));
            return doc;
        } catch (Exception e) {
            throw new NfseNacionalException("Falha ao criar o documento: " + e.getMessage(), e);
        }
    }

    private static Element sub(Element pai, String nome) {
        Element e = pai.getOwnerDocument().createElementNS(NS, nome);
        pai.appendChild(e);
        return e;
    }

    /** Tag com texto. Ausente ou vazia nao entra: e' assim que o XSD trata. */
    private static void txt(Element pai, String nome, String valor) {
        if (valor == null || valor.isBlank()) {
            return;
        }
        Element e = pai.getOwnerDocument().createElementNS(NS, nome);
        e.setTextContent(valor);
        pai.appendChild(e);
    }

    /**
     * Decimal com ponto.
     *
     * <p>Ponto, nunca virgula: a prefeitura valida contra XSD, e os exemplos
     * do material usam {@code 1000.00}. Com 2 casas, porque e' o que o
     * {@code AliquotaISSQN} de {@code 5.0} dos exemplos e' depois de formatado.
     */
    public static String dec(BigDecimal v) {
        return v == null ? "0.00" : v.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static String digitos(String s) {
        return s == null ? "" : s.replaceAll("\\D", "");
    }

    /** So os digitos, ou null se nao sobrou nada. CEP com ponto e barra. */
    private static String soDigitos(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        String d = s.replaceAll("\\D", "");
        return d.isEmpty() ? null : d;
    }

    private static boolean preenchido(String s) {
        return s != null && !s.isBlank();
    }

    private static boolean vazio(String s) {
        return s == null || s.isBlank();
    }

    private static boolean naoVazio(String s) {
        return s != null && !s.isBlank();
    }

    private static boolean todosVazios(String... ss) {
        for (String s : ss) {
            if (naoVazio(s)) {
                return false;
            }
        }
        return true;
    }
}
