package br.com.brasil_saas.compras.repository;

import br.com.brasil_saas.compras.model.ConferenciaFaturaCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ConferenciaFaturaCompraRepository extends JpaRepository<ConferenciaFaturaCompra, Long> {
    List<ConferenciaFaturaCompra> findByEmpresaIdOrderByCreatedAtDesc(Long empresaId);
    Optional<ConferenciaFaturaCompra> findByIdAndEmpresaIdAndDeletedAtIsNull(Long id, Long empresaId);
}