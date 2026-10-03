package br.com.brasil_saas.compras.repository;
import br.com.brasil_saas.compras.model.SolicitacaoCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface SolicitacaoCompraRepository extends JpaRepository<SolicitacaoCompra,Long>{
 // Os itens vem na mesma consulta. Lazy e a sessao fecha antes do Jackson
 // serializar: aprovar e listar devolviam 500 (could not initialize proxy - no
 // Session) sempre que a solicitacao tinha itens.
 @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "itens")
 List<SolicitacaoCompra> findByEmpresaIdAndDeletedAtIsNullOrderByDataSolicitacaoDesc(Long empresaId);

 @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "itens")
 Optional<SolicitacaoCompra> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id,Long empresaId);
}