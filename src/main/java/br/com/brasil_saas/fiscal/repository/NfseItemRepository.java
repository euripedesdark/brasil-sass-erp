package br.com.brasil_saas.fiscal.repository;

import br.com.brasil_saas.fiscal.model.NfseItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NfseItemRepository extends JpaRepository<NfseItem, Long> {
    List<NfseItem> findByNfseId(Long nfseId);
}
