package br.com.brasil_saas.workflow.repository;
import br.com.brasil_saas.workflow.model.WkfDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface WkfDefinitionRepository extends JpaRepository<WkfDefinition, Long> {
    Optional<WkfDefinition> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
    List<WkfDefinition> findByEmpresaIdAndDeletedAtIsNull(Long empresaId);
}
