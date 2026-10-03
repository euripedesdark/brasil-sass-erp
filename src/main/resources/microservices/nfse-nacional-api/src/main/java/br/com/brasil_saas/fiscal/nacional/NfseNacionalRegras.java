package br.com.brasil_saas.fiscal.nacional;

import java.util.ArrayList;
import java.util.List;

/**
 * O que o ERP precisa mandar, e o que a prefeitura exige.
 *
 * <p><b>Estas regras sao todas do material do AGILIBlue</b> — do XSD
 * {@code nfse-v-100.xsd} e dos 10 exemplos de {@code GerarNfse} que vieram com
 * os perfis de tributacao. Nenhuma vem da Prefeitura de Sao Paulo, e nenhuma foi
 * inventada: cada uma existe porque um dos arquivos do material mostra o
 * formato, ou porque o XSD declara o tipo.
 *
 * <p><b>Por que validar aqui e nao so na prefeitura.</b> A documentacao lista
 * a validacao em XSD como a segunda de tres etapas, e cada etapa so roda se a
 * anterior passou. Um erro de tag chega como recusa de assinatura — e quem
 * depura passa a tarde mexendo no certificado, que esta perfeito. Aqui o erro
 * sai com o nome do campo.
 */
@org.springframework.stereotype.Component
public class NfseNacionalRegras {

