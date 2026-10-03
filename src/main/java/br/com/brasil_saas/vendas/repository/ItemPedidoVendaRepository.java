package br.com.brasil_saas.vendas.repository;

import br.com.brasil_saas.vendas.model.ItemPedidoVenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ItemPedidoVendaRepository extends JpaRepository<ItemPedidoVenda, Long> {
    List<ItemPedidoVenda> findByPedidoId(Long pedidoId);
}
