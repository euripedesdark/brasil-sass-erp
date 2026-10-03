package br.com.brasil_saas.fiscal.repository;

import br.com.brasil_saas.fiscal.model.Mdfe;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MdfeRepository extends JpaRepository<Mdfe, Long> {
    Optional<Mdfe> findByEmpresaIdAndChaveAcesso(Long empresaId, String chaveAcesso);
}
