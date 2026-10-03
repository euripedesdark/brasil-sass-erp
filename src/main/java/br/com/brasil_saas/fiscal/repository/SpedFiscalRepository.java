package br.com.brasil_saas.fiscal.repository;

import br.com.brasil_saas.fiscal.model.SpedFiscal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SpedFiscalRepository extends JpaRepository<SpedFiscal, Long> {
    Optional<SpedFiscal> findByEmpresaIdAndCompetencia(Long empresaId, String competencia);
}
