package br.com.brasil_saas.compras.repository;

import br.com.brasil_saas.compras.model.ConferenciaFaturaCompraItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConferenciaFaturaCompraItemRepository extends JpaRepository<ConferenciaFaturaCompraItem, Long> {
    List<ConferenciaFaturaCompraItem> findByEmpresaIdAndConferenciaIdAndDeletedAtIsNullOrderByIdAsc(
            Long empresaId, Long conferenciaId);
}
