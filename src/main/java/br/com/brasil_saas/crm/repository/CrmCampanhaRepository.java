package br.com.brasil_saas.crm.repository;
import br.com.brasil_saas.crm.model.CrmCampanha; import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface CrmCampanhaRepository extends JpaRepository<CrmCampanha,Long> {
 Optional<CrmCampanha> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id,Long empresaId);
 List<CrmCampanha> findByEmpresaIdAndDeletedAtIsNullOrderByIdDesc(Long empresaId);
}
