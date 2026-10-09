package br.com.brasil_saas.fiscal.repository;

import br.com.brasil_saas.fiscal.model.Reinf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import br.com.brasil_saas.core.model.Empresa;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface ReinfRepository extends JpaRepository<Reinf, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Empresa e where e.id = :empresaId")
    Optional<Empresa> bloquearEmpresa(@Param("empresaId") Long empresaId);
    Optional<Reinf> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
    List<Reinf> findByEmpresaIdAndCompetencia(Long empresaId, String competencia);
    List<Reinf> findByEmpresaIdOrderByCompetenciaDescGeradoAtDesc(Long empresaId);
    Optional<Reinf> findByEmpresaIdAndCompetenciaAndEvento(Long empresaId, String competencia, String evento);
}
