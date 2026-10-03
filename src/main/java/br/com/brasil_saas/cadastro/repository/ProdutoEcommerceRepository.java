package br.com.brasil_saas.cadastro.repository;

import br.com.brasil_saas.cadastro.model.ProdutoEcommerce;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProdutoEcommerceRepository extends JpaRepository<ProdutoEcommerce, Long> {
    Optional<ProdutoEcommerce> findByProdutoIdAndDeletedAtIsNull(Long produtoId);
}
