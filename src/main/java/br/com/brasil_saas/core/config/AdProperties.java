package br.com.brasil_saas.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Conexao do ERP com o Active Directory.
 *
 * <p><b>Por que keytab e nao senha.</b> O ERP nao tem senha de nobody aqui: ele
 * se apresenta ao AD com uma <em>chave</em>, guardada num arquivo que so o root e
 * o usuario do servico leem. E o mesmo mecanismo que o Windows usa, que e o que
 * torna o SSO nativo em vez de um arranjo — o navegador da maquina unida ao
 * dominio manda o tiquete, e o ERP valida com a mesma chave que valida para
 * qualquer outro cliente Kerberos.
 *
 * <p><b>Desligado por padrao.</b> {@code enabled} comeca em {@code false}. Um
 * sistema de producao nao pode subirDepending de um keytab que talvez nao esteja
 * la, e nem todo ambiente tem AD. Com desligado, o ERP funciona exatamente como
 * antes: perfil no Postgres. Ligar e uma decisao de ambiente, nao de build.
 *
 * <p><b>As duas fontes.</b> O grupo do AD <em>soma</em> ao que o perfil no
 * Postgres ja concedia, nunca substitui. Perder o AD nao tira ninguem do
 * sistema — tira o acrescimo, e o log avisa.
 */
@Component
@ConfigurationProperties(prefix = "brasil-saas.ad")
@Getter
@Setter
public class AdProperties {

    /** Liga a leitura de grupos no AD. Desligado = o ERP se comporta como sempre. */
    private boolean enabled = false;

    /**
     * URL do LDAP do DC. O DC tambem e servidor de LDAP, entao costuma ser o
     * proprio host: {@code ldap://dc-erp.srvcloud.cloud:389}.
     */
    private String url = "ldap://dc-erp.srvcloud.cloud:389";

    /** Base da busca: a raiz do dominio. */
    private String baseDn = "DC=srvcloud,DC=cloud";

    /**
     * Keytab do servico. Tem duas chaves: a de cliente (para o ERP pedir tiquete
     * e consultar grupos) e a de servico {@code HTTP/<host>} (para validar o
     * tiquete que o navegador manda no SPNEGO).
     */
    private String keytab = "/etc/srvcloud/certs/srvcloud.keytab";

    /** Principal de cliente: a conta de servico, no formato UPN. */
    private String principal = "srvcloud-svc@srvcloud.cloud";

    /**
     * Principal de servico do SPNEGO. Precisa casar com o que o navegador pede,
     * que e {@code HTTP} seguido do hostname que o usuario digitou. Por isso o
     * FQDN e lido do request, e nao fixado aqui.
     */
    private String servicePrincipal = "HTTP/dc-erp.srvcloud.cloud";

    /**
     * Grupo que permite criar sessao. Quem nao esta nele fica com o que o perfil
     * no Postgres der, e nao ganha nada do AD — que e o comportamento seguro:
     * um grupo novo no AD nunca tranca ninguem por acidente.
     */
    private String grupoAcesso = "GRP_ERP_ACESSO";

    /** Grupo que concede {@code ROLE_ADMIN}, usado nos guards hasAnyRole(...). */
    private String grupoAdmin = "GRP_ERP_ADMIN";

    /**
     * Prefixo dos grupos que resolvem a empresa. O sufixo e' o CNPJ:
     * {@code ERP_EMPRESA_00000000000191} e' a empresa 00000000000191.
     */
    private String prefixoGrupoEmpresa = "ERP_EMPRESA_";

    /**
     * Quanto tempo vale a leitura de grupos, em segundos.
     *
     * <p>Existe porque o filtro de JWT recarrega o usuario <em>a cada
     * requisicao</em>. Sem cache, cada clique na tela seria uma consulta no AD.
     * O preco de manter o cache e que uma mudanca de grupo vale ate expiresSegundos
     * — o que e aceitavel, porque quem entra no grupo para usar o sistema ja o
     * tinha antes de entrar.
     */
    private long cacheSegundos = 300;

    /**
     * Onde o cache de grupos fica gravado.
     *
     * <p><b>Por que disco e nao so memoria.</b> Tres motivos, todos práticos.
     * Primeiro, um restart do ERP nao joga fora o que o ERP ja sabia — quem entra
     * logo apos a subida nao espera consulta nenhuma. Segundo, e o mais
     * importante: se o AD estiver fora do ar, o ERP le o <em>ultimo estado
     * conhecido</em> do disco e continua autorizando em vez de derrubar
     * autoridade de todo mundo. Terceiro, o arquivo e legivel por um humano e por
     * outra instancia do ERP, o que o cache em memoria nao seria.
     *
     * <p>O arquivo e reescrito inteiro a cada consulta bem-sucedida. E um
     * arquivo pequeno — um registro por usuario consultado, so os nomes dos
     * grupos — entao reescrever e barato e evita corromper por escrita parcial.
     */
    private String arquivoCache = "/var/lib/brasil-saas/cache/grupos-ad.json";

    /**
     * Quanto tempo o ERP para de tentar o AD depois que uma consulta falha, em
     * segundos. Sem isso, um AD fora do ar vira uma consulta por requisicao que
     * só volta a falhar depois do timeout.
     */
    private long cooldownFalhaSegundos = 30;

    /**
     * Se true, quem nao tem {@link #grupoAcesso} e' barrado mesmo tendo perfil no
     * Postgres. Default false: as duas fontes somam, e o AD nunca tira acesso
     * que o Postgres concedeu.
     */
    private boolean exigirGrupoAcesso = false;
}
