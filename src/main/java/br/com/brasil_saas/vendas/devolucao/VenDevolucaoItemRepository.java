package br.com.brasil_saas.vendas.devolucao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import br.com.brasil_saas.vendas.devolucao.VenDevolucaoItem;
public interface VenDevolucaoItemRepository extends JpaRepository<VenDevolucaoItem, Long> {
    List<VenDevolucaoItem> findByDevolucaoIdAndEmpresaIdAndDeletedAtIsNull(Long devolucaoId, Long empresaId);
}
