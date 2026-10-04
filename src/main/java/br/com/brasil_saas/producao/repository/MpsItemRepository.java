package br.com.brasil_saas.producao.repository;
import br.com.brasil_saas.producao.model.MpsItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface MpsItemRepository extends JpaRepository<MpsItem, Long> {
    Optional<MpsItem> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
    List<MpsItem> findByEmpresaIdAndDeletedAtIsNull(Long empresaId);
    List<MpsItem> findByEmpresaIdAndPeriodoAndDeletedAtIsNull(Long empresaId, String periodo);
}
