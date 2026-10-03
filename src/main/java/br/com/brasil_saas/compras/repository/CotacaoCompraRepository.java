package br.com.brasil_saas.compras.repository;
import br.com.brasil_saas.compras.model.CotacaoCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface CotacaoCompraRepository extends JpaRepository<CotacaoCompra,Long>{
 List<CotacaoCompra> findByEmpresaIdAndDeletedAtIsNullOrderByDataAberturaDesc(Long empresaId);
 Optional<CotacaoCompra> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id,Long empresaId);
}