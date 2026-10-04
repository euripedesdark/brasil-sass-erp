package br.com.brasil_saas.projetos.repository;
import br.com.brasil_saas.projetos.model.PrjRisco;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface PrjRiscoRepository extends JpaRepository<PrjRisco, Long> {
    Optional<PrjRisco> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
    List<PrjRisco> findByEmpresaIdAndDeletedAtIsNull(Long empresaId);
    List<PrjRisco> findByProjetoIdAndEmpresaIdAndDeletedAtIsNull(Long projetoId, Long empresaId);
}
