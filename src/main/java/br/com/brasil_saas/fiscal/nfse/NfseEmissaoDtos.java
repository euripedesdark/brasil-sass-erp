package br.com.brasil_saas.fiscal.nfse;

import br.com.brasil_saas.cadastro.model.Servico;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/** Dados para emitir uma NFS-e da Prefeitura de Sao Paulo. */
public final class NfseEmissaoDtos {

    private NfseEmissaoDtos() {
    }

    /**
     * O que o usuario preenche na tela de emissao.
     *
     * <p>{@code servicoId} vem do cadastro: e dele que saem o codigo municipal
     * (que vai no RPS e na assinatura), o NBS e a aliquota. Pedir o codigo na
     * tela permitiria mandar um codigo que nao bate com o cadastro.
     */
    @Data
    @Builder
    public static class Emitir {
        private Long empresaId;
        private Long servicoId;
        private Long clienteId;
        private Long pessoaId;

        /** Tomador. CPF ou CNPJ, sem mascara. */
        private String cpfCnpjTomador;
        private String razaoSocialTomador;
        private String emailTomador;
        private String inscricaoMunicipalTomador;

        private BigDecimal valorServicos;
        private BigDecimal valorDeducoes;
        private BigDecimal aliquota;
        private String discriminacao;
        private String dataEmissao;

        private String serieRps;
        private Long numeroRps;

        /** Tributacao do RPS: T (tributado em SP), F, A, B, I, P. Padrao T. */
        private String tributacaoRps;
        private Boolean issRetido;
    }

    /** O que a prefeitura devolve. */
    public record Resultado(
            Long nfseId,
            String numeroNfse,
            String codigoVerificacao,
            String chaveNotaNacional,
            String inscricaoMunicipal,
            String status,
            String xmlArquivado,
            String pdfArquivado,
            java.util.List<String> alertas) {
    }

    /** Confirma o que o servico precisa ter para poder emitir. */
    public static void exigirCodigoMunicipal(Servico servico) {
        if (servico == null) {
            throw new br.com.brasil_saas.shared.exception.BusinessException("Servico nao informado.");
        }
        if (servico.getCodigoTributacaoMunicipal() == null
                || servico.getCodigoTributacaoMunicipal().isBlank()) {
            throw new br.com.brasil_saas.shared.exception.BusinessException(
                    "O servico '" + servico.getNome() + "' (codigo " + servico.getCodigo()
                            + ") nao tem codigo de tributacao municipal de Sao Paulo. "
                            + "Cadastre-o na tela de Cadastro de Servicos antes de emitir: "
                            + "sem ele a prefeitura recusa com o erro 306. "
                            + "Para suporte tecnico em informacao, por exemplo, e 2919 "
                            + "(LC 116 01.07).");
        }
    }
}
