package br.com.brasil_saas.cadastro.repository;

import br.com.brasil_saas.cadastro.model.UnidadeMedida;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UnidadeMedidaRepository extends JpaRepository<UnidadeMedida, Long> {
    List<UnidadeMedida> findByDeletedAtIsNullOrderByNome();
    Optional<UnidadeMedida> findBySiglaAndDeletedAtIsNull(String sigla);
    boolean existsBySiglaAndDeletedAtIsNull(String sigla);
}
