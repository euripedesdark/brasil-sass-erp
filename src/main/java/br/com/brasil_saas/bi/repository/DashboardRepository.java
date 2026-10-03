package br.com.brasil_saas.bi.repository;

import br.com.brasil_saas.bi.model.Dashboard;
import br.com.brasil_saas.shared.repository.TenantRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DashboardRepository extends TenantRepository<Dashboard, Long> {

    List<Dashboard> findByEmpresaIdAndAtivoTrue(Long empresaId);

    List<Dashboard> findByEmpresaIdAndPublicoTrue(Long empresaId);

    List<Dashboard> findByEmpresaIdAndTipo(Long empresaId, String tipo);

    List<Dashboard> findByEmpresaIdAndCriadoPor(Long empresaId, Long criadoPor);

    @Query("SELECT d FROM Dashboard d WHERE d.empresaId = :empresaId AND d.ativo = true ORDER BY d.dataCriacao DESC")
    List<Dashboard> findRecentByEmpresaId(Long empresaId);

    @Query("SELECT COUNT(d) FROM Dashboard d WHERE d.empresaId = :empresaId AND d.ativo = true")
    long countByEmpresaId(Long empresaId);
}
