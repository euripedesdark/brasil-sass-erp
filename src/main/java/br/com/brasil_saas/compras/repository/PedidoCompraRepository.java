package br.com.brasil_saas.compras.repository;

import br.com.brasil_saas.compras.model.PedidoCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface PedidoCompraRepository extends JpaRepository<PedidoCompra, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PedidoCompra p WHERE p.id = :id")
    Optional<PedidoCompra> findByIdForUpdate(Long id);
    List<PedidoCompra> findByEmpresaIdOrderByDataEmissaoDesc(Long empresaId);
    List<PedidoCompra> findByEmpresaIdAndStatus(Long empresaId, String status);
}
