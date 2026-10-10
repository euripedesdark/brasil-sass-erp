package br.com.brasil_saas.crm.repository;
import br.com.brasil_saas.crm.model.CrmCampanhaContato; import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface CrmCampanhaContatoRepository extends JpaRepository<CrmCampanhaContato,Long> {
 List<CrmCampanhaContato> findByCampanhaIdAndEmpresaIdAndDeletedAtIsNull(Long campanhaId,Long empresaId);
 Optional<CrmCampanhaContato> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id,Long empresaId);
 boolean existsByCampanhaIdAndLeadIdAndEmpresaIdAndDeletedAtIsNull(Long campanhaId,Long leadId,Long empresaId);
}
