package br.com.brasil_saas.core.model;

import br.com.brasil_saas.shared.model.TenantEntity;
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
public class Usuario extends TenantEntity {

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
