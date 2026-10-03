package br.com.brasil_saas.compras.repository;

import br.com.brasil_saas.compras.model.RecebimentoCompra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecebimentoCompraRepository extends JpaRepository<RecebimentoCompra, Long> {
    List<RecebimentoCompra> findByEmpresaIdOrderByDataRecebimentoDescCreatedAtDesc(Long empresaId);

    Optional<RecebimentoCompra> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);

    List<RecebimentoCompra> findByEmpresaIdAndPedidoIdAndDeletedAtIsNullOrderByDataRecebimentoDesc(
            Long empresaId, Long pedidoId);
}
