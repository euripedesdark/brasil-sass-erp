package br.com.brasil_saas.fiscal.repository;

import br.com.brasil_saas.fiscal.model.Cte;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CteRepository extends JpaRepository<Cte, Long> {
    Optional<Cte> findByEmpresaIdAndChaveAcesso(Long empresaId, String chaveAcesso);
}
