package br.com.brasil_saas.wms.repository;
import br.com.brasil_saas.wms.model.WmsVolume;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface WmsVolumeRepository extends JpaRepository<WmsVolume, Long> {
    Optional<WmsVolume> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
    List<WmsVolume> findByEmpresaIdAndDeletedAtIsNull(Long empresaId);
    List<WmsVolume> findByExpedicaoIdAndEmpresaIdAndDeletedAtIsNull(Long expedicaoId, Long empresaId);
}
