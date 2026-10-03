package br.com.brasil_saas.financeiro.repository;

import br.com.brasil_saas.financeiro.model.RemessaItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RemessaItemRepository extends JpaRepository<RemessaItem, Long> {

    List<RemessaItem> findByRemessaId(Long remessaId);
}
