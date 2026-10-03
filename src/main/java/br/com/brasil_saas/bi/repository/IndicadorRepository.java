package br.com.brasil_saas.bi.repository;

import br.com.brasil_saas.bi.model.Indicador;
import br.com.brasil_saas.shared.repository.TenantRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IndicadorRepository extends TenantRepository<Indicador, Long> {

    List<Indicador> findByEmpresaIdAndAtivoTrue(Long empresaId);

    List<Indicador> findByEmpresaIdAndCategoria(Long empresaId, String categoria);

    List<Indicador> findByEmpresaIdAndVisivelDashboardTrue(Long empresaId);

    @Query("SELECT i FROM Indicador i WHERE i.empresaId = :empresaId AND i.ativo = true ORDER BY i.ordemExibicao, i.nome")
    List<Indicador> findAllActiveByEmpresaId(Long empresaId);

    @Query("SELECT COUNT(i) FROM Indicador i WHERE i.empresaId = :empresaId AND i.ativo = true")
    long countByEmpresaId(Long empresaId);

    @Query("SELECT i FROM Indicador i WHERE i.empresaId = :empresaId AND i.categoria = :categoria AND i.visivelDashboard = true ORDER BY i.ordemExibicao")
    List<Indicador> findDashboardIndicators(Long empresaId, String categoria);

    @Query("SELECT i FROM Indicador i WHERE i.empresaId = :empresaId AND i.valorAtual IS NOT NULL AND i.ativo = true")
    List<Indicador> findWithValues(Long empresaId);
}
