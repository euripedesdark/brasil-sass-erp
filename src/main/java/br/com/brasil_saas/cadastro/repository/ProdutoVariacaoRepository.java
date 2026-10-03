package br.com.brasil_saas.cadastro.repository;

import br.com.brasil_saas.cadastro.model.ProdutoVariacao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProdutoVariacaoRepository extends JpaRepository<ProdutoVariacao, Long> {
    List<ProdutoVariacao> findByProdutoIdAndDeletedAtIsNull(Long produtoId);
}
