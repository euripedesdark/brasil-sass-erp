package br.com.brasil_saas.compras.repository;
import br.com.brasil_saas.compras.model.DevCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface DevCompraRepository extends JpaRepository<DevCompra, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select d from DevCompra d where d.id = :id and d.empresaId = :empresaId and d.deletedAt is null")
    Optional<DevCompra> findForUpdate(@org.springframework.data.repository.query.Param("id") Long id,
                                   @org.springframework.data.repository.query.Param("empresaId") Long empresaId);
    List<DevCompra> findByPedidoIdAndEmpresaIdAndDeletedAtIsNull(Long pedidoId, Long empresaId);
    List<DevCompra> findByEmpresaIdAndDeletedAtIsNullOrderByIdDesc(Long empresaId);
    Optional<DevCompra> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
}
