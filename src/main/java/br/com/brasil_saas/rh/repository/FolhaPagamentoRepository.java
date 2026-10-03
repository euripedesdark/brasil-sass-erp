package br.com.brasil_saas.rh.repository;
import br.com.brasil_saas.rh.model.FolhaPagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface FolhaPagamentoRepository extends JpaRepository<FolhaPagamento, Long> {
    List<FolhaPagamento> findByEmpresaIdOrderByCompetenciaDesc(Long empresaId);
}
