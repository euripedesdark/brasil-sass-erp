package br.com.brasil_saas.projetos.repository;
import br.com.brasil_saas.projetos.model.PrjFaturamento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface PrjFaturamentoRepository extends JpaRepository<PrjFaturamento, Long> {
    Optional<PrjFaturamento> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
    List<PrjFaturamento> findByEmpresaIdAndDeletedAtIsNull(Long empresaId);
    List<PrjFaturamento> findByProjetoIdAndEmpresaIdAndDeletedAtIsNull(Long projetoId, Long empresaId);
}
