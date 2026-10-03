package br.com.brasil_saas.fiscal.sp;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuracao da integracao com a NFS-e da Prefeitura de Sao Paulo.
 *
 * <p>Todas as regras aqui foram validadas contra o WebService de producao em
 * 25/09/2026, com o certificado A1 da SrvCloud. Ver
 * {@code src/main/resources/microservices/nfse-sp-bridge/brasil-saas.md}.
 *
 * <p>Precedencia: variavel de ambiente > application.yml > valor padrao.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "brasil-saas.fiscal.nfse-sp")
public class NfseSpProperties {

    /** Liga/desliga a integracao. Desligado por padrao. */
    private boolean enabled = false;

    /** Endereco do WebService sincrono. */
    private String url = "https://nfews.prefeitura.sp.gov.br/lotenfe.asmx";

    /**
     * CNPJ do prestador, sem mascara. Vai no {@code CPFCNPJRemetente} de toda
     * mensagem.
     */
    private String cnpjRemetente;

    /**
     * Versao do leiaute (XSD) enviado no cabecalho.
     *
     * <p>1 = classico. 2 = leiaute IBS/CBS da LC 214/2025.
     *
     * <p>A escolha NAO e do integrador: a prefeitura compara com o cadastro
     * do prestador e responde o erro 641
     * ("Contribuinte cadastrado como Simples Nacional na data informada.
     * Devera ser utilizado o leiaute 1") quando nao bate.
     *
     * <p>A SrvCloud e do Simples Nacional: use 1.
     */
    private int xsdVersion = 1;

    /** Caminho do certificado A1 (.pfx ou .p12). */
    private String certificadoCaminho;

    /**
     * Senha do .pfx.
     *
     * <p>Por seguranca a senha NAO tem valor padrao e nao vai para o banco.
     * Vem de {@code NFSE_SP_CERT_PASS}. Quem abre o certificado guarda a senha
     * na propria estacao.
     */
    private String certificadoSenha;

    /** Timeout de conexao, em segundos. A prefeitura e lenta (1s por chamada). */
    private int timeoutConexaoSegundos = 20;

    /** Timeout de leitura, em segundos. */
    private int timeoutLeituraSegundos = 120;

    /** Path interno da assinatura do RPS. Fica em false para nao expor no actuator. */
    private boolean registrarAssinatura = false;
}
