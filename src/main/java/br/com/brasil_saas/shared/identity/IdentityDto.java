package br.com.brasil_saas.shared.identity;

import java.util.List;

/**
 * Contrato oficial do Auth Service.
 *
 * <p><b>Por que o ERP conhece este registro.</b> O Auth Service e' a autoridade de
 * identidade da plataforma. Quando ele existe, o ERP nao pergunta "a qual grupo
 * essa pessoa pertence" ao Active Directory: pergunta ao Auth Service, que ja
 * perguntou. Este registro e' a copia do contrato publicado, palavra por palavra.
 *
 * <p><b>Os nomes dos campos nao sao nossos.</b> identityId, username, provider e
 * groups sao os quatro campos que o servico publica. Se um deles mudar de nome, o
 * contrato mudou e a mudanca e do servico, nao deste arquivo.
 *
 * <p><b>O que este registro nao e.</b> Nao e cache, nem copia local, nem fonte
 * de identidade. Ele vale enquanto dura a requisicao que o pediu.
 *
 * @param identityId identificador canonico da pessoa, conforme o Auth Service
 * @param username   rotulo de login; nao e chave e pode se repetir entre provedores
 * @param provider   de onde a identidade veio (AD, POSTGRES, LINUX, CERTIFICADO)
 * @param groups     grupos que o Auth Service devolveu
 */
public record IdentityDto(
        String identityId,
        String username,
        String provider,
        List<String> groups) {

    /**
     * Devolve os grupos nunca nulos, para o chamador nao lidar com lista nula.
     *
     * @return a lista de grupos, ou lista vazia quando o servico devolveu nulo
     */
    public List<String> gruposOuVazio() {
        return groups == null ? List.of() : groups;
    }
}
