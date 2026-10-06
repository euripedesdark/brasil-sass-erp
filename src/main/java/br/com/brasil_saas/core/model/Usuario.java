package br.com.brasil_saas.core.model;

import br.com.brasil_saas.shared.model.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "bc_core_usuario", schema = "brasil_saas")
@Getter
@Setter
public class Usuario extends BaseEntity {

    /**
     * A empresa do usuario, ou ausente enquanto ele ainda nao cadastrou.
     *
     * <p><b>Por que este campo esta aqui e nao no {@code TenantEntity}.</b> O
     * {@code @TenantId} faz o Hibernate gravar no {@code INSERT} o tenant
     * corrente da sessao. No login nao ha principal autenticado, o resolver
     * devolve {@code SEM_FILTRO = -1}, e o usuario novo nascia com
     * {@code empresa_id = -1} — que viola a FK
     * {@code bc_core_usuario_empresa_id_fkey} e devolve 409. Nenhum usuario do
     * AD conseguia ser criado, entao o fluxo documentado em
     * {@code MinhaEmpresaController} ("o primeiro acesso cadastra a empresa")
     * nunca chegava a rodar.
     *
     * <p><b>Por que nao uma linha sentinela -1 em {@code bc_core_empresa}.</b>
     * {@code EmpresaTenantIdentifierResolver.isRoot(-1)} e' verdadeiro, entao
     * o usuario ficaria sem filtro de tenant — vendo as empresas dos outros.
     * Aqui o estado e' {@code null} de verdade: a coluna aceita null no banco,
     * e {@code usuario.getEmpresaId() == null} cai no mesmo caminho de
     * "sem empresa" que o resto do codigo ja trata.
     *
     * <p><b>Por que a listagem nao quebra.</b> Quem precisa de isolamento ja
     * filtra na query: {@code findAllByEmpresaIdAndDeletedAtIsNull(empresaId)}.
     * Os {@code COUNT} de perfil ({@code contarSuperusersAtivos},
     * {@code contarAtivosComPerfilExceto}) sao documentados como "em qualquer
     * empresa", e as buscas de login sao por username ou id. Nenhum deles
     * dependia do filtro que o {@code @TenantId} acrescentava.
     *
     * <p>O cadastro e' por {@code UsuarioRepository.vincularEmpresa(id, empresaId)},
     * chamado do {@code POST /api/core/minha-empresa}.
     */
    @Column(name = "empresa_id")
    private Long empresaId;

    @Column(length = 150)
    private String nome;

    @Column(length = 50, unique = true)
    private String username;

    @Column(length = 150)
    private String email;

    /**
     * A senha nunca sai por JSON.
     *
     * <p><b>Por que o {@code @JsonIgnore} esta aqui e nao no DTO.</b> O
     * {@code UsuarioAdminController.listar()} devolvia a entidade
     * {@code Usuario} inteira, e o Lombok gera o getter de todo campo. Sem isto,
     * o hash de senha de <b>todos</b> os usuarios de <b>todas</b> as empresas
     * ia no corpo da resposta de {@code GET /api/superadmin/usuarios}.
     *
     * <p>O hash nao e' segredo reversivel, mas e' material de ataque offline:
     * um bcrypt fraco cai em dicionario. E a resposta nao precisa dele para
     * nada — quem autentica le a senha do corpo do login, nunca da entidade.
     *
     * <p>Colocar no campo e' mais seguro que no DTO: o proximo endpoint que
     * devolver a entidade por engano nasce protegido, em vez de vazar.
     */
    @JsonIgnore
    @Column(name = "senha_hash")
    private String senhaHash;

    @Column
    private Boolean ativo;

    @Column(name = "mfa_habilitado")
    private Boolean mfaHabilitado;

    @Column(name = "tentativas_login")
    private Integer tentativasLogin;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "bc_core_usuario_perfil",
        schema = "brasil_saas",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "perfil_id")
    )
    private Set<Perfil> perfis = new HashSet<>();
    
    @Column(name = "foto_url", columnDefinition = "TEXT")
    private String fotoUrl;
    
    @Column(name = "foto_tipo_conteudo", length = 50)
    private String fotoTipoConteudo;
    
    @Column(name = "foto_tamanho")
    private Long fotoTamanho;
}