    /**
     * Os problemas, em ordem de gravidade.
     *
     * @return lista vazia significa que pode ir para o XSD
     */
    public List<String> validar(NfseNacionalController.EmissaoRequest req,
                                NfseNacionalProperties props) {
        List<String> p = new ArrayList<>();
        if (req == null) {
            return List.of("requisicao vazia");
        }

        // --- o que a prefeitura exige e nao se deduz ---------------
        if (vazio(props.getUnidadeGestora())) {
            p.add("UnidadeGestora ausente: e' o CNPJ da Prefeitura de Rondonopolis, "
                    + "e a documentacao diz que se obtem com a administracao municipal "
                    + "(nfse.nacional.unidade-gestora)");
        }
        if (vazio(props.getCnpjPrestador())) {
            p.add("CNPJ do prestador ausente (nfse.nacional.cnpj-prestador)");
        } else if (soDigitos(props.getCnpjPrestador()).length() != 14) {
            p.add("CNPJ do prestador com " + soDigitos(props.getCnpjPrestador()).length()
                    + " digitos, e o esperado e' 14");
        }
        // A Inscricao Municipal ENTRA nesta lista, e o motivo esta no servidor.
        //
        // <p>Eu tinha tirado daqui porque o XSD declara minOccurs="0". A
        // prefeitura respondeu E16 — "Inscricao municipal do prestador do
        // servico nao informada. Corrija e tente novamente" — em homologacao.
        // <b>minOccurs="0" no XSD nao significa dispensavel</b>: o schema permite
        // omitir, o servidor exige. E o unico que decide e' o servidor.
        if (vazio(props.getInscricaoMunicipalPrestador())) {
            p.add("Inscricao Municipal do prestador ausente. O XSD aceita sem ela "
                    + "(minOccurs=0), mas a prefeitura respondeu E16: "
                    + "\"Inscricao municipal do prestador do servico nao informada\"");
        }

        // A Inscricao Municipal NAO entra nesta lista, de proposito.
        //
        // <p>O XSD declara <xsd:element name="InscricaoMunicipal"
        // minOccurs="0"/>, porque o prestador pode nao estar cadastrado no
        // Database de Contribuinentes. <b>Exigir aqui o que o schema nao exige
        // trava a emissao de um prestador que a prefeitura aceita</b>, e o
        // sintoma e' um 422 local com uma exigencia que a prefeitura jamais
        // fez. Quem decide e' a prefeitura, na chamada. O aviso fica no
        // health, em NfseNacionalController.status().

        // --- autenticacao: uma das duas, nunca as duas -----------------
        boolean temChave = !vazio(props.getChaveDigital());
        boolean temCertificado = !vazio(props.getCertificadoCaminho());
        if (temChave && temCertificado) {
            p.add("ChaveDigital e certificado estao configurados ao mesmo tempo. O XSD do "
                    + "AGILIBlue aceita UM ou o outro no IdentificacaoPrestador, nunca os dois.");
        } else if (!temChave && !temCertificado) {
            p.add("Nenhuma autenticacao configurada: sem ChaveDigital o XSD exige "
                    + "dsig:Signature, que precisa do certificado A1");
        }
        if (temChave && props.getChaveDigital().trim().length() != 32) {
            // O comprimento e da string, nao dos digitos. Medir depois de tirar
            // as letras dava nunca-32 num MD5 com letras - que e' o caso normal -
            // e a recusa saia em toda emissao com chave valida.
            p.add("ChaveDigital com " + props.getChaveDigital().trim().length()
                    + " caracteres. O tipo tsChaveDigital do XSD e' MD5: 32 caracteres, "
                    + "com letras de a a f.");
        }

        // --- o que o ERP mandou ------------------------------------------
        if (vazio(req.discriminacao())) {
            p.add("discriminacao do servico vazia");
        }
        if (vazio(req.itemLei116())) {
            p.add("ItemLei116 vazio");
        } else if (!req.itemLei116().matches("\\d{1,2}\\.\\d{2}")) {
            // Os exemplos do material usam 1.06, e o padrao nacional usa ponto.
            // Sem ponto, a prefeitura procura um item que nao existe.
            p.add("ItemLei116 fora do formato: veio '" + req.itemLei116()
                    + "' e o padrao nacional e' N.NN, com ponto (exemplo do material: 1.06)");
        }
        if (req.valorServicosN() == null) {
            p.add("valor do servico ausente");
        } else if (req.valorServicosN().signum() < 0) {
            p.add("valor do servico negativo");
        }
        if (vazio(req.codigoMunicipioIncidencia())) {
            p.add("MunicipioIncidencia ausente. O padrao nacional nao presume: em Sao Paulo o "
                    + "municipio era implicito, aqui nao e'.");
        } else if (!req.codigoMunicipioIncidencia().matches("\\d{7}")) {
            p.add("CodigoMunicipioIBGE com " + req.codigoMunicipioIncidencia().length()
                    + " digitos, e o esperado e' 7 (Rondonopolis e' 5107602)");
        }
        // A atividade economica tem TRES tags no XSD e a prefeitura parametriza
        // qual usar. Preencher duas e' o erro de de/para que produz nota invalida.
        int atividades = (vazio(req.codigoAtividadeEconomica()) ? 0 : 1)
                + (vazio(req.codigoCnaeAtividadeEconomica()) ? 0 : 1)
                + (vazio(req.itemLei116AtividadeEconomica()) ? 0 : 1);
        if (atividades > 1) {
            p.add("Atividade economica preenchida " + atividades + " vezes: "
                    + "CodigoAtividadeEconomica, CodigoCnaeAtividadeEconomica e "
                    + "ItemLei116AtividadeEconomica sao alternativas, e a prefeitura "
                    + "parametrizou uma so. O XSD recusa mais de uma.");
        } else if (atividades == 0) {
            p.add("Nenhuma atividade economica informada");
        }
        if (vazio(req.codigoAtividadeEconomica())
                && preenchido(req.codigoCnaeAtividadeEconomica())) {
            p.add("CodigoCnaeAtividadeEconomica fora do formato: veio '"
                    + req.codigoCnaeAtividadeEconomica()
                    + "' e o padrao e' NNNNN.NN, com ponto (exemplo do material: 62040.00)");
        } else if (preenchido(req.codigoAtividadeEconomica())
                && !req.codigoAtividadeEconomica().matches("\\d{4,7}\\.\\d{2}")) {
            // Os exemplos trazem 62040.00. A documentacao avisa que o codigo
            // depende de como a prefeitura configurou a atividade, e que por
            // CNAE sao tres niveis.
            p.add("CodigoAtividadeEconomica fora do formato: veio '"
                    + req.codigoAtividadeEconomica()
                    + "' e o padrao e' NNNNN.NN, com ponto (exemplo do material: 62040.00)");
        }
        if (vazio(req.exigibilidadeIssqn())) {
            p.add("ExigibilidadeISSQN vazio: o AGILIBlue usa -1 (exigivel) nos exemplos");
        }
        if (vazio(req.responsavelIssqn())) {
            p.add("ResponsavelISSQN vazio: o AGILIBlue usa -3 (prestador) nos exemplos");
        }

        // --- coerencia dos valores ---------------------------------------
        if (req.valorIssqnCalculadoN() != null && req.aliquotaN() != null
                && req.valorServicosN() != null) {
            var esperado = req.valorServicosN()
                    .multiply(req.aliquotaN())
                    .divide(new java.math.BigDecimal("100"), 2,
                            java.math.RoundingMode.HALF_UP);
            // Tolerancia de 1 centavo: arredondamento no meio do caminho e'
            // normal, e recusar por causa disso trava a emissao de um valor
            // que esta certo.
            if (req.valorIssqnCalculadoN().subtract(esperado).abs()
                    .compareTo(new java.math.BigDecimal("0.01")) > 0) {
                p.add("ValorISSQNCalculado (" + req.valorIssqnCalculadoN()
                        + ") nao bate com ValorServicos x AliquotaISSQN (" + esperado
                        + "). A prefeitura recalcula e a divergencia vira erro de "
                        + "apuracao, nao de formato");
            }
        }

        // --- o que so importa em producao --------------------------------
        if (!props.isHomologacao() && (vazio(props.getUnidadeGestora()))) {
            p.add("sem UnidadeGestora nao ha como emitir em producao");
        }

        return p;
    }

    private static boolean preenchido(String s) {
        return s != null && !s.isBlank();
    }

    private static boolean vazio(String s) {
        return s == null || s.isBlank();
    }

    private static String soDigitos(String s) {
        return s == null ? "" : s.replaceAll("\\D", "");
    }
}
