package br.com.brasil_saas.crm.repository;
import br.com.brasil_saas.crm.model.CrmAtividade;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface CrmAtividadeRepository extends JpaRepository<CrmAtividade, Long> {
    Optional<CrmAtividade> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
    List<CrmAtividade> findByEmpresaIdAndDeletedAtIsNull(Long empresaId);
    List<CrmAtividade> findByLeadIdAndEmpresaIdAndDeletedAtIsNull(Long leadId, Long empresaId);
}
