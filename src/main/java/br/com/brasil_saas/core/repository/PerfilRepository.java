package br.com.brasil_saas.core.repository;

import br.com.brasil_saas.core.model.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PerfilRepository extends JpaRepository<Perfil, Long> {

    /**
     * O perfil pelo nome, na empresa.
     *
     * <p>Existe porque o cadastro de usuario recebe o perfil pelo nome que o
     * administrator digitou na tela ("ADMIN", "VENDEDOR"), e nao pelo id. A
     * empresa entra na busca porque o perfil e' dado de tenant: o mesmo nome
     * numa empresa e' outro perfil na outra, e um ADMIN de empresa nenhuma nao
     * pode virar superuser por causa de um nome.
     */
    @Query("SELECT p FROM Perfil p WHERE p.nome = :nome AND p.empresaId = :empresaId AND p.deletedAt IS NULL")
    Optional<Perfil> findByNomeAndEmpresaId(@Param("nome") String nome, @Param("empresaId") Long empresaId);

    /**
     * Os nomes de perfil que existem na empresa, para a tela de cadastro
     * oferecer a lista certa em vez de o administrador digitar errado e receber
     * erro.
     */
    @Query("SELECT p.nome FROM Perfil p WHERE p.empresaId = :empresaId AND p.deletedAt IS NULL ORDER BY p.nome")
    List<String> listarNomesPorEmpresa(@Param("empresaId") Long empresaId);
}
