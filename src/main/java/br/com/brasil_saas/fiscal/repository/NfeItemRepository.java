package br.com.brasil_saas.fiscal.repository;

import br.com.brasil_saas.fiscal.model.NfeItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NfeItemRepository extends JpaRepository<NfeItem, Long> {
    List<NfeItem> findByNfeIdOrderByNumeroItem(Long nfeId);
    void deleteByNfeId(Long nfeId);
}
