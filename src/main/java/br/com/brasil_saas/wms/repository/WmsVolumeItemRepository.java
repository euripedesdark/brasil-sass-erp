package br.com.brasil_saas.wms.repository;
import br.com.brasil_saas.wms.model.WmsVolumeItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface WmsVolumeItemRepository extends JpaRepository<WmsVolumeItem, Long> {
    Optional<WmsVolumeItem> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
    List<WmsVolumeItem> findByEmpresaIdAndDeletedAtIsNull(Long empresaId);
    List<WmsVolumeItem> findByVolumeIdAndEmpresaIdAndDeletedAtIsNull(Long volumeId, Long empresaId);
    List<WmsVolumeItem> findByOndaItemIdAndEmpresaIdAndDeletedAtIsNull(Long ondaItemId, Long empresaId);
}
