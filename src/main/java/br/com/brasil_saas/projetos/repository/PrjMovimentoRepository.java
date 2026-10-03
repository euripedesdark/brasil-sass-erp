package br.com.brasil_saas.projetos.repository;
import br.com.brasil_saas.projetos.model.PrjMovimento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface PrjMovimentoRepository extends JpaRepository<PrjMovimento, Long> {
    Optional<PrjMovimento> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
    List<PrjMovimento> findByEmpresaIdAndDeletedAtIsNull(Long empresaId);
    List<PrjMovimento> findByProjetoIdAndEmpresaIdAndDeletedAtIsNull(Long projetoId, Long empresaId);
}
