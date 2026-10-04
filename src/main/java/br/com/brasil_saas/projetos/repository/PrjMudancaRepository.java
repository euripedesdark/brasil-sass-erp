package br.com.brasil_saas.projetos.repository;
import br.com.brasil_saas.projetos.model.PrjMudanca;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface PrjMudancaRepository extends JpaRepository<PrjMudanca, Long> {
    Optional<PrjMudanca> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
    List<PrjMudanca> findByEmpresaIdAndDeletedAtIsNull(Long empresaId);
    List<PrjMudanca> findByProjetoIdAndEmpresaIdAndDeletedAtIsNull(Long projetoId, Long empresaId);
}
