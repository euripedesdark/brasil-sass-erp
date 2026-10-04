package br.com.brasil_saas.projetos.repository;
import br.com.brasil_saas.projetos.model.PrjEtapa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface PrjEtapaRepository extends JpaRepository<PrjEtapa, Long> {
    Optional<PrjEtapa> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
    List<PrjEtapa> findByEmpresaIdAndDeletedAtIsNull(Long empresaId);
    List<PrjEtapa> findByProjetoIdAndEmpresaIdAndDeletedAtIsNullOrderByOrdem(Long projetoId, Long empresaId);
}
