package br.com.brasil_saas.wms.repository;
import br.com.brasil_saas.wms.model.WmsOndaItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface WmsOndaItemRepository extends JpaRepository<WmsOndaItem, Long> {
    Optional<WmsOndaItem> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
    List<WmsOndaItem> findByEmpresaIdAndDeletedAtIsNull(Long empresaId);
    List<WmsOndaItem> findByOndaIdAndEmpresaIdAndDeletedAtIsNull(Long ondaId, Long empresaId);
}
