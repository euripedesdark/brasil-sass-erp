package br.com.brasil_saas.financeiro.repository;

import br.com.brasil_saas.financeiro.model.RetornoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RetornoItemRepository extends JpaRepository<RetornoItem, Long> {

    List<RetornoItem> findByRetornoId(Long retornoId);

    List<RetornoItem> findByTituloId(Long tituloId);
}
