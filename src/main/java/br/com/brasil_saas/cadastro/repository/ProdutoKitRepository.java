package br.com.brasil_saas.cadastro.repository;

import br.com.brasil_saas.cadastro.model.ProdutoKit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProdutoKitRepository extends JpaRepository<ProdutoKit, Long> {
    List<ProdutoKit> findByKitId(Long kitId);
    List<ProdutoKit> findByItemId(Long itemId);
}
