package br.com.brasil_saas.compras.repository;
import br.com.brasil_saas.compras.model.CotacaoFornecedor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface CotacaoFornecedorRepository extends JpaRepository<CotacaoFornecedor,Long>{
 List<CotacaoFornecedor> findByEmpresaIdAndCotacaoIdAndDeletedAtIsNull(Long empresaId,Long cotacaoId);
 Optional<CotacaoFornecedor> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id,Long empresaId);
}