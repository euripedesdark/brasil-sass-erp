package br.com.brasil_saas.core.repository;

import br.com.brasil_saas.core.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @Query("""
        SELECT u FROM Usuario u
        LEFT JOIN FETCH u.perfis p
        LEFT JOIN FETCH p.permissoes
        WHERE u.username = :username AND u.deletedAt IS NULL
        """)
    Optional<Usuario> findByUsernameWithAuthorities(String username);

    /**
     * Empresa do usuario, direto do banco.
     *
     * O empresaId do token vale para a requisicao, mas nao para a decisao de
     * "ele ja cadastrou a empresa?": o token e emitido no login, e o cadastro
     * acontece depois dele. Consultar o banco evita o caso em que o usuario
     * cadastra a empresa e so descobre no proximo login.
     *
     * Optional vazio quando o usuario ainda nao tem empresa — que e
     * exatamente o estado que dispara a tela de configuracao.
     */
    @Query("SELECT u.empresaId FROM Usuario u WHERE u.id = :id")
    Optional<Long> findEmpresaIdById(@Param("id") Long id);

    /**
     * Usuarios de uma empresa, para a tela de administracao.
     *
     * <p>Era {@code findAll()}, que devolvia <b>todos</b> os usuarios de
     * <b>todas</b> as empresas para qualquer ADMIN. Agora a lista e' da empresa
     * de quem olha, e quem ve todas e' o SUPERUSER — que e' o unico papel que
     * administra o sistema inteiro.
     */
    List<Usuario> findAllByEmpresaIdAndDeletedAtIsNull(Long empresaId);

    @Modifying
    @Query("UPDATE Usuario u SET u.empresaId = :empresaId WHERE u.id = :id")
    int vincularEmpresa(@Param("id") Long id, @Param("empresaId") Long empresaId);

    /**
     * Existe algum SUPERUSER ativo, em qualquer empresa.
     *
     * <p>Existe para o "ultimo superuser" do {@code DELETE}: sem esta consulta o
     * proprio superuser pode se apagar e deixar o sistema sem ninguem capaz de
     * administeredar. A contagem e feita por nome de perfil, e nao por
     * authority do token, porque o token e do usuario que esta apagando — se
     * ele ja nao tem o perfil, a contagem e do que sobrou no banco.
     */
    @Query("""
        SELECT COUNT(DISTINCT u.id) FROM Usuario u
        JOIN u.perfis p
        WHERE u.ativo = true AND u.deletedAt IS NULL
          AND (p.nome = 'SUPERUSER' OR p.nome = 'SUPERADMIN')
        """)
    long contarSuperusersAtivos();

    /**
     * Quantos usuarios ativos tem o perfil pedido, para o mesmo tipo de trava.
     */
    @Query("""
        SELECT COUNT(DISTINCT u.id) FROM Usuario u
        JOIN u.perfis p
        WHERE u.id <> :id AND u.ativo = true AND u.deletedAt IS NULL AND p.nome = :perfil
        """)
    long contarAtivosComPerfilExceto(@Param("id") Long id, @Param("perfil") String perfil);
}
