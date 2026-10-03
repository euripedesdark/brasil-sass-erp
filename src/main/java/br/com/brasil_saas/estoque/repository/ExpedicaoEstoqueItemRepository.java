package br.com.brasil_saas.estoque.repository;

import br.com.brasil_saas.estoque.model.ExpedicaoEstoqueItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExpedicaoEstoqueItemRepository extends JpaRepository<ExpedicaoEstoqueItem,Long> {
    List<ExpedicaoEstoqueItem> findByEmpresaIdAndExpedicaoIdOrderByIdAsc(Long empresaId, Long expedicaoId);
}