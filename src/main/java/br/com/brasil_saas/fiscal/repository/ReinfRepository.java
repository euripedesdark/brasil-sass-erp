package br.com.brasil_saas.fiscal.repository;

import br.com.brasil_saas.fiscal.model.Reinf;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReinfRepository extends JpaRepository<Reinf, Long> {
    List<Reinf> findByEmpresaIdAndCompetencia(Long empresaId, String competencia);
    List<Reinf> findByEmpresaIdOrderByCompetenciaDescGeradoAtDesc(Long empresaId);
    Optional<Reinf> findByEmpresaIdAndCompetenciaAndEvento(Long empresaId, String competencia, String evento);
}
