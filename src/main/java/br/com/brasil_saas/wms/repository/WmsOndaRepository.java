package br.com.brasil_saas.wms.repository;
import br.com.brasil_saas.wms.model.WmsOnda;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface WmsOndaRepository extends JpaRepository<WmsOnda, Long> {
    Optional<WmsOnda> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
    List<WmsOnda> findByEmpresaIdAndDeletedAtIsNull(Long empresaId);
    List<WmsOnda> findByEmpresaIdAndStatusAndDeletedAtIsNull(Long empresaId, String status);
}
