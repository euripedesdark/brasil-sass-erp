package br.com.brasil_saas.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Como o ERP monta a identidade de quem entra.
 *
 * <p><b>O que existia antes e por que foi tirado.</b> Quando o
 * {@code AuthServiceImpl} provisionava o usuario ERP de uma role
 * {@code SUPERUSER} do PostgreSQL, ele montava o email como
 * {@code username + "@localhost"} — string fixa, escrita a mao. O mesmo
 * acontecia em {@code UsuarioAdminController} quando o cadastro vinha sem email.
 * A migration {@code V89} tambem nasceu com um {@code sysdba@localhost}
 * embutido.
 *
 * <p>{@code localhost} e' um nome de loopback, nao um dominio. Ele nao identifica
 * ninguem, nao pode receber mensagem e — o que de fato acontece — gravou
 * Identidade FALSA no cadastro: a tela mostrava um email que nunca existiu, e o
 * dominio de verdade da pessoa sumia. Pior, o valor gravado na primeira vez era
 * o que ficava para sempre: corrigir o codigo depois nao corrige a linha.
 *
 * <p><b>O que e' agora.</b> O dominio vem daqui, e o padrao e' o mesmo do
 * Active Directory: o UPN do Active Directory tem a forma
 * {@code usuario@dominio}, entao {@code euripedes@srvcloud.cloud} e' a identidade
 * que o AD ja usa para essa pessoa. O cadastro do ERP passa a falar a mesma
 * lingua do AD, e nao um dominio inventado.
 *
 * <p><b>Por que uma propriedade e nao uma constante.</b> porque o dominio
 * correto depende de onde o ERP esta instalado. Em outro dominio, e' so trocar a
 * variavel de ambiente.
 */
@Component
@ConfigurationProperties(prefix = "brasil-saas.identidade")
@Getter
@Setter
public class IdentidadeProperties {

    /**
     * Dominio usado para montar o email de quem ainda nao tem um.
     *
     * <p>Padrao: o realm do AD desta instalacao ({@code SRVCLOUD.CLOUD}).
     * Variavel de ambiente: {@code BRASIL_SAAS_IDENTIDADE_DOMINIO}.
     */
    private String dominioPadrao = "srvcloud.cloud";

    /**
     * Monta o email no formato do UPN do AD: {@code usuario@dominio}.
     *
     * <p>Se o dominio vier vazio por configuracao errada, o metodo nao volta a
     * inventar um: devolve o proprio username, que pelo menos nao afirma um
     * dominio falso. Email ausente o banco aceita; email falso e' o que causou
     * o problema.
     *
     * @param username nome de usuario, ja sem o prefixo {@code DOMINIO\}
     * @return o email no formato {@code usuario@dominio}
     */
    public String emailPara(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        String dominio = dominioPadrao == null ? "" : dominioPadrao.trim();
        if (dominio.isEmpty()) {
            return username;
        }
        return username + "@" + dominio;
    }
}
