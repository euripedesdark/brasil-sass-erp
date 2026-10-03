package br.com.brasil_saas.producao.repository;

import br.com.brasil_saas.producao.model.ItemProducao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemProducaoRepository extends JpaRepository<ItemProducao, Long> {
}
