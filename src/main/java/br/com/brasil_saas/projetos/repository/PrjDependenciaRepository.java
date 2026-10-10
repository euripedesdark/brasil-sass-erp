package br.com.brasil_saas.projetos.repository;
import br.com.brasil_saas.projetos.model.PrjDependencia; import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface PrjDependenciaRepository extends JpaRepository<PrjDependencia,Long> {
 List<PrjDependencia> findByProjetoIdAndEmpresaIdAndDeletedAtIsNull(Long projeto,Long empresa);
 Optional<PrjDependencia> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id,Long empresa);
}
