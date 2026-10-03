package br.com.brasil_saas.rh.repository;

import br.com.brasil_saas.rh.model.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {
    List<Funcionario> findByEmpresaIdAndAtivoTrue(Long empresaId);

    /** O nome do colaborador vive em bc_cad_pessoa, por isso o JOIN. */
    @Query("""
        SELECT f FROM Funcionario f
        JOIN br.com.brasil_saas.cadastro.model.Pessoa p ON p.id = f.pessoaId
        WHERE f.empresaId = :empresaId AND f.ativo = true
        ORDER BY p.nome
        """)
    List<Funcionario> listarAtivosOrdenados(@Param("empresaId") Long empresaId);

    Optional<Funcionario> findByUsuarioId(Long usuarioId);
}
