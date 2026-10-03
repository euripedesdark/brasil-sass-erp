package br.com.brasil_saas.fiscal.repository;

import br.com.brasil_saas.fiscal.model.NfceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NfceItemRepository extends JpaRepository<NfceItem, Long> {
    List<NfceItem> findByNfceId(Long nfceId);
}
