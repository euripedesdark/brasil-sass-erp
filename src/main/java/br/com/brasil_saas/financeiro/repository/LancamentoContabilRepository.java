package br.com.brasil_saas.financeiro.repository;

import br.com.brasil_saas.financeiro.model.LancamentoContabil;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LancamentoContabilRepository extends JpaRepository<LancamentoContabil, Long> {
    Optional<LancamentoContabil> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
    List<LancamentoContabil> findByEmpresaIdAndDeletedAtIsNullOrderByDataLancamentoDesc(Long empresaId);
}
