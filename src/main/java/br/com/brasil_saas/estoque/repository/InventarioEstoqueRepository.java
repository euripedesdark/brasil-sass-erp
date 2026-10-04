package br.com.brasil_saas.estoque.repository;

import br.com.brasil_saas.estoque.model.InventarioEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventarioEstoqueRepository extends JpaRepository<InventarioEstoque,Long> {
    List<InventarioEstoque> findByEmpresaIdAndDeletedAtIsNullOrderByDataContagemDesc(Long empresaId);
    Optional<InventarioEstoque> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);

    @Query("select count(i) > 0 from InventarioEstoque i where i.empresaId=:empresaId and i.depositoId=:depositoId and i.status='ABERTO' and i.deletedAt is null")
    boolean existsAberto(@Param("empresaId") Long empresaId, @Param("depositoId") Long depositoId);
}