package br.com.brasil_saas.producao.repository;
import br.com.brasil_saas.producao.model.CentroTrabalho;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface CentroTrabalhoRepository extends JpaRepository<CentroTrabalho,Long>{
 List<CentroTrabalho> findByEmpresaIdAndDeletedAtIsNullOrderByCodigo(Long empresaId);
 Optional<CentroTrabalho> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id,Long empresaId);
}
