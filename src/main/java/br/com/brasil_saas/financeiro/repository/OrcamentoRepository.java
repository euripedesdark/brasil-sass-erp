package br.com.brasil_saas.financeiro.repository;
import br.com.brasil_saas.financeiro.model.Orcamento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface OrcamentoRepository extends JpaRepository<Orcamento, Long> {
    Optional<Orcamento> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
    List<Orcamento> findByEmpresaIdAndDeletedAtIsNull(Long empresaId);
    List<Orcamento> findByEmpresaIdAndAnoAndDeletedAtIsNull(Long empresaId, Integer ano);
}
