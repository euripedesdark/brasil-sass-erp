package br.com.brasil_saas.compras.repository;
import br.com.brasil_saas.compras.model.DevCompraItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface DevCompraItemRepository extends JpaRepository<DevCompraItem, Long> {
    List<DevCompraItem> findByEmpresaIdAndDevolucaoIdAndDeletedAtIsNull(Long empresaId, Long devolucaoId);
}
