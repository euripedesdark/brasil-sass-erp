package br.com.brasil_saas.compras.repository;
import br.com.brasil_saas.compras.model.DevCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface DevCompraRepository extends JpaRepository<DevCompra, Long> {
    List<DevCompra> findByEmpresaIdAndDeletedAtIsNullOrderByIdDesc(Long empresaId);
    Optional<DevCompra> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
}
