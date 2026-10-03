package br.com.brasil_saas.cadastro.repository;

import br.com.brasil_saas.cadastro.model.ProdutoImagem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProdutoImagemRepository extends JpaRepository<ProdutoImagem, Long> {
    List<ProdutoImagem> findByProdutoIdAndDeletedAtIsNullOrderByOrdem(Long produtoId);
}
