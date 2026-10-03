package br.com.brasil_saas.financeiro.repository;

import br.com.brasil_saas.financeiro.model.ConciliacaoItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConciliacaoItemRepository extends JpaRepository<ConciliacaoItem, Long> {
    List<ConciliacaoItem> findByConciliacaoIdAndDeletedAtIsNullOrderByIdAsc(Long conciliacaoId);
    Optional<ConciliacaoItem> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
    boolean existsByConciliacaoIdAndExtratoIdAndDeletedAtIsNull(Long conciliacaoId, Long extratoId);
}