package br.com.brasil_saas.bi.repository;

import br.com.brasil_saas.bi.model.RelatorioAgendado;
import br.com.brasil_saas.shared.repository.TenantRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RelatorioAgendadoRepository extends TenantRepository<RelatorioAgendado, Long> {

    List<RelatorioAgendado> findByEmpresaIdAndAtivoTrue(Long empresaId);

    List<RelatorioAgendado> findByEmpresaIdAndProximaExecucaoBefore(Long empresaId, LocalDateTime data);

    List<RelatorioAgendado> findByEmpresaIdAndFrequencia(Long empresaId, String frequencia);

    @Query("SELECT ra FROM RelatorioAgendado ra WHERE ra.empresaId = :empresaId AND ra.ativo = true AND ra.proximaExecucao <= :now ORDER BY ra.proximaExecucao")
    List<RelatorioAgendado> findPendingExecutions(Long empresaId, LocalDateTime now);

    @Query("SELECT ra FROM RelatorioAgendado ra WHERE ra.ativo = true AND ra.proximaExecucao <= :now ORDER BY ra.proximaExecucao")
    List<RelatorioAgendado> findAllPendingExecutions(LocalDateTime now);

    @Query("SELECT COUNT(ra) FROM RelatorioAgendado ra WHERE ra.empresaId = :empresaId AND ra.ativo = true")
    long countByEmpresaId(Long empresaId);

    @Query("SELECT ra FROM RelatorioAgendado ra WHERE ra.empresaId = :empresaId AND ra.ativo = true ORDER BY ra.proximaExecucao")
    List<RelatorioAgendado> findAllActiveByEmpresaId(Long empresaId);
}
