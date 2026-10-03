package br.com.brasil_saas.contabilidade.repository;
import br.com.brasil_saas.contabilidade.model.CtbLancamento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface CtbLancamentoRepository extends JpaRepository<CtbLancamento, Long> {
    Optional<CtbLancamento> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
    List<CtbLancamento> findByEmpresaIdAndDeletedAtIsNull(Long empresaId);
    List<CtbLancamento> findByEmpresaIdAndPeriodoAndDeletedAtIsNullOrderByDataDescIdDesc(Long empresaId, String periodo);
    List<CtbLancamento> findByEmpresaIdAndStatusAndDeletedAtIsNull(Long empresaId, String status);
}
