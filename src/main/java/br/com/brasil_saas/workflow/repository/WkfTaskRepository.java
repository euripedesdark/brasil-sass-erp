package br.com.brasil_saas.workflow.repository;
import br.com.brasil_saas.workflow.model.WkfTask;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface WkfTaskRepository extends JpaRepository<WkfTask, Long> {
    Optional<WkfTask> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
    List<WkfTask> findByEmpresaIdAndDeletedAtIsNull(Long empresaId);
    List<WkfTask> findByInstanceIdAndEmpresaIdAndDeletedAtIsNull(Long instanceId, Long empresaId);
    List<WkfTask> findByEmpresaIdAndStatusAndDeletedAtIsNull(Long empresaId, String status);
}
