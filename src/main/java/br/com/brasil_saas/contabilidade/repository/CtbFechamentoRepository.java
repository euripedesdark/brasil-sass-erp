package br.com.brasil_saas.contabilidade.repository;
import br.com.brasil_saas.contabilidade.model.CtbFechamento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface CtbFechamentoRepository extends JpaRepository<CtbFechamento, Long> {
    Optional<CtbFechamento> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
    List<CtbFechamento> findByEmpresaIdAndDeletedAtIsNull(Long empresaId);
    Optional<CtbFechamento> findByEmpresaIdAndPeriodoAndDeletedAtIsNull(Long empresaId, String periodo);
}
