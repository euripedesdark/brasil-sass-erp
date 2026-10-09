package br.com.brasil_saas.estoque.repository;

import br.com.brasil_saas.estoque.model.LoteEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface LoteEstoqueRepository extends JpaRepository<LoteEstoque, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from LoteEstoque l where l.empresaId = :empresaId and l.id = :id and l.deletedAt is null")
    Optional<LoteEstoque> findForUpdate(@Param("empresaId") Long empresaId, @Param("id") Long id);
    List<LoteEstoque> findByEmpresaIdAndDeletedAtIsNullOrderByDataValidadeAsc(Long empresaId);
    List<LoteEstoque> findByEmpresaIdAndProdutoIdAndDeletedAtIsNullOrderByDataValidadeAsc(Long empresaId, Long produtoId);
    List<LoteEstoque> findByEmpresaIdAndDepositoIdAndStatusAndDeletedAtIsNullOrderByDataValidadeAsc(Long empresaId, Long depositoId, String status);
    Optional<LoteEstoque> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
    Optional<LoteEstoque> findByEmpresaIdAndProdutoIdAndCodigoAndDeletedAtIsNull(Long empresaId, Long produtoId, String codigo);
}
