package br.com.brasil_saas.servicos.repository;
import br.com.brasil_saas.servicos.model.OsApontamento;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface OsApontamentoRepository extends JpaRepository<OsApontamento, Long> {
    List<OsApontamento> findByOsIdAndDeletedAtIsNull(Long osId);
}
