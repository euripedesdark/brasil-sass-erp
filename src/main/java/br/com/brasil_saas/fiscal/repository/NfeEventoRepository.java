package br.com.brasil_saas.fiscal.repository;

import br.com.brasil_saas.fiscal.model.NfeEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NfeEventoRepository extends JpaRepository<NfeEvento, Long> {
    List<NfeEvento> findByNfeIdOrderBySequenciaDesc(Long nfeId);
}
