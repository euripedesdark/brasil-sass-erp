package br.com.brasil_saas.financeiro.repository;
import br.com.brasil_saas.financeiro.model.OrcamentoRealizado;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface OrcamentoRealizadoRepository extends JpaRepository<OrcamentoRealizado, Long> {
    Optional<OrcamentoRealizado> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
    List<OrcamentoRealizado> findByEmpresaIdAndDeletedAtIsNull(Long empresaId);
    List<OrcamentoRealizado> findByOrcamentoIdAndEmpresaIdAndDeletedAtIsNull(Long orcamentoId, Long empresaId);
}
