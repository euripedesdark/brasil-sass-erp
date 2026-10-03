package br.com.brasil_saas.estoque.repository;

import br.com.brasil_saas.estoque.model.TransferenciaEstoqueItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransferenciaEstoqueItemRepository extends JpaRepository<TransferenciaEstoqueItem, Long> {
    List<TransferenciaEstoqueItem> findByTransferenciaId(Long transferenciaId);
}