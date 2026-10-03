package br.com.brasil_saas.vendas.repository;

import br.com.brasil_saas.vendas.model.TabelaPreco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TabelaPrecoRepository extends JpaRepository<TabelaPreco, Long> {
    List<TabelaPreco> findByEmpresaIdAndAtivoTrueAndDeletedAtIsNullOrderByNomeAsc(Long empresaId);
    Optional<TabelaPreco> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
}
