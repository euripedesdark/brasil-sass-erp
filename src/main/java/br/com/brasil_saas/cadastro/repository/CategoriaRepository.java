package br.com.brasil_saas.cadastro.repository;

import br.com.brasil_saas.cadastro.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findByDeletedAtIsNullOrderByNome();
    List<Categoria> findByCategoriaPaiIdAndDeletedAtIsNullOrderByNome(Long categoriaPaiId);
    boolean existsByNomeIgnoreCaseAndDeletedAtIsNull(String nome);
}
