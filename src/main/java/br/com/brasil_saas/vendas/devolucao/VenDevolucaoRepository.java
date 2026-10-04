package br.com.brasil_saas.vendas.devolucao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import br.com.brasil_saas.vendas.devolucao.VenDevolucao;
public interface VenDevolucaoRepository extends JpaRepository<VenDevolucao, Long> {
    Optional<VenDevolucao> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
    List<VenDevolucao> findByEmpresaIdAndDeletedAtIsNullOrderByIdDesc(Long empresaId);
}
