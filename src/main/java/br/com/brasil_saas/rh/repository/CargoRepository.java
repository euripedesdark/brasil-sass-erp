package br.com.brasil_saas.rh.repository;
import br.com.brasil_saas.rh.model.Cargo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface CargoRepository extends JpaRepository<Cargo, Long> {
    List<Cargo> findByEmpresaIdAndAtivoTrueOrderByNome(Long empresaId);
}
