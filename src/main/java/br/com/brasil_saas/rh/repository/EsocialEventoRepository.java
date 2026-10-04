package br.com.brasil_saas.rh.repository;
import br.com.brasil_saas.rh.model.EsocialEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface EsocialEventoRepository extends JpaRepository<EsocialEvento, Long> {
    Optional<EsocialEvento> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
    List<EsocialEvento> findByEmpresaIdAndDeletedAtIsNullOrderByIdDesc(Long empresaId);
    List<EsocialEvento> findByEmpresaIdAndStatusAndDeletedAtIsNull(Long empresaId, String status);
}
