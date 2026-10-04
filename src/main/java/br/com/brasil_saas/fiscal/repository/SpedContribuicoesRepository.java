package br.com.brasil_saas.fiscal.repository;

import br.com.brasil_saas.fiscal.model.SpedContribuicoes;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SpedContribuicoesRepository extends JpaRepository<SpedContribuicoes, Long> {
    Optional<SpedContribuicoes> findByEmpresaIdAndCompetencia(Long empresaId, String competencia);
    java.util.List<SpedContribuicoes> findByEmpresaIdOrderByCompetenciaDesc(Long empresaId);
}
