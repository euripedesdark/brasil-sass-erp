package br.com.brasil_saas.ativos.repository;
import br.com.brasil_saas.ativos.model.Manutencao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ManutencaoRepository extends JpaRepository<Manutencao,Long>{
 List<Manutencao> findAllByEmpresaIdAndDeletedAtIsNullOrderByDataProgramadaAsc(Long empresaId);
}