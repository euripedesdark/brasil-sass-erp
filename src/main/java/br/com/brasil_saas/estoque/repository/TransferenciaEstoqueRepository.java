package br.com.brasil_saas.estoque.repository;

import br.com.brasil_saas.estoque.model.TransferenciaEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransferenciaEstoqueRepository extends JpaRepository<TransferenciaEstoque, Long> {
    List<TransferenciaEstoque> findByEmpresaIdAndDeletedAtIsNullOrderByDataTransferenciaDesc(Long empresaId);
}