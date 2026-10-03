package br.com.brasil_saas.servicos.repository;
import br.com.brasil_saas.servicos.model.OrdemServico;
import org.springframework.data.domain.Page; import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.Optional;
public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long> {
    Optional<OrdemServico> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
    Page<OrdemServico> findByEmpresaIdAndDeletedAtIsNull(Long empresaId, Pageable pageable);
    Page<OrdemServico> findByEmpresaIdAndStatusAndDeletedAtIsNull(Long empresaId, String status, Pageable pageable);
    long countByEmpresaIdAndDeletedAtIsNull(Long empresaId);
}
