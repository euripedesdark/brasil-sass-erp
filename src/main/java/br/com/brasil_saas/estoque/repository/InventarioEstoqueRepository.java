package br.com.brasil_saas.estoque.repository;

import br.com.brasil_saas.estoque.model.InventarioEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InventarioEstoqueRepository extends JpaRepository<InventarioEstoque,Long> {
    List<InventarioEstoque> findByEmpresaIdAndDeletedAtIsNullOrderByDataContagemDesc(Long empresaId);
    Optional<InventarioEstoque> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
}