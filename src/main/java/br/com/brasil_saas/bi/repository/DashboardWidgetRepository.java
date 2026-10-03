package br.com.brasil_saas.bi.repository;

import br.com.brasil_saas.bi.model.DashboardWidget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DashboardWidgetRepository extends JpaRepository<DashboardWidget, Long> {
    
    List<DashboardWidget> findByDashboardId(Long dashboardId);
    
    List<DashboardWidget> findByDashboardIdOrderByPositionXAscPositionYAsc(Long dashboardId);
    
    Long countByDashboardId(Long dashboardId);
}
