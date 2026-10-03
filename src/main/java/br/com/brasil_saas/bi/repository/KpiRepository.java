package br.com.brasil_saas.bi.repository;

import br.com.brasil_saas.bi.model.Kpi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface KpiRepository extends JpaRepository<Kpi, Long> {
    
        List<Kpi> findByEmpresaIdAndIsActiveTrueOrderByNameAsc(Long empresaId);

    List<Kpi> findByEmpresaIdAndIsActiveTrue(Long empresaId);
    
    List<Kpi> findByEmpresaIdAndKpiTypeOrderByNameAsc(Long empresaId, String kpiType);
    
    Page<Kpi> findByEmpresaId(Long empresaId, Pageable pageable);
    
    List<Kpi> findByEmpresaIdAndKpiTypeIn(Long empresaId, List<String> kpiTypes);
}
