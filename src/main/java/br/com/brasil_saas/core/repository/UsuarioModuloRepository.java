package br.com.brasil_saas.core.repository;

import br.com.brasil_saas.core.model.UsuarioModulo;
import br.com.brasil_saas.core.model.UsuarioModuloId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioModuloRepository extends JpaRepository<UsuarioModulo, UsuarioModuloId> {

    List<UsuarioModulo> findByUsuarioId(Long usuarioId);

    void deleteByUsuarioId(Long usuarioId);
}
