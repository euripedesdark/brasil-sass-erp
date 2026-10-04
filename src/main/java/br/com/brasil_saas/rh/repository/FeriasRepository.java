package br.com.brasil_saas.rh.repository;
import br.com.brasil_saas.rh.model.Ferias;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface FeriasRepository extends JpaRepository<Ferias, Long> {
    List<Ferias> findByEmpresaIdAndDeletedAtIsNullOrderByDataInicioDesc(Long empresaId);
    List<Ferias> findByEmpresaIdAndFuncionarioIdAndDeletedAtIsNullOrderByDataInicioDesc(Long empresaId, Long funcionarioId);
}
