package br.com.brasil_saas.estoque.repository;

import br.com.brasil_saas.estoque.model.ExpedicaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ExpedicaoEstoqueRepository extends JpaRepository<ExpedicaoEstoque,Long> {
    List<ExpedicaoEstoque> findByEmpresaIdAndDeletedAtIsNullOrderByDataAberturaDesc(Long empresaId);
    Optional<ExpedicaoEstoque> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
    Optional<ExpedicaoEstoque> findByEmpresaIdAndPedidoVendaIdAndDeletedAtIsNull(Long empresaId, Long pedidoVendaId);
}