package br.com.brasil_saas.compras.repository;
import br.com.brasil_saas.compras.model.CotacaoFornecedorItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface CotacaoFornecedorItemRepository extends JpaRepository<CotacaoFornecedorItem,Long>{
 List<CotacaoFornecedorItem> findByEmpresaIdAndCotacaoFornecedorIdAndDeletedAtIsNull(Long empresaId,Long cotacaoFornecedorId);
}