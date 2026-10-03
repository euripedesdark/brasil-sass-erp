package br.com.brasil_saas.fiscal.repository;

import br.com.brasil_saas.fiscal.model.CteItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CteItemRepository extends JpaRepository<CteItem, Long> {
    List<CteItem> findByCteId(Long cteId);
}
