package br.com.brasil_saas.compras.repository;

import br.com.brasil_saas.compras.model.RecebimentoCompraItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecebimentoCompraItemRepository extends JpaRepository<RecebimentoCompraItem, Long> {
    List<RecebimentoCompraItem> findByEmpresaIdAndRecebimentoIdAndDeletedAtIsNullOrderByIdAsc(
            Long empresaId, Long recebimentoId);
}
