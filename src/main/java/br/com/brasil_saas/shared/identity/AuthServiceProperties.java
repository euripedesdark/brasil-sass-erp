package br.com.brasil_saas.shared.identity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Onde esta o Auth Service e como o ERP fala com ele.
 *
 * <p><b>Por que uma propriedade e nao uma constante.</b> O endereco do Auth
 * Service depende de onde o ERP esta instalado. Em outro ambiente e' so trocar
 * a variavel de ambiente, sem recompilar e sem tocar em codigo.
 *
 * <p><b>Por que enabled existe e nao pode ser removido.</b> Enquanto o corte do
 * LDAP nao estiver decidido, o ERP precisa continuar lendo grupo do jeito antigo.
 * Desligar e o que mantem o caminho velho funcionando; e nao um atalho, e o
 * interruptor do corte.
 *
 * <p><b>Este arquivo nao guarda segredo nenhum.</b> Nao ha senha, nao ha bind, nao
 * ha DN de servico. O Auth Service e quem fala com o AD; o ERP so precisa saber
 * o endereco e qual provider asking.
 */
@Component("identityAuthServiceProperties")
@ConfigurationProperties(prefix = "brasil-saas.auth-service")
@Getter
@Setter
public class AuthServiceProperties {

    /**
     * Liga ou desliga o consumo do Auth Service.
     *
     * <p>Padrao {@code false}: o padrao e o sistema que ja roda hoje, sem
     * depender de um servico que pode nao estar no ar.
     *
     * <p>Variavel de ambiente: {@code BRASIL_SAAS_AUTH_SERVICE_ENABLED}.
     */
    private boolean enabled = false;

    /**
     * Endereco base do Auth Service, sem barra no fim.
     *
     * <p>Variavel de ambiente: {@code BRASIL_SAAS_AUTH_SERVICE_BASE_URL}.
     */
    private String baseUrl = "http://192.168.2.10:8181";

    /**
     * Qual origem de identidade pedir ao Auth Service.
     *
     * <p>O valor vai no corpo do {@code POST /api/v1/identity/authenticate}, no
     * campo {@code provider} do contrato. {@code AD} e o que existe nesta
     * instalacao; o Auth Service tambem conhece POSTGRES, LINUX e CERTIFICADO.
     */
    private String provider = "AD";

    /**
     * Tempo limite para abrir a conexao, em milissegundos.
     *
     * <p>Curto de proposito: quem espera muito por grupo trava a requisicao de
     * todo mundo. Erro de connect e' preferivel a espera longa.
     */
    private int connectTimeoutMs = 3000;

    /**
     * Tempo limite de leitura da resposta, em milissegundos.
     */
    private int readTimeoutMs = 5000;
}
