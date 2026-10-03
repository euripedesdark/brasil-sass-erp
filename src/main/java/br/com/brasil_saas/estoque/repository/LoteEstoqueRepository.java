package br.com.brasil_saas.estoque.repository;

import br.com.brasil_saas.estoque.model.LoteEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LoteEstoqueRepository extends JpaRepository<LoteEstoque, Long> {
    List<LoteEstoque> findByEmpresaIdAndDeletedAtIsNullOrderByDataValidadeAsc(Long empresaId);
    List<LoteEstoque> findByEmpresaIdAndProdutoIdAndDeletedAtIsNullOrderByDataValidadeAsc(Long empresaId, Long produtoId);
    List<LoteEstoque> findByEmpresaIdAndDepositoIdAndStatusAndDeletedAtIsNullOrderByDataValidadeAsc(Long empresaId, Long depositoId, String status);
    Optional<LoteEstoque> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
    Optional<LoteEstoque> findByEmpresaIdAndProdutoIdAndCodigoAndDeletedAtIsNull(Long empresaId, Long produtoId, String codigo);
}