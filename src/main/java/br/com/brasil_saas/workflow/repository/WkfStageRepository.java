package br.com.brasil_saas.workflow.repository;
import br.com.brasil_saas.workflow.model.WkfStage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface WkfStageRepository extends JpaRepository<WkfStage, Long> {
    Optional<WkfStage> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
    List<WkfStage> findByEmpresaIdAndDeletedAtIsNull(Long empresaId);
    List<WkfStage> findByDefinitionIdAndEmpresaIdAndDeletedAtIsNullOrderByOrdem(Long definitionId, Long empresaId);
}
