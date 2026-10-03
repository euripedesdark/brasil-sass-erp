package br.com.brasil_saas.shared.tenant;

import br.com.brasil_saas.shared.security.AuthenticatedUser;

/**
 * De qual empresa e' a gravacao — o dado que o INSERT precisa e que ninguem
 * preenche sozinho.
 *
 * <p><b>Por que existe.</b> As 84 entidades que estendem {@code TenantEntity}
 * tem {@code empresa_id NOT NULL} e {@code @TenantId}. O Hibernate cuida da
 * <em>leitura</em>: acrescenta {@code empresa_id = :tenant} em toda query. A
 * <em>gravacao</em> e' com o controller: cada {@code criar} precisa dizer de
 * qual empresa e'. Sem isso o INSERT vai com {@code empresa_id} nulo e o banco
 * recusa com violacao de FK — devolvida como 409, que parece duplicidade mas
 * e' falta do tenant. Foi o que fez criar categoria, marca, servico e
 * unidade de medida nunca funcionarem.
 *
 * <p><b>Por que nao do header.</b> Existia um caminho em que o cliente mandava
 * {@code X-Empresa-Id} e o backend acreditava. Isso e' um IDOR de uma linha:
 * trocar o header e' trocar de empresa. Aqui nao ha header. O
 * {@code empresaId} sai do principal que o {@code JwtAuthenticationFilter}
 * montou a partir do banco — a fonte que nao depende do cliente.
 *
 * <p><b>Por que nao {@code empresaParaGravar()}.</b> Aquele metodo existe para
 * o superuser, que ve todas as empresas mas nao pertence a nenhuma: ele recusa
 * sem destino declarado. E' a regra certa para o caso geral, mas o superuser
 * deste ERP tem empresa no vinculo ({@code bc_core_usuario_empresa}), entao o
 * principal tem {@code empresaId} e gravar nele e' o comportamento correto.
 * Aqui a unica regra que vale sempre: <b>empresa ausente e' erro, nunca
 * gravacao sem destino.</b>
 */
public final class EmpresaDaGravacao {

    private EmpresaDaGravacao() {
    }

    /**
     * A empresa que vai receber a gravacao, ou erro se nao houver.
     *
     * @throws IllegalStateException se o usuario nao tem empresa definida
     */
    public static Long de(AuthenticatedUser usuario) {
        Long empresaId = (usuario == null) ? null : usuario.getEmpresaId();
        if (empresaId == null) {
            throw new IllegalStateException(
                "Usuario sem empresa tentando gravar. O vinculo usuario-empresa precisa "
                + "existir antes da primeira gravacao — gravar com empresa_id nulo cria "
                + "linha que nenhuma empresa ve, que e' o pior estado possivel para um "
                + "cadastro.");
        }
        return empresaId;
    }
}
