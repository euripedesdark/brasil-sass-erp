package br.com.brasil_saas.estoque.repository;

import br.com.brasil_saas.estoque.model.InventarioEstoqueItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventarioEstoqueItemRepository extends JpaRepository<InventarioEstoqueItem,Long> {
    List<InventarioEstoqueItem> findByInventarioIdOrderByIdAsc(Long inventarioId);
    Optional<InventarioEstoqueItem> findByIdAndInventarioId(Long id, Long inventarioId);

    @Query("select count(i) > 0 from InventarioEstoqueItem i where i.inventarioId = :inventarioId and i.produtoId = :produtoId and ((i.loteId = :loteId) or (i.loteId is null and :loteId is null)) and ((i.enderecoId = :enderecoId) or (i.enderecoId is null and :enderecoId is null))")
    boolean existsSameScope(@Param("inventarioId") Long inventarioId, @Param("produtoId") Long produtoId,
                            @Param("loteId") Long loteId, @Param("enderecoId") Long enderecoId);

    @Query("select count(i) > 0 from InventarioEstoqueItem i where i.inventarioId = :inventarioId and i.produtoId = :produtoId and ((i.loteId = :loteId) or (i.loteId is null and :loteId is null)) and ((i.enderecoId is null and :enderecoId is not null) or (i.enderecoId is not null and :enderecoId is null))")
    boolean existsMixedScope(@Param("inventarioId") Long inventarioId, @Param("produtoId") Long produtoId,
                             @Param("loteId") Long loteId, @Param("enderecoId") Long enderecoId);
}