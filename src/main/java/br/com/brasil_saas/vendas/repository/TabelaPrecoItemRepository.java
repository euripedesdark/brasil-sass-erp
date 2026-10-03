package br.com.brasil_saas.vendas.repository;

import br.com.brasil_saas.vendas.model.TabelaPrecoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TabelaPrecoItemRepository extends JpaRepository<TabelaPrecoItem, Long> {
    List<TabelaPrecoItem> findByEmpresaIdAndTabelaPrecoIdAndAtivoTrue(Long empresaId, Long tabelaPrecoId);
    Optional<TabelaPrecoItem> findByEmpresaIdAndTabelaPrecoIdAndProdutoIdAndAtivoTrue(
            Long empresaId, Long tabelaPrecoId, Long produtoId);
}